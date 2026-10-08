package com.badlogic.drop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main implements ApplicationListener {
    Texture backgroundTexture;
    FitViewport viewport;
    SpriteBatch spriteBatch;
    Texture fogueteTexture;
    Sprite fogueteSprite;

    Rectangle fogueteRectangle;



    @Override
    public void create() {
        backgroundTexture = new Texture("background.png");
        fogueteTexture = new Texture("foguete.png");
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(25, 15);

        fogueteSprite = new Sprite(fogueteTexture);
        fogueteSprite.setSize(4,5);
        fogueteRectangle = new Rectangle();
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

        spriteBatch.end();
    }

    public void resume() {

    }

    @Override
    public void dispose() {
        // Destroy application's resources here.
    }
}
