package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

/**
 * refers to the entity as visible in the world
 * @author Geert van Ieperen. Created on 9-7-2018.
 */
public class PowerupEntity extends MovingEntity implements Spectral {
    private static final float DESPAWN_TIME = 0.5f;
    private static final float RESPAWN_TIME = 20f;
    private static final float SCALING = 3f;
    private static final Quaternionf BASE_ROTATION = new Quaternionf()
            .rotateTo(new Vector3f(-1, -1, -1), new Vector3f(0, 0, -1));

    private final PowerupColor type;

    private boolean isCollected = false;
    private float collectionTime = 0;
    private Shape shape;

    private int debugValue = 0;

    /**
     * allows for using values "Powerup_TYPE" where TYPE must be replaced with any of the {@link PowerupColor} enum
     * values
     */
    public static void init() {
        for (PowerupColor type : PowerupColor.values()) {
            addConstructor("Powerup_" + type, (id, position, rotation, velocity, game) ->
                    new PowerupEntity(id, type, position, game.getTimer(), game)
            );
        }
    }

    /**
     * creates a collectible power-up on the given fixed position in the world
     * @param id id of this entity given by the game
     * @param type     type of this powerup
     * @param position position in the world
     * @param timer    the timer (used for rendering animations)
     * @param game the place to deposit particles
     */
    private PowerupEntity(int id, PowerupColor type, PosVector position, GameTimer timer, SpawnReceiver game) {
        super(id, position, DirVector.zeroVector(), new Quaternionf(), 0, 1, timer, game);
        this.type = type;
        this.position = position;
        shape = GeneralShapes.CUBE;
        Logger.printOnline(() -> String.valueOf(debugValue));
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        if (isCollected) return;
        ms.scale(ServerSettings.POWERUP_COLLECTION_RANGE/2);
        action.accept(GeneralShapes.CUBE);
    }

    @Override
    public void draw(GL2 gl) {
        float s = isCollected ? SCALING * ((renderTime() - collectionTime) / DESPAWN_TIME) : SCALING;
        if (s < 0) return;

        // TODO model and animation
        gl.setMaterial(Material.GLOWING, type.color);
        gl.pushMatrix();
        {
            gl.translate(position);

            float rot = 1f * renderTime();
            gl.rotate(DirVector.zVector(), rot);
            gl.scale(s);

            gl.rotate(BASE_ROTATION);
            gl.draw(shape);
        }
        gl.popMatrix();
    }

    @Override
    public void update(float currentTime) {
        debugValue++;
        if (isCollected && (currentTime > (collectionTime + RESPAWN_TIME))) {
            isCollected = false;
        }
    }

    @Override
    protected void updateShape(float deltaTime) {

    }

    @Override
    public void applyPhysics(DirVector netForce, float deltaTime) {
        // Non.
    }

    @Override
    public void toLocalSpace(MatrixStack ms, Runnable action) {
        ms.pushMatrix();
        ms.translate(position);
        action.run();
        ms.popMatrix();
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(Material.GLOWING, type.color);
    }

    @Override
    public void acceptCollision(Collision cause) {
        if (isCollected) return;

        MovingEntity source = cause.source();
        if (source instanceof AbstractJet) {
            // if the player accepts it, remove from field
            isCollected = ((AbstractJet) source).addPowerup(type);

            if (isCollected) {
                float time = cause.timeScalar * gameTimer.getRenderTime().difference() + gameTimer.time();
                entityDeposit.powerupCollect(this, time, PowerupColor.NONE);
            }
        }
    }

    public PowerupColor getPowerupType() {
        return type;
    }

    @Override
    public float getRange() {
        return isCollected ? 0 : ServerSettings.POWERUP_COLLECTION_RANGE;
    }

    @Override
    public PosVector getExpectedMiddle() {
        return new PosVector(position);
    }
}
