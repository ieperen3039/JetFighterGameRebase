package nl.NG.Jetfightergame.AbstractEntities.Powerups;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.EntityMapping;
import nl.NG.Jetfightergame.AbstractEntities.Factory.EntityClass;
import nl.NG.Jetfightergame.AbstractEntities.Factory.EntityFactory;
import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Spectral;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * refers to the entity as visible in the world
 * @author Geert van Ieperen. Created on 9-7-2018.
 */
public class PowerupEntity extends MovingEntity implements Spectral {
    private static final float DESPAWN_TIME = 0.2f;
    private static final float RESPAWN_TIME = 20f;
    private static final float SCALING = 2f;
    private static final Quaternionf BASE_ROTATION = new Quaternionf()
            .rotateTo(new Vector3f(-1, -1, -1), new Vector3f(0, 0, -1));

    private final PowerupColor type;

    private boolean isCollected = false;
    private float collectionTime = 0;
    private Shape shape;

    /**
     * creates a collectible power-up on the given fixed position in the world
     * @param id id of this entity given by the game
     * @param type     type of this powerup
     * @param position position in the world
     * @param timer    the timer (used for rendering animations)
     * @param game the place to deposit particles
     */
    private PowerupEntity(int id, PowerupColor type, PosVector position, GameTimer timer, SpawnReceiver game) {
        super(id, position, DirVector.zeroVector(), new Quaternionf(), 0, timer, game);
        this.type = type;
        this.position = position;
        shape = GeneralShapes.CUBE;
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        if (isCollected) return;
        ms.scale(ServerSettings.POWERUP_COLLECTION_RANGE/2);
        action.accept(GeneralShapes.ICOSAHEDRON);
    }

    @Override
    public void draw(GL2 gl) {
        float s;
        if (isCollected && renderTime() > collectionTime) {
            float timeSince = renderTime() - collectionTime;
            s = SCALING * (1 - (timeSince / DESPAWN_TIME));
            if (s < 0) return;
        } else {
            s = SCALING;
        }

        // TODO model and animation
        gl.setMaterial(Material.GLOWING, type.color);
        toLocalSpace(gl, () -> {
            float rot = 1f * renderTime();
            gl.rotate(DirVector.zVector(), rot);
            gl.scale(s);

            gl.rotate(BASE_ROTATION);
            gl.draw(shape);
        });
    }

    @Override
    public EntityFactory getFactory() {
        return new Factory(this);
    }

    @Override
    public void update(float currentTime) {
        if (isCollected && (currentTime > (collectionTime + RESPAWN_TIME))) {
            entityDeposit.powerupCollect(this, collectionTime, false);
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
            AbstractJet jet = (AbstractJet) source;
            isCollected = jet.addPowerup(type);

            if (isCollected) {
                float time = cause.timeScalar * gameTimer.getRenderTime().difference() + gameTimer.time();
                entityDeposit.powerupCollect(this, time, true);
                entityDeposit.playerPowerupState(jet, jet.getCurrentPowerup());
                collectionTime = time;
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

    public void setState(boolean isCollected, float collectionTime) {
        this.isCollected = isCollected;
        this.collectionTime = collectionTime;
    }

    public boolean isCollected() {
        return isCollected;
    }

    public static class Factory extends EntityFactory {
        private PowerupColor color;

        public Factory() {
            super();
        }

        public Factory(PowerupEntity e) {
            super(EntityClass.POWERUP, e);
            this.color = e.type;
        }

        public Factory(PosVector position, PowerupColor color) {
            super(EntityClass.POWERUP, position, new Quaternionf(), new DirVector());
            this.color = color;
        }

        @Override
        public void writeInternal(DataOutput out) throws IOException {
            super.writeInternal(out);
            out.writeByte(color.ordinal());
        }

        @Override
        public void readInternal(DataInput in) throws IOException {
            super.readInternal(in);
            color = PowerupColor.get(in.readByte());
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
            return new PowerupEntity(id, color, position, game.getTimer(), game);
        }
    }
}
