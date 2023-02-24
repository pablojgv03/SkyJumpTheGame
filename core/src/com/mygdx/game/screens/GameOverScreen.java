package com.mygdx.game.screens;

import static com.mygdx.game.screens.GameScreen.scoreNumber;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.MainGame;
//GAME OVER SCREEN
public class GameOverScreen  extends BaseScreen{
    //Variables Declaration and Initialization
    private SpriteBatch batch;
    private Texture gameOverTexture;
    private boolean touched;
    private int height, width;
    private BitmapFont score;

    /**
     * Constructor
     * @param mainGame
     */
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
        //Draw his score at the 90% of height and 1 world unity from the left frame
        score.draw(this.batch, "SCORE: " +  scoreNumber, 1f, height*0.9f);
        batch.end();
        //when touch send to get ready screen
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
