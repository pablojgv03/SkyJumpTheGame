package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.MainGame;


public class GetReadyScreen extends BaseScreen {
    private SpriteBatch batch;
    private Texture getReadyTexture;
    private boolean touched;
    private int height, width;
    private float transitionTime = 0;
    private final float fade1Time = 0.25f;
    private final float fade2Time = 0.5f;
    private final float fade3Time = 0.75f;
    private final float transitionDuration = 1f;


    public GetReadyScreen(MainGame mainGame) {
        super(mainGame);
        this.height = Gdx.graphics.getHeight();
        this.width = Gdx.graphics.getWidth();
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        getReadyTexture = new Texture("ready.png");
        touched = false;
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        batch.begin();
        batch.draw(getReadyTexture,0,0, width, height);
        batch.end();
        if (Gdx.input.justTouched() && !touched) {
            touched = true;
        }
        if(touched) {
            transitionTime += delta;
            if (transitionTime >= fade1Time){
                getReadyTexture = new Texture("ready(0).png");
            }
            if (transitionTime >= fade2Time){
                getReadyTexture = new Texture("ready(1).png");
            }
            if (transitionTime >= fade3Time){
                getReadyTexture = new Texture("ready(2).png");
            }
            if (transitionTime >= transitionDuration) {
                mainGame.setScreen(new GameScreen(mainGame));
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        getReadyTexture.dispose();
    }
}
