package com.mygdx.game.screens;

import static com.mygdx.game.screens.GameScreen.scoreNumber;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.MainGame;

public class GameOverScreen  extends BaseScreen{
    private SpriteBatch batch;
    private Texture gameOverTexture;
    private boolean touched;
    private int height, width;
    private BitmapFont score;

    public GameOverScreen(MainGame mainGame) {
        super(mainGame);
        this.height = Gdx.graphics.getHeight();
        this.width = Gdx.graphics.getWidth();
        this.score = this.mainGame.assetManager.getFont();
        this.score.getData().scale(2f);
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        gameOverTexture = new Texture("gameOver.jpg");
        touched = false;
    }

    @Override
    public void render(float delta) {
        super.render(delta);

        batch.begin();
        batch.draw(gameOverTexture,0,0, width, height);
        score.draw(this.batch, "" +  scoreNumber, width/2, height*0.9f);
        batch.end();
        if (Gdx.input.justTouched() && !touched) {
            touched = true;
            mainGame.setScreen(new GetReadyScreen(mainGame));
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        gameOverTexture.dispose();
    }
}
