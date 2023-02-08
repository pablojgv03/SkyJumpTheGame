package com.mygdx.game.actors;



import static com.mygdx.game.extra.Utils.JUMP_SPEED;
import static com.mygdx.game.extra.Utils.USER_SLIME;
import static com.mygdx.game.extra.Utils.WORLD_WIDTH;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class Slime extends Actor {
    //Variables Declaration and Initialization
    //Static final variables
    private static final float SLIME_WIDTH = 0.7f;
    private static final float SLIME_HEIGHT = 0.7f;
    public static final int STATE_ALIVE = 0;
    public static final int STATE_DEAD = 1;

    //State of the slime
    private int state;

    //position
    private Vector2 position;

    //Texture
    TextureRegion bodyTr;

    //Body
    private Body body;

    //Give a form to the body
    private Fixture fixture;

    //Horizontal speed
    public float xSpeed;

    //Save the total time the body has been drawn
    private float stateTime;

    //The world class manages all physics entities, dynamic simulation, and asynchronous queries.
    private World world;


    /**
     * Constructor
     * @param world
     * @param position
     * @param bodyTr
     */
    public Slime(World world, Vector2 position, TextureRegion bodyTr){
        //initialice the position with the value passed by parameter
        this.position = position;
        //same with the world
        this.world = world;
        //and with the textureRegion
        this.bodyTr = bodyTr;
        //Set a state to the slime
        this.state = STATE_ALIVE;
        //Create body
        createBody();
        //create the "shape" of the body
        createFixture();
    }

    //Create the body
    private void createBody(){
        BodyDef bodyDef = new BodyDef();
        //Establish the position
        bodyDef.position.set(this.position);
        //Set the type of body to dynamic
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        //Create the body on our world
        this.body = this.world.createBody(bodyDef);
    }

    //Create the body's shape or "hitbox"
    private void createFixture(){
        //Create a rectangular shape
        PolygonShape shape = new PolygonShape();
        //Set the hitbox box' size
        shape.setAsBox(SLIME_WIDTH/2, SLIME_HEIGHT/2);
        //Create the body's shape with the previous rectangular shape
        this.fixture = this.body.createFixture(shape, 8);
        //And set it to the body
        this.fixture.setUserData(USER_SLIME);
        //Dispose the shape
        shape.dispose();
    }

    public int getState(){
        return this.state;
    }

    public void kill() {
        state = STATE_DEAD;
        stateTime = 0f;
    }

    @Override
    public void act(float delta) {
        //jump action
        boolean jump  = Gdx.input.justTouched();
        if(jump){
            move(0,JUMP_SPEED);
        }
    }
    public void move(float x, float y){
        this.body.setLinearVelocity(x,y);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        //se le establece la posicion
        setPosition(body.getPosition().x-(SLIME_WIDTH/2), body.getPosition().y-(SLIME_HEIGHT/2));
        batch.draw(this.bodyTr, getX(),getY(), SLIME_WIDTH, SLIME_HEIGHT);

        //delta es el lapso de tiempo entre el frame actual y el último frame (en segundos).
    }

    //para eliminar
    public void detach(){
        //destruye la forma
        this.body.destroyFixture(this.fixture);
        //y el cuerpo
        this.world.destroyBody(this.body);
    }

    public Vector2 getLinearVelocity(){
    return this.body.getLinearVelocity();
    }



    public void derecha(){
        this.body.setTransform(new Vector2(WORLD_WIDTH,this.body.getPosition().y), 0);
    }
    public void izquierda(){
        this.body.setTransform(new Vector2(0,this.body.getPosition().y), 0);
    }
}














