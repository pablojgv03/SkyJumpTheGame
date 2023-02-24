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
    //Linear Interpolation (in order to make camera movements more fluid)
    private static final float lerp = 0.08f;

    //Those variables are for the control of platform spawn
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

    //Background image
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

    //Need two cameras one for the world and another one in order to show the score
    private OrthographicCamera worldOrtCamera;
    private OrthographicCamera fontOrtCamera;
    private BitmapFont score;

    /**
     * The game screen constructor
     * @param mainGame
     */
    public GameScreen(MainGame mainGame) {
        super(mainGame);

        //Initialize the world
        this.world = new World(new Vector2(0,-14), true);

        this.world.setContactListener(this);
        //The scoreNumber always starts at 0
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

        //Initialize the orthographic camera
        this.worldOrtCamera = (OrthographicCamera) this.stage.getCamera();

        //Initialize the render
        this.debugRenderer = new Box2DDebugRenderer();

        prepareScore();
    }

    /**
     * Add or update the background
     */
    public void addBackground(){
        this.background = new Image(mainGame.assetManager.getBackground());

        this.background.setPosition(0,0);

        this.background.setSize(WORLD_WIDTH,WORLD_HEIGHT);

        this.stage.addActor(this.background);
    }

    /**
     * Add our actor to the stage
     */
    public void addActor(){
        //Get the texture Region of the actor and save it on slimeTr
        TextureRegion slimeTr = mainGame.assetManager.getSlimeTR();
        this.slime = new Slime(this.world, new Vector2(2.4f,6f), slimeTr);

        //Add the actor to the stage
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
        //Here I use an animation for the platforms
        Animation<TextureRegion> platfSprite = mainGame.assetManager.getPlatformAnimation();
        int posicion = 4;
        //Each platform is 5 world units upper from the others
        for(int i =0; i<3; i++) {
            this.platform = new Platform(this.world, platfSprite, new Vector2(2.4f, posicion));
            arrayPlatforms.add(this.platform);
            this.stage.addActor(this.platform);
            posicion += 5;
        }
    }

    /**
     * Add the floor, which will be the object that kill our actor
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
     * When it detect that the actor is out of screen from one of the lateral frames
     * teleport the actor to the opposite site frame
     */
    private void endScreenTeleport() {
        // Verifies if it has gone out form the Left site
        if (slime.getX() < -0.35) {
            //Set the position at the same height but at the right frame
            slime.right();
        }
        // Verifies if it has gone out form the right site
        if (slime.getX() > WORLD_WIDTH-0.35) {
            //Set the position at the same height but at the left frame
            slime.left();
        }
    }

    /**
     * Get the gyroscope movement and call a method which set a 'x' axis
     * linear velocity to the actor depending of gyroscope values
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

        addPlatform(delta);

        this.stage.act();

        gyroMovement();

        //The camera position actualize in order to follow our actor that will make a bigger jump feeling
        worldOrtCamera.position.set(WORLD_WIDTH/2, (worldOrtCamera.position.y + (slime.getY() - worldOrtCamera.position.y)*lerp), 0);

        endScreenTeleport();

        this.world.step(delta,6,2);

        //Draw the stage
        this.stage.draw();

        removePlatform();

        updateScore();

        //If we want to show the physics frames only uncomment the line below...
        //this.debugRenderer.render(this.world, this.worldOrtCamera.combined);
    }

    /**
     * Refresh the score
     */
    public void updateScore(){
        this.stage.getBatch().setProjectionMatrix(this.fontOrtCamera.combined);
        this.stage.getBatch().begin();
        this.score.draw(this.stage.getBatch(), "SCORE: "+scoreNumber,SCREEN_WIDTH*0.1f, SCREEN_HEIGHT*0.9f);
        this.stage.getBatch().end();
    }

    /**
     * Add the necessary elements and starts the background music
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

    /**
     * This method add the platforms outside of our camera vision and add then to the platform's array
     * @param delta
     */
    public void addPlatform(float delta){
        Animation<TextureRegion> platfSprite = mainGame.assetManager.getPlatformAnimation();
        //Check that our actor still alive
        if(slime.getState() == slime.STATE_ALIVE) {
            this.lastCreatedTime+=delta;
            this.platformSpawnTime+=delta;
            //If since the last platform was created has elapsed more than 1.40 seconds then
            //stop the fast go down whit the enough() method which established the velocity
            //to default velocity
            if(lastCreatedTime>1.40f){
                enougth();
                //If the platform spawn time has been risen then spawn a new platform
                if(this.platformSpawnTime >= PLATFORM_SPAWN_TIME) {

                    this.platformSpawnTime-=PLATFORM_SPAWN_TIME;

                    //Spawn in a random x axis position
                    float posRandomX = MathUtils.random((PLATFORM_WIDTH/2), WORLD_WIDTH-(PLATFORM_WIDTH/2));

                    //Create the platform
                    this.platform = new Platform(this.world, platfSprite, new Vector2(posRandomX, 15f));

                    //Add the new platform to the array
                    arrayPlatforms.add(this.platform);

                    //Add the new platform to the stage
                    this.stage.addActor(this.platform);

                    //Set the created time to 0
                    lastCreatedTime=0;
                }
            }
        }
    }

    /**
     * Remove ta platform when it goes out of screen and the world isn't locked
     * this is done to free memory space
     */
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
    //get the gyroscope availability
    boolean gyroscopeAvail = Gdx.input.isPeripheralAvailable(Input.Peripheral.Gyroscope);

    /**
     * Detect if the parameters bodies are collider
     * @param contact
     * The detected contact
     * @param objA
     * Body A
     * @param objB
     * Body B
     * @return
     * True in case of collision, False otherwise
     */
    private boolean areCollider(Contact contact, Object objA, Object objB) {
        return (contact.getFixtureA().getUserData().equals(objA) && contact.getFixtureB().getUserData().equals(objB)) ||
                (contact.getFixtureA().getUserData().equals(objB) && contact.getFixtureB().getUserData().equals(objA));
    }

    @Override
    public void beginContact(Contact contact) {
        //Check if there is a collision between the bodies
        //Case slime collides with the platform increases the platform speed for a period of time
        if (areCollider(contact, USER_SLIME, USER_PLATFORM)) {
            if(slime.getLinearVelocity().y < 0) {
                slime.move(0, JUMP_SPEED);
                //Play the jump sound
                this.jumpS.play();

                //Increases the platform speed
                platfGoDown();

                platformSpawnTime += 4;

                //Score increases
                this.scoreNumber++;
            }
        }
        //if the collision isn't with a platform the smile die and the game finish
        else {
            //Kill the slime
            slime.kill();

            //Stop the background music
            this.backgroundM.stop();

            //Stop all the array platforms
            for(Platform platf: arrayPlatforms){
                platf.stopPlatform();
            }

            //Play the kill sound
            killS.play();

            //Shows the game over screen
            mainGame.setScreen(new GameOverScreen(mainGame));
        }
    }

    /**
     * Set all the array platforms Speed to the default Speed
     */
    public void enougth(){
        for (Platform platform1 : this.arrayPlatforms){
            platform1.goDown(PLATFSPEEDY);
        }
    }

    /**
     * Set all the array's platforms Speed to the fast fall Speed
     */
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





