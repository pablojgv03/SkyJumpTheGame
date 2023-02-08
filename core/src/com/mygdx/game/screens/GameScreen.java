package com.mygdx.game.screens;


import static com.mygdx.game.extra.Utils.JUMP_SPEED;
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
import com.badlogic.gdx.math.Matrix4;
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

import java.math.BigDecimal;
import java.math.RoundingMode;

public class GameScreen  extends BaseScreen implements ContactListener {
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
        FitViewport fitViewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);
        //Se le pone al stage
        this.stage = new Stage(fitViewport);
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

    public void addPlatform(){
        Animation<TextureRegion> platfSprite = mainGame.assetManager.getPlatformAnimation();
        this.platform = new Platform(this.world, platfSprite, new Vector2(1f, 4f));
        this.stage.addActor(this.platform);
    }

    private void gyroMovement() {
        System.out.println("x" + slime.getX()+"        ");
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
    @Override
    public void render(float delta) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        this.stage.act();
        platformMov();
        if(gyroscopeAvail){
            float gyroX = Gdx.input.getAccelerometerX();
            slime.move(-gyroX, slime.getLinearVelocity().y);
        }
        ortCamera.position.set(WORLD_WIDTH/2, slime.getY() + slime.getHeight() / 2, 0);
        gyroMovement();
        //Esto realiza principalmente la detección de colisiones
        this.world.step(delta,6,2);
        //dibuja la escena
        this.stage.draw();
        //Establece la matriz de proyección.
        this.debugRenderer.render(this.world, this.ortCamera.combined);

    }



    public void platformMov(){

    }


    @Override
    public void show() {
        addBackground();
        addActor();
        addFloor();
        //addCeiling();
        addPlatform();
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
        } else {
            System.out.println("hola");
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





