package com.mygdx.game.screens;


import static com.mygdx.game.actors.Platform.FALLSPEED;
import static com.mygdx.game.actors.Platform.PLATFSPEEDY;
import static com.mygdx.game.extra.Utils.CAMERA_HEIGHT;
import static com.mygdx.game.extra.Utils.CAMERA_WIDTH;
import static com.mygdx.game.extra.Utils.JUMP_SPEED;
import static com.mygdx.game.extra.Utils.PLATFORM_WIDTH;
import static com.mygdx.game.extra.Utils.SCREEN_HEIGHT;
import static com.mygdx.game.extra.Utils.SCREEN_WIDTH;
import static com.mygdx.game.extra.Utils.USER_FLOOR;
import static com.mygdx.game.extra.Utils.USER_PLATFORM;
import static com.mygdx.game.extra.Utils.USER_SLIME;
import static com.mygdx.game.extra.Utils.WORLD_HEIGHT;
import static com.mygdx.game.extra.Utils.WORLD_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.mygdx.game.MainGame;
import com.mygdx.game.actors.Platform;
import com.mygdx.game.actors.Slime;

public class GameScreen  extends BaseScreen implements ContactListener {
    //Linear Interpolation (to make camera movements more fluid)
    private static final float lerp = 0.08f;

    //those variables are for the control of platform spawn
    private  static final float PLATFORM_SPAWN_TIME = 2.25f;
    private float platformSpawnTime;
    private float lastCreatedTime;

    private Stage stage;

    //Actor
    private Slime slime;

    //Platform
    private Platform platform;

    //Floor
    private Fixture fixFloor;
    private Body bodyFloor;

    //background image
    private Image background;

    private World world;

    //Score variable I'll use it in GameOverScreen too
    public static int scoreNumber;

    //Platforms Array
    private Array<Platform> arrayPlatforms;

    //SOUND AND MUSIC
    private Music backgroundM;
    private Sound jumpS, killS;

    private Box2DDebugRenderer debugRenderer;

    //need two cameras one for the world and another one in order to show the score
    private OrthographicCamera worldOrtCamera;
    private OrthographicCamera fontOrtCamera;
    private BitmapFont score;


    public GameScreen(MainGame mainGame) {
        super(mainGame);

        //initialize the world
        this.world = new World(new Vector2(0,-14), true);

        this.world.setContactListener(this);
        //the scoreNumber always starts at 0
        scoreNumber = 0;

        FitViewport fitViewport = new FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT);

        this.stage = new Stage(fitViewport);

        //Music & sounds
        this.backgroundM = this.mainGame.assetManager.getBacgroundM();
        this.jumpS = this.mainGame.assetManager.getJumpS();
        this.killS = this.mainGame.assetManager.getKillS();

        //Array where I save the created platforms
        this.arrayPlatforms = new Array();
        this.platformSpawnTime = 0f;
        this.lastCreatedTime = 0f;

        //initialize the orthographic camera
        this.worldOrtCamera = (OrthographicCamera) this.stage.getCamera();

        //and initialize the render
        this.debugRenderer = new Box2DDebugRenderer();

