package com.badlogic.drop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;


public class Main implements ApplicationListener {

    // ------------------------------------------------------------------
    // CONSTANTES (mexa aqui para ajustar o jogo)
    // ------------------------------------------------------------------

    // Tamanho do mundo (em unidades, não em pixels)
    private static final float LARGURA_MUNDO = 25f;
    private static final float ALTURA_MUNDO = 15f;

    // Foguete
    private static final float FOGUETE_LARGURA = 4f;
    private static final float FOGUETE_ALTURA = 5f;

    // Meteoros
    private static final float METEORO_TAMANHO = 4f;
    private static final float METEORO_VELOCIDADE = 2.0f;   // unidades por segundo
    private static final float METEORO_INTERVALO = 2.4f;    // segundos entre cada meteoro
    private static final float METEORO_ESPACO_MINIMO = 0.5f; // folga mínima entre meteoros
    private static final int TENTATIVAS_POSICAO = 10;        // tentativas de achar um lugar livre

    // Texto
    private static final float FONTE_ESCALA = 0.08f;
    private static final float BLOCO_PADDING = 0.25f;        // espaço entre o texto e a borda do bloco
    private static final Color BLOCO_COR = new Color(0f, 0f, 0f, 0.75f);

    // ------------------------------------------------------------------
    // CLASSE AUXILIAR
    // ------------------------------------------------------------------

    /** Um meteoro com a sua equação e a resposta correta. */
    public static class MeteoroObjeto {
        public final Sprite sprite;
        public final String equacao;
        public final int respostaCorreta;

        public MeteoroObjeto(Sprite sprite, String equacao, int respostaCorreta) {
            this.sprite = sprite;
            this.equacao = equacao;
            this.respostaCorreta = respostaCorreta;
        }
    }

    // ------------------------------------------------------------------
    // ATRIBUTOS
    // ------------------------------------------------------------------

    // Texturas
    private Texture backgroundTexture;
    private Texture fogueteTexture;
    private Texture meteoroTexture;
    private Texture pixelBranco; // 1x1 branco, usado para desenhar os blocos

    // Desenho
    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private BitmapFont font;
    private final GlyphLayout layout = new GlyphLayout(); // mede o tamanho do texto

    // Objetos do jogo
    private Sprite fogueteSprite;
    private Array<MeteoroObjeto> meteoros;

    // Estado do jogo
    private float meteoroTimer;
    private final StringBuilder entradaUsuario = new StringBuilder();

    // Retângulos reutilizados (evita criar objetos novos a cada frame)
    private final Rectangle retanguloNovo = new Rectangle();
    private final Rectangle retanguloExistente = new Rectangle();

    // Vida
    private int vida = 3;

    // ------------------------------------------------------------------
    // CICLO DE VIDA
    // ------------------------------------------------------------------

