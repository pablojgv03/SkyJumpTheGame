package com.mygdx.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.MainGame;


public class GetReadyScreen extends BaseScreen {
    //Variables Declaration and Initialization
    private SpriteBatch batch;
    private Texture getReadyTexture;
    private boolean touched;
    private int height, width;
    private float transitionTime = 0;
    private final float transitionDuration = 1f;
    private final float fade1Time = transitionDuration * 0.25f; //25%
    private final float fade2Time = transitionDuration * 0.5f;  //50%
    private final float fade3Time = transitionDuration * 0.75f; //75%
    private Music getReadyM;

    /**
     * Get Ready Screen Constructor
     */
    public GetReadyScreen(MainGame mainGame) {
        super(mainGame);
        this.height = Gdx.graphics.getHeight();
        this.width = Gdx.graphics.getWidth();
        this.getReadyM = this.mainGame.assetManager.getGetReadyM();

    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        getReadyTexture = new Texture("ready.png");
        touched = false;
        this.getReadyM.setLooping(true);
        this.getReadyM.play();
        //Establish the music volume to 100%
        this.getReadyM.setVolume(1);
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
        //if it was touched i start the fade process getting the image darker
        //and in addition setting the music volume a 25% lower until it reaches 0%
        //that make the transition less sudden
        if(touched) {
            transitionTime += delta;
            if (transitionTime >= fade1Time){
                getReadyTexture = new Texture("ready(0).png");
                this.getReadyM.setVolume(0.75f);
            }
            if (transitionTime >= fade2Time){
                getReadyTexture = new Texture("ready(1).png");
                this.getReadyM.setVolume(0.5f);
            }
            if (transitionTime >= fade3Time){
                getReadyTexture = new Texture("ready(2).png");
                this.getReadyM.setVolume(0.25f);
            }
            if (transitionTime >= transitionDuration) {
                mainGame.setScreen(new GameScreen(mainGame));
                this.getReadyM.stop();
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        getReadyTexture.dispose();
    }
}
