package com.mygdx.game.extra;

import static com.mygdx.game.extra.Utils.ATLAS_MAP;
import static com.mygdx.game.extra.Utils.BACKGROUND_IMAGE;
import static com.mygdx.game.extra.Utils.BACKGROUND_M;
import static com.mygdx.game.extra.Utils.JUMP_S;
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

//Como su nombre indica es el manager de los assets
public class AssetMan {

    private AssetManager assetManager;
    //carga las imagenes del atlas
    private TextureAtlas textureAtlas;

    public AssetMan(){
        this.assetManager = new AssetManager();
        //carga el atlas
        assetManager.load(ATLAS_MAP, TextureAtlas.class);
        //Bloquea hasta que todos los assets estan cargados
        assetManager.load(JUMP_S, Sound.class);
        assetManager.load(BACKGROUND_M, Music.class);


        assetManager.finishLoading();
        //Se le pasa el mapa de atlas procesado por el assetManager y se guarda en el textureAtlas
        this.textureAtlas = assetManager.get(ATLAS_MAP);



    }
    //BACKGROUND IMAGE
    public TextureRegion getBackground(){
        return this.textureAtlas.findRegion(BACKGROUND_IMAGE);
    }
    //SLIME IMAGE
    public TextureRegion getSlimeTR(){
        return this.textureAtlas.findRegion(USER_SLIME);
    }
    //PLATFORM ANIMATION
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
    public Sound getJumpS(){
        return this.assetManager.get(JUMP_S);
    }

    public Music getBacgroundM(){
        return this.assetManager.get(BACKGROUND_M);
    }

    /*
    public BitmapFont getFont(){
        return new BitmapFont(Gdx.files.internal(FONT_FNT),Gdx.files.internal(FONT_PNG), false);
    }*/

}