    @Override
    public void create() {
        backgroundTexture = new Texture("background.png");
        fogueteTexture = new Texture("foguete.png");
        meteoroTexture = new Texture("meteoro.png");
        pixelBranco = criarPixelBranco();

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(LARGURA_MUNDO, ALTURA_MUNDO);

        fogueteSprite = new Sprite(fogueteTexture);
        fogueteSprite.setSize(FOGUETE_LARGURA, FOGUETE_ALTURA);
        // O foguete fica sempre centralizado na parte de baixo
        fogueteSprite.setPosition((LARGURA_MUNDO - FOGUETE_LARGURA) / 2f, 0);

        meteoros = new Array<MeteoroObjeto>();

        configurarFonte();
        configurarInput();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        atualizar();
        desenhar();
    }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        fogueteTexture.dispose();
        meteoroTexture.dispose();
        pixelBranco.dispose();
        spriteBatch.dispose();
        font.dispose();
    }

    // ------------------------------------------------------------------
    // CONFIGURAÇÃO INICIAL
    // ------------------------------------------------------------------

    /** Cria uma textura de 1x1 pixel branco. Esticada, vira qualquer retângulo colorido. */
    private Texture criarPixelBranco() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture textura = new Texture(pixmap);
        pixmap.dispose();
        return textura;
    }

    private void configurarFonte() {
        font = new BitmapFont();
        font.getData().setScale(FONTE_ESCALA);
        font.setColor(Color.WHITE);
        // Filtro linear deixa o texto pequeno mais nítido (menos "borrado/embolado")
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        // Impede que a fonte arredonde posições para inteiros (o que quebra com escala pequena)
        font.setUseIntegerPositions(false);
    }

    private void configurarInput() {
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyTyped(char caractere) {
                tratarTecla(caractere);
                return true;
            }
        });
    }

    // ------------------------------------------------------------------
    // INPUT
    // ------------------------------------------------------------------

    private void tratarTecla(char caractere) {
        if (caractere == '\n' || caractere == '\r') {
            confirmarResposta();
        } else if (caractere == '\b') {
            apagarUltimoDigito();
        } else if (Character.isDigit(caractere)) {
            entradaUsuario.append(caractere);
        }
    }

    private void apagarUltimoDigito() {
        if (entradaUsuario.length() > 0) {
            entradaUsuario.deleteCharAt(entradaUsuario.length() - 1);
        }
    }

    /** Chamado ao apertar Enter: destrói o primeiro meteoro cuja resposta bate com a digitada. */
    private void confirmarResposta() {
        if (entradaUsuario.length() == 0) return;

        try {
            int respostaDigitada = Integer.parseInt(entradaUsuario.toString().trim());
            destruirMeteoroComResposta(respostaDigitada);
        } catch (NumberFormatException e) {
            // Entrada inválida: ignora
        }

        entradaUsuario.setLength(0);
    }

    private void destruirMeteoroComResposta(int resposta) {
        for (int i = 0; i < meteoros.size; i++) {
            if (meteoros.get(i).respostaCorreta == resposta) {
                meteoros.removeIndex(i);
            }
        }
    }

    // ------------------------------------------------------------------
    // LÓGICA
    // ------------------------------------------------------------------

    private void atualizar() {
        float delta = Gdx.graphics.getDeltaTime();
        moverMeteoros(delta);
        atualizarSpawnDeMeteoros(delta);
    }

    private void moverMeteoros(float delta) {
        // Percorre de trás pra frente para poder remover itens com segurança
        for (int i = meteoros.size - 1; i >= 0; i--) {
            Sprite sprite = meteoros.get(i).sprite;
            sprite.translateY(-METEORO_VELOCIDADE * delta);

            // Saiu da tela por baixo (futuramente: dar dano ao foguete)
            if (sprite.getY() < -sprite.getHeight()) {
                meteoros.removeIndex(i);
                vida -= 1;
            }
            if (vida <= 0){
                Gdx.app.exit();
            }
        }
    }

    private void atualizarSpawnDeMeteoros(float delta) {
        meteoroTimer += delta;
        if (meteoroTimer < METEORO_INTERVALO) return;

        // Só zera o timer se conseguiu criar; se não houver espaço livre,
        // tenta de novo no próximo frame.
        if (criarMeteoro()) {
            meteoroTimer = 0;
        }
    }

    /**
     * Cria um meteoro numa posição horizontal que não colide com os outros.
     * @return true se criou, false se não achou espaço livre
     */
    private boolean criarMeteoro() {
        float x = encontrarXLivre();
        if (x < 0) return false;

        Sprite sprite = new Sprite(meteoroTexture);
        sprite.setSize(METEORO_TAMANHO, METEORO_TAMANHO);
        sprite.setPosition(x, ALTURA_MUNDO);

        int a = MathUtils.random(10, 20);
        int b = MathUtils.random(10, 20);

        meteoros.add(new MeteoroObjeto(sprite, a + "+" + b, a + b));
        return true;
    }

    /** Sorteia posições X até achar uma que não sobreponha outro meteoro. Retorna -1 se falhar. */
    private float encontrarXLivre() {
        for (int tentativa = 0; tentativa < TENTATIVAS_POSICAO; tentativa++) {
            float x = MathUtils.random(0f, LARGURA_MUNDO - METEORO_TAMANHO);
            retanguloNovo.set(x, ALTURA_MUNDO, METEORO_TAMANHO, METEORO_TAMANHO);

            if (!sobrepoeAlgumMeteoro(retanguloNovo)) {
                return x;
            }
        }
        return -1f;
    }

    private boolean sobrepoeAlgumMeteoro(Rectangle area) {
        for (MeteoroObjeto meteoro : meteoros) {
            Sprite s = meteoro.sprite;
            // Aumenta um pouco o retângulo existente para manter uma folga entre eles
            retanguloExistente.set(
                s.getX() - METEORO_ESPACO_MINIMO,
                s.getY() - METEORO_ESPACO_MINIMO,
                s.getWidth() + METEORO_ESPACO_MINIMO * 2,
                s.getHeight() + METEORO_ESPACO_MINIMO * 2
            );
            if (area.overlaps(retanguloExistente)) return true;
        }
        return false;
    }

    // ------------------------------------------------------------------
    // DESENHO
    // ------------------------------------------------------------------

    private void desenhar() {
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        spriteBatch.draw(backgroundTexture, 0, 0, LARGURA_MUNDO, ALTURA_MUNDO);
        fogueteSprite.draw(spriteBatch);

        desenharContadorDeVida(vida);

        for (MeteoroObjeto meteoro : meteoros) {
            desenharMeteoro(meteoro);
        }

        desenharEntradaDoUsuario();

        spriteBatch.end();
    }

    /** Desenha o meteoro e, no centro dele, um bloco escuro com a equação. */
    private void desenharMeteoro(MeteoroObjeto meteoro) {
        Sprite sprite = meteoro.sprite;
        sprite.draw(spriteBatch);

        float centroX = sprite.getX() + sprite.getWidth() / 2f;
        float centroY = sprite.getY() + sprite.getHeight() / 2f;

        desenharTextoComBloco(meteoro.equacao, centroX, centroY);
    }

    private void desenharEntradaDoUsuario() {
        // Canto inferior esquerdo; usa o mesmo estilo de bloco para ficar legível
        String texto = ": " + entradaUsuario;
        layout.setText(font, texto);
        float centroX = 1f + (layout.width / 2f) + BLOCO_PADDING;
        float centroY = 1f;
        desenharTextoComBloco(texto, centroX, centroY);
    }

    /** Desenha um texto centralizado em (centroX, centroY) com um bloco escuro atrás dele. */
    private void desenharTextoComBloco(String texto, float centroX, float centroY) {
        layout.setText(font, texto);

        float blocoLargura = layout.width + BLOCO_PADDING * 2;
        float blocoAltura = layout.height + BLOCO_PADDING * 2;
        float blocoX = centroX - blocoLargura / 2f;
        float blocoY = centroY - blocoAltura / 2f;

        // Bloco de fundo
        spriteBatch.setColor(BLOCO_COR);
        spriteBatch.draw(pixelBranco, blocoX, blocoY, blocoLargura, blocoAltura);
        spriteBatch.setColor(Color.WHITE); // volta ao normal para não tingir os próximos desenhos

        // Texto (o BitmapFont desenha a partir do topo do texto)
        float textoX = centroX - layout.width / 2f;
        float textoY = centroY + layout.height / 2f;
        font.draw(spriteBatch, layout, textoX, textoY);
    }

    private void desenharContadorDeVida(int vida){
        layout.setText(font, "Vida: " + String.valueOf(vida));

        float blocoLargura = layout.width + BLOCO_PADDING * 2;
        float blocoAltura = layout.height + BLOCO_PADDING * 2;
        float blocoX = 0;
        float blocoY = 14;

        spriteBatch.setColor(BLOCO_COR);
        spriteBatch.draw(pixelBranco, blocoX, blocoY, blocoLargura, blocoAltura);
        spriteBatch.setColor(Color.WHITE);

        float textoX = 0;
        float textoY = 15;
        font.draw(spriteBatch, layout, textoX, textoY);
    }
}