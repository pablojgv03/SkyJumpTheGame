package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.mygdx.game.extra.AssetMan;
import com.mygdx.game.screens.GameOverScreen;
import com.mygdx.game.screens.GameScreen;
import com.mygdx.game.screens.GetReadyScreen;

public class MainGame extends Game {

	private GameScreen gameScreen;
	public GameOverScreen gameOverScreen;
	public GetReadyScreen getReadyScreen;

	public AssetMan assetManager;

	//SE CREA LA PANTALLA DEL JUEGO Y SE LE ESTABLECE
	@Override
	public void create() {

		this.assetManager = new AssetMan();
		//Initialize the game screens
		this. getReadyScreen = new GetReadyScreen(this);
		this. gameScreen = new GameScreen(this);
		//Se establece la pantalla
		setScreen(this.getReadyScreen);
	}
}