        prepareScore();
    }

    /**
     * add or update the background
     */
    public void addBackground(){
        this.background = new Image(mainGame.assetManager.getBackground());

        this.background.setPosition(0,0);

        this.background.setSize(WORLD_WIDTH,WORLD_HEIGHT);

        this.stage.addActor(this.background);
    }

    public void addActor(){
        //Get the texture Region of the actor and save it on slimeTr
        TextureRegion slimeTr = mainGame.assetManager.getSlimeTR();
        this.slime = new Slime(this.world, new Vector2(2.4f,6f), slimeTr);

        //add the acto to the stage
        this.stage.addActor(this.slime);
    }

    /**
     * Prepare the score into the orthographic camera and scale a 50% because was too big
     */
    private void prepareScore(){
        this.scoreNumber = 0;
        this.score = this.mainGame.assetManager.getFont();
        this.score.getData().scale(0.5f);

        this.fontOrtCamera = new OrthographicCamera();
        this.fontOrtCamera.setToOrtho(false, SCREEN_WIDTH,SCREEN_HEIGHT);
        this.fontOrtCamera.update();
    }

    /**
     * Add some initial platforms while we wait the other go down enough
     */
    public void addInitPlarforms(){
        //here i use an animation for the platfoms
        Animation<TextureRegion> platfSprite = mainGame.assetManager.getPlatformAnimation();
        int posicion = 4;
        //each platform is 5 world units upper than the others
        for(int i =0; i<3; i++) {
            this.platform = new Platform(this.world, platfSprite, new Vector2(2.4f, posicion));
            arrayPlatforms.add(this.platform);
            this.stage.addActor(this.platform);
            posicion += 5;
        }
    }

    /**
     * add the floor, which will be the object that kill our actor
     * doesn't have texture due to the fact that it will be invisible
     */
    private void addFloor() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(WORLD_WIDTH / 2f, 1.2f);
        bodyDef.type = BodyDef.BodyType.StaticBody;
        this.bodyFloor = world.createBody(bodyDef);

        PolygonShape edge = new PolygonShape();
        edge.setAsBox(WORLD_WIDTH / 2, 0f);
        this.fixFloor = this.bodyFloor.createFixture(edge, 3);
        this.fixFloor.setUserData(USER_FLOOR);
        edge.dispose();
    }

    /**
     * this metod allow the actor teleport from one part of the screen to the opposite site
     */
    private void endScreenTeleport() {
        // Verifies if it has gone out form the Left site
        if (slime.getX() < -0.35) {
            //set the position at the same height but at the right frame
            slime.right();
        }
        // Verifies if it has gone out form the right site
        if (slime.getX() > WORLD_WIDTH-0.35) {
            //set the position at the same height but at the left frame
            slime.left();
        }
    }

    /**
     * get the gyroscope movement and move the actor depending of gyroscope movement
     */
    private void gyroMovement(){
        if(gyroscopeAvail){
            float gyroX = Gdx.input.getAccelerometerX();
            slime.move(-gyroX, slime.getLinearVelocity().y);
        }
    }


    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        //add the platforms
        addPlatform(delta);

        this.stage.act();

        //set the actor x movements depending of the gyroscope values
        gyroMovement();
        //the camera position actualize in order to follow our actor that will make a bigger jump feeling
        worldOrtCamera.position.set(WORLD_WIDTH/2, (worldOrtCamera.position.y + (slime.getY() - worldOrtCamera.position.y)*lerp), 0);

        endScreenTeleport();

        this.world.step(delta,6,2);

        //draw the stage
        this.stage.draw();

        //if we want to show the physics frames only uncomment the line below
        //this.debugRenderer.render(this.world, this.worldOrtCamera.combined);

        //check the platform remove
        removePlatform();
        //update the score
        updateScore();
    }

    /**
     * refresh the score
     */
    public void updateScore(){
        this.stage.getBatch().setProjectionMatrix(this.fontOrtCamera.combined);
        this.stage.getBatch().begin();
        this.score.draw(this.stage.getBatch(), "SCORE: "+scoreNumber,SCREEN_WIDTH*0.1f, SCREEN_HEIGHT*0.9f);
        this.stage.getBatch().end();
    }

    /**
     * add the necessary elements and starts the background music
     */
    @Override
    public void show() {
        addBackground();
        addActor();
        addFloor();
        addInitPlarforms();

        this.backgroundM.setLooping(true);
        this.backgroundM.play();
    }


    public void addPlatform(float delta){
        Animation<TextureRegion> platfSprite = mainGame.assetManager.getPlatformAnimation();

        if(slime.getState() == slime.STATE_ALIVE) {
            this.lastCreatedTime+=delta;
            this.platformSpawnTime+=delta;
            //Todo 4. Si el tiempo acumulado es mayor que el tiempo que hemos establecido, se crea una tubería...
            if(lastCreatedTime>1.40f){
                enougth();
                if(this.platformSpawnTime >= PLATFORM_SPAWN_TIME) {

                    //Todo 4.1 ... y le restamos el tiempo a la variable acumulada para que vuelva el contador a 0.
                    this.platformSpawnTime-=PLATFORM_SPAWN_TIME;
                    float posRandomX = MathUtils.random((PLATFORM_WIDTH/2), WORLD_WIDTH-(PLATFORM_WIDTH/2));
                    //Cambiamos la coordenada x para que se cree fuera de la pantalla (5f)
                    this.platform = new Platform(this.world, platfSprite, new Vector2(posRandomX, 15f));
                    arrayPlatforms.add(this.platform);
                    this.stage.addActor(this.platform);
                    lastCreatedTime=0;
                }
            }
        }
    }
    public void removePlatform(){
        for (Platform platf : this.arrayPlatforms) {
            if(!world.isLocked()) {
                if(platf.isOutOfScreen()) {
                    platf.detach();
                    platf.remove();
                    arrayPlatforms.removeValue(platf,false);
                }
            }
        }
    }

    @Override
    public void hide() {
        this.slime.detach();
        this.slime.remove();
    }


    @Override
    public void dispose() {
        this.stage.dispose();
        this.world.dispose();
    }

    boolean gyroscopeAvail = Gdx.input.isPeripheralAvailable(Input.Peripheral.Gyroscope);


    //////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////
    private boolean areColider(Contact contact, Object objA, Object objB) {
        return (contact.getFixtureA().getUserData().equals(objA) && contact.getFixtureB().getUserData().equals(objB)) ||
                (contact.getFixtureA().getUserData().equals(objB) && contact.getFixtureB().getUserData().equals(objA));
    }

    @Override
    public void beginContact(Contact contact) {

        if (areColider(contact, USER_SLIME, USER_PLATFORM)) {
            if(slime.getLinearVelocity().y < 0) {
                slime.move(0, JUMP_SPEED);
                this.jumpS.play();
                platfGoDown();
                platformSpawnTime += 4;
                this.scoreNumber++;
            }
        } else {
            slime.kill();
            this.backgroundM.stop();
            for(Platform platf: arrayPlatforms){
                platf.stopPlatform();
            }
            killS.play();
            mainGame.setScreen(new GameOverScreen(mainGame));
        }
    }


    public void enougth(){
        //Han bajado lo suficiente entonces vuelvo a establecer su velocidad al valor por defecto
        for (Platform platform1 : this.arrayPlatforms){
            platform1.goDown(PLATFSPEEDY);
        }
    }
    public void platfGoDown(){
        for (Platform platform1 : this.arrayPlatforms ){
            platform1.goDown(FALLSPEED);
        }
    }

    @Override
    public void endContact(Contact contact) {

    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {

    }
}





