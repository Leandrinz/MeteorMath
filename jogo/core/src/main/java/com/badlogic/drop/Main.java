package com.badlogic.drop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener, InputProcessor {
    Texture backgroundTexture;
    Texture fogueteTexture;
    Texture meteoroTexture;

    SpriteBatch spriteBatch;
    Sprite fogueteSprite;

    public static class MeteoroObjeto{
            public Sprite sprite;
            public String equacao;
            public int respostaCorreta;

            public MeteoroObjeto(Sprite sprite, String equacao, int respostaCorreta){
                this.sprite = sprite;
                this.equacao = equacao;
                this.respostaCorreta = respostaCorreta;
            }
    }

    Array<MeteoroObjeto> meteoroObjetos;

    FitViewport viewport;

    Rectangle fogueteRectangle;
    Rectangle meteoroRectangle;

    float meteoroTimer;
    BitmapFont font;

    StringBuilder entradaUsuario = new StringBuilder();

    @Override
    public void create() {
        backgroundTexture = new Texture("background.png");
        fogueteTexture = new Texture("foguete.png");
        meteoroTexture = new Texture("meteoro.png");

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(25, 15);

        fogueteSprite = new Sprite(fogueteTexture);
        fogueteSprite.setSize(4,5);
        meteoroObjetos = new Array<MeteoroObjeto>();
        fogueteRectangle = new Rectangle();
        meteoroRectangle = new Rectangle();

        // Iniciar fonte padrão pra equação
        font = new BitmapFont();

        font.getData().setScale(0.08f);
        font.setColor(Color.WHITE);

        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        input();
        logic();
        draw();
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    public void input() {

    }

    public void logic() {
        float larguraMundo = viewport.getWorldWidth();
        float alturaMundo = viewport.getWorldHeight();

        float fogueteLargura = fogueteSprite.getWidth();
        float fogueteAltura = fogueteSprite.getHeight();

        float delta = Gdx.graphics.getDeltaTime();

        // Geração dos meteoros
        for (int i = meteoroObjetos.size - 1; i >= 0; i--) {
            MeteoroObjeto meteoro = meteoroObjetos.get(i);
            Sprite meteoroSprite = meteoro.sprite;

            float meteoroLargura = meteoroSprite.getWidth();
            float meteoroAltura = meteoroSprite.getHeight();

            meteoroSprite.translateY((-0.5f * delta));
            meteoroRectangle.set(meteoroSprite.getX(), meteoroSprite.getY(), meteoroLargura, meteoroAltura);

            // Remove o meteoro após sair da tela (e futuramente dar dano)
            if (meteoroSprite.getY() < -meteoroAltura) meteoroObjetos.removeIndex(i);
        }
        meteoroTimer += delta;
        if (meteoroTimer >= 2.4f){
            meteoroTimer = 0;
            createMeteoro();
        }
    }

    public void draw() {
        viewport.apply();
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        spriteBatch.begin();

        float larguraMundo = viewport.getWorldWidth();
        float alturaMundo = viewport.getWorldHeight();

        spriteBatch.draw(backgroundTexture, 0, 0, larguraMundo, alturaMundo);
        fogueteSprite.setPosition((viewport.getWorldWidth() - fogueteSprite.getWidth()) / 2, 0);
        fogueteSprite.draw(spriteBatch);

        for (MeteoroObjeto meteoro : meteoroObjetos) {
            meteoro.sprite.draw(spriteBatch);

            float textoX = meteoro.sprite.getX() + (meteoro.sprite.getWidth() / 4f);
            float textoY = meteoro.sprite.getY() + (meteoro.sprite.getHeight() / 2f);

            font.draw(spriteBatch, meteoro.equacao, textoX, textoY);
        }

        font.draw(spriteBatch, ": " + entradaUsuario.toString(), 1f, 1f);

        spriteBatch.end();
    }

    private void createMeteoro() {
        float meteoroLargura = 4;
        float meteoroAltura = 4;
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();



        Sprite meteoroSprite = new Sprite(meteoroTexture);
        meteoroSprite.setSize(meteoroLargura, meteoroAltura);
        meteoroSprite.setX(MathUtils.random(0f, worldWidth - meteoroLargura));
        meteoroSprite.setY(worldHeight);

        // Gera a equação

        int a = MathUtils.random(1, 9);
        int b = MathUtils.random(1, 9);
        String equacao = a + "+" + b;
        int respostaCorreta = a + b;

        meteoroObjetos.add(new MeteoroObjeto(meteoroSprite, equacao, respostaCorreta));

    }

    // --- MÉTODOS DO INPUT ---

    @Override
    public boolean keyTyped(char character) {
        // Valida a resposta ao apertar enter
        if (character == '\n' || character == '\r') {
            if (entradaUsuario.length() > 0){
                try {
                    int respostaDigitada = Integer.parseInt(entradaUsuario.toString().trim());

                    // Procura um meteoro na tela com essa resposta
                    for (int i = 0; i < meteoroObjetos.size; i++){
                        if (meteoroObjetos.get(i).respostaCorreta == respostaDigitada){
                            meteoroObjetos.removeIndex(i);
                            break;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignora caso digitem algo inválido
                }
                entradaUsuario.setLength(0);
            }
        }
        else if (character == '\b'){
            if (entradaUsuario.length() > 0) {
                entradaUsuario.deleteCharAt(entradaUsuario.length() - 1);
            }
        }
        // Se for um número, adiciona à string de entrada
        else if (Character.isDigit(character)){
            entradaUsuario.append(character);
        }
        return true;
    }

    @Override public boolean keyDown(int keycode) {return false;}
    @Override public boolean keyUp(int keycode) { return false; }
    @Override public boolean touchDown(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }

    public void resume() {

    }

    @Override
    public void dispose() {
        backgroundTexture.dispose();
        fogueteTexture.dispose();
        meteoroTexture.dispose();
        spriteBatch.dispose();
        font.dispose();
    }
}
