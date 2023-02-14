package com.mygdx.game.screens;


import static com.mygdx.game.actors.Platform.FALLSPEED;
import static com.mygdx.game.actors.Platform.PLATFSPEEDY;
import static com.mygdx.game.extra.Utils.CAMERA_HEIGHT;
import static com.mygdx.game.extra.Utils.CAMERA_WIDTH;
import static com.mygdx.game.extra.Utils.JUMP_SPEED;
import static com.mygdx.game.extra.Utils.PLATFORM_WIDTH;
import static com.mygdx.game.extra.Utils.USER_FLOOR;
import static com.mygdx.game.extra.Utils.USER_PLATFORM;
import static com.mygdx.game.extra.Utils.USER_SLIME;
import static com.mygdx.game.extra.Utils.WORLD_HEIGHT;
import static com.mygdx.game.extra.Utils.WORLD_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
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
    private  static final float PLATFORM_SPAWN_TIME = 2.25f;

    private float platformSpawnTime;
    private float lastCreatedTime;
    //Escenario
    private Stage stage;
    //Actor
    private Slime slime;
    //Platform
    private Platform platform;
    //Fondo
    private Image background;

    //Mundo
    private World world;

    //Score
    private int scoreNumber;

    //Platforms Array
    private Array<Platform> arrayPlatforms;

    int screenWidth = Gdx.graphics.getWidth();
    int screenHeight = Gdx.graphics.getHeight();

    //Depuración
    private Box2DDebugRenderer debugRenderer;
    //camara ortografica
    private OrthographicCamera ortCamera;

    //Pantalla del juego
    public GameScreen(MainGame mainGame) {
        //constructor juego principal
        super(mainGame);

        //inicializacion del mundo
        this.world = new World(new Vector2(0,-14), true);

        //se le pasa el objeto que implementa la interfaz
        this.world.setContactListener(this);

        //siempre mantiene la relación de aspecto del tamaño de la pantalla virtual
        //(ventana virtual), al tiempo que la escala tanto como sea posible para
        //que se ajuste a la pantalla.
        FitViewport fitViewport = new FitViewport(CAMERA_WIDTH, CAMERA_HEIGHT);

        //Se le pone al stage
        this.stage = new Stage(fitViewport);

        //Inicializo el array
        this.arrayPlatforms = new Array();
        this.platformSpawnTime = 0f;
        this.lastCreatedTime = 0f;
        //Se inicializa la camara ortografica
        this.ortCamera = (OrthographicCamera) this.stage.getCamera();

        //Se inicializa el renderizado
        this.debugRenderer = new Box2DDebugRenderer();
    }



    //Para añadir o actualizar el fondo
    public void addBackground(){
        //Se le pone la imagen al fondo
        this.background = new Image(mainGame.assetManager.getBackground());
        //Se posiciona el fondo en la posicion x:0 y:0
        this.background.setPosition(0,0);
        //Se le pone al fondo el tamaño del mundo
        this.background.setSize(WORLD_WIDTH,WORLD_HEIGHT);
        //El escenario o escena añade al actor en el fondo
        this.stage.addActor(this.background);
    }

    public void addActor(){
        //Obtengo el la region de la textura de nuestro actor para pasarsela por parametro
        TextureRegion SlimeTr = mainGame.assetManager.getSlimeTR();
        this.slime = new Slime(this.world, new Vector2(1f,4f), SlimeTr);
        //añado el actor a la escena
        this.stage.addActor(this.slime);
    }

    private void addFloor() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.position.set(WORLD_WIDTH / 2f, 0f);
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bodyDef);
        body.setUserData(USER_FLOOR);

        PolygonShape edge = new PolygonShape();
        edge.setAsBox(WORLD_WIDTH / 2, 0f);
        body.createFixture(edge, 8);
        edge.dispose();
    }

    public void addCeiling(){
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bodyDef);

        EdgeShape edge = new EdgeShape();
        edge.set(0,WORLD_HEIGHT,WORLD_WIDTH,WORLD_HEIGHT);
        body.createFixture(edge, 1);
        edge.dispose();
    }

    private void endScreenTeleport() {
        if (slime.getX() < -0.35) {
            // Ajusta la posición para que aparezca por el lado derecho
            slime.derecha();
        }
            // Verifica si el actor ha salido de la pantalla por el lado derecho
        if (slime.getX() > WORLD_WIDTH-0.35) {
            // Ajusta la posición para que aparezca por el lado izquierdo
            slime.izquierda();
        }
    }

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
        ortCamera.position.set(WORLD_WIDTH/2, (ortCamera.position.y + (slime.getY() - ortCamera.position.y)*lerp), 0);
        endScreenTeleport();
        //Esto realiza principalmente la detección de colisiones
        this.world.step(delta,6,2);
        //dibuja la escena
        this.stage.draw();
        //Establece la matriz de proyección.
        this.debugRenderer.render(this.world, this.ortCamera.combined);
        removePlatform();
    }






    @Override
    public void show() {
        addBackground();
        addActor();
        addFloor();
        //addCeiling();
        //addPlatform();
    }

    /*public void addPlatform(){
           Animation<TextureRegion> platfSprite = mainGame.assetManager.getPlatformAnimation();
           this.platform = new Platform(this.world, platfSprite, new Vector2(1f, 4f));
           this.stage.addActor(this.platform);
       }*/
    public void addPlatform(float delta){
        Animation<TextureRegion> platfSprite = mainGame.assetManager.getPlatformAnimation();

        if(slime.getState() == slime.STATE_ALIVE) {
            this.lastCreatedTime+=delta;
            this.platformSpawnTime+=delta;
            //Todo 4. Si el tiempo acumulado es mayor que el tiempo que hemos establecido, se crea una tubería...
            if(lastCreatedTime>1.399f){
                enougth();
                if(this.platformSpawnTime >= PLATFORM_SPAWN_TIME) {

                    //Todo 4.1 ... y le restamos el tiempo a la variable acumulada para que vuelva el contador a 0.
                    this.platformSpawnTime-=PLATFORM_SPAWN_TIME;
                    float posRandomX = MathUtils.random((PLATFORM_WIDTH/2), WORLD_WIDTH-(PLATFORM_WIDTH/2));
                    //Cambiamos la coordenada x para que se cree fuera de la pantalla (5f)
                    this.platform = new Platform(this.world, platfSprite, new Vector2((PLATFORM_WIDTH/2), 12.2f));
                    arrayPlatforms.add(this.platform);
                    this.stage.addActor(this.platform);
                    lastCreatedTime=0;
                }
            }
        }
    }
    public void removePlatform(){
        for (Platform pipe : this.arrayPlatforms) {
            //Todo 6.1 Si el mundo no está bloqueado, es decir, que no esté actualizando la física en ese preciso momento...
            if(!world.isLocked()) {
                //Todo 6.2...y la tubería en cuestión está fuera de la pantalla.
                if(pipe.isOutOfScreen()) {
                    //Todo 6.3 Eliminamos los recursos
                    pipe.detach();
                    //Todo 6.4 La eliminamos del escenario
                    pipe.remove();

                    //Todo 6.5 La eliminamos del array
                    arrayPlatforms.removeValue(pipe,false);
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
        //Todo 7. Si 'han colisionado' el pájaro con el contador sumamos 1 al contador...
        System.out.println(contact.getFixtureA().getUserData());
        if (areColider(contact, USER_SLIME, USER_PLATFORM)&&slime.getLinearVelocity().y < 0) {
            this.scoreNumber++;
            slime.move(0,JUMP_SPEED);
            platfGoDown();
            platformSpawnTime +=4;
        } else {
            System.out.println("hola");
        }
    }

    public void enougth(){
        //Han bajado lo suficiente entonces vuelvo a establecer su velocidad al valor por defecto
        for (Platform platform1 : this.arrayPlatforms ){
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





