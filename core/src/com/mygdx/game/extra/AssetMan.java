package com.mygdx.game.extra;

import static com.mygdx.game.extra.Utils.ATLAS_MAP;
import static com.mygdx.game.extra.Utils.BACKGROUND_IMAGE;
import static com.mygdx.game.extra.Utils.BACKGROUND_M;
import static com.mygdx.game.extra.Utils.FONT_FNT;
import static com.mygdx.game.extra.Utils.FONT_PNG;
import static com.mygdx.game.extra.Utils.GETREADY_M;
import static com.mygdx.game.extra.Utils.JUMP_S;
import static com.mygdx.game.extra.Utils.KILL_S;
import static com.mygdx.game.extra.Utils.PLATFORM1;
import static com.mygdx.game.extra.Utils.PLATFORM2;
import static com.mygdx.game.extra.Utils.PLATFORM3;
import static com.mygdx.game.extra.Utils.PLATFORM4;
import static com.mygdx.game.extra.Utils.PLATFORM5;
import static com.mygdx.game.extra.Utils.PLATFORM6;
import static com.mygdx.game.extra.Utils.PLATFORM7;
import static com.mygdx.game.extra.Utils.USER_SLIME;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;


public class AssetMan {

    private AssetManager assetManager;
    private TextureAtlas textureAtlas;

    /**
     * Constructor
     * - Loads the necessaries asset managers
     */
    public AssetMan(){
        this.assetManager = new AssetManager();
        //Loads images from texture atlases
        assetManager.load(ATLAS_MAP, TextureAtlas.class);
        assetManager.load(JUMP_S, Sound.class);
        assetManager.load(KILL_S, Sound.class);
        assetManager.load(BACKGROUND_M, Music.class);
        assetManager.load(GETREADY_M, Music.class);

        assetManager.finishLoading();
        //get the atlas map and save it in our textureAtlas
        this.textureAtlas = assetManager.get(ATLAS_MAP);
    }
    /**
     * Get the Background's Atlas TextureRegion
     * @return TextureRegion
     * - A region of the Atlas
     */
    public TextureRegion getBackground(){
        return this.textureAtlas.findRegion(BACKGROUND_IMAGE);
    }

    /**
     * Get the Slime's Atlas TextureRegion
     * @return TextureRegion
     * - A region of the Atlas
     */
    public TextureRegion getSlimeTR(){
        return this.textureAtlas.findRegion(USER_SLIME);
    }


    /**
     * Return the platform's animation
     * @return Animation
     * - An Animation<TextureRegion> Composed by a frame duration and a group
     * of regions in the textureAtlas
     */
    public Animation<TextureRegion> getPlatformAnimation(){
        return new Animation<TextureRegion>(0.143f,
                textureAtlas.findRegion(PLATFORM1),
                textureAtlas.findRegion(PLATFORM2),
                textureAtlas.findRegion(PLATFORM3),
                textureAtlas.findRegion(PLATFORM4),
                textureAtlas.findRegion(PLATFORM5),
                textureAtlas.findRegion(PLATFORM6),
                textureAtlas.findRegion(PLATFORM7));
    }

    //SOUNDS

    /**
     * Get the jump sound
     * @return Sound
     * - A sound from the assetManager
     */
    public Sound getJumpS(){
        return this.assetManager.get(JUMP_S);
    }

    /**
     * Get the kill sound
     * @return Sound
     * - A sound from the assetManager
     */
    public Sound getKillS(){
        return this.assetManager.get(KILL_S);
    }

    /**
     * Get the background music
     * @return Sound
     * - A music from the assetManager
     */
    public Music getBacgroundM(){
        return this.assetManager.get(BACKGROUND_M);
    }

    /**
     * Get the getReadyScreen music
     * @return Sound
     * - A music from the assetManager
     */
    public Music getGetReadyM(){
        return this.assetManager.get(GETREADY_M);
    }

    /**
     * Creates a BitmapFont from a BMFont file and return it
     * @return
     */
    public BitmapFont getFont(){
        return new BitmapFont(Gdx.files.internal(FONT_FNT),Gdx.files.internal(FONT_PNG), false);
    }

}














