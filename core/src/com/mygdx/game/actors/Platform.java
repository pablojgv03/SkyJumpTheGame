package com.mygdx.game.actors;

import static com.mygdx.game.extra.Utils.PLATFORM_HEIGHT;
import static com.mygdx.game.extra.Utils.PLATFORM_WIDTH;
import static com.mygdx.game.extra.Utils.USER_PLATFORM;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.mygdx.game.extra.Utils;

public class Platform extends Actor {

        //Velocities
    public static final float PLATFSPEEDY = -2f;

    private static final float SPEEDX = 0;

    public static final float FALLSPEED= -4;

    //Texture
    private Animation<TextureRegion> platfAnimation;

    //Body
    private Body bodyPlatf;

    //Fixture
    private Fixture fixturePlatf;

    //StateTime
    private float stateTime;

    //Position
    private Vector2 position;

    //World
    private World world;

    /**
     * Constructor
     * @param world
     * @param animation
     * @param position
     */
    public Platform(World world, Animation<TextureRegion> animation, Vector2 position) {
        this.platfAnimation = animation;
        this.position = position;
        this.world = world;
        this.stateTime = 0f;
        createBodyPlatf(position);
        createFixture();
    }


    /**
     * Create body
     * @param position
     */
    private void createBodyPlatf(Vector2 position) {
        BodyDef def = new BodyDef();
        def.position.set(position);
        def.type = BodyDef.BodyType.KinematicBody;
        bodyPlatf = world.createBody(def);
        bodyPlatf.setUserData(USER_PLATFORM);
        bodyPlatf.setLinearVelocity(SPEEDX,PLATFSPEEDY);
    }


    /**
     * Create the Platform's Fixture
     */
    private void createFixture() {
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(PLATFORM_WIDTH/2, PLATFORM_HEIGHT/2);
        this.fixturePlatf = bodyPlatf.createFixture(shape, 8);
        this.fixturePlatf.setSensor(true);
        this.fixturePlatf.setUserData(USER_PLATFORM);
        //shape dispose
        shape.dispose();
    }

    /**
     * Moves the platform as if it were falling
     * @param fallSpeed
     */
    public void goDown(float fallSpeed){
        this.bodyPlatf.setLinearVelocity(0,fallSpeed);
    }

    /**
     * Allows know if the platform is out of screen
     * @return
     */
    public boolean isOutOfScreen(){
        return this.bodyPlatf.getPosition().y <= PLATFORM_HEIGHT;
    }




    //Platform action
    @Override
    public void act(float delta) {
        super.act(delta);
    }

    //Draw platform
    @Override
    public void draw(Batch batch, float parentAlpha) {
        setPosition(this.bodyPlatf.getPosition().x - (PLATFORM_WIDTH/2), this.bodyPlatf.getPosition().y - (PLATFORM_HEIGHT/2) );
        batch.draw(this.platfAnimation.getKeyFrame(stateTime, true), getX(),getY(),PLATFORM_WIDTH,PLATFORM_HEIGHT);

        stateTime += Gdx.graphics.getDeltaTime();
    }

    /**
     * Stop the body
     */
    public void stopPlatform() {
        this.bodyPlatf.setLinearVelocity(0, 0);
    }


    /**
     * Detach the body destroying its fixture and destroying it self
     */
    public void detach(){
        bodyPlatf.destroyFixture(fixturePlatf);
        world.destroyBody(bodyPlatf);
    }
}
