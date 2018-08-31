package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.Hitbox.Collision;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Spectral;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.GameState.RacePathDescription;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Sound.MovingAudioSource;
import nl.NG.Jetfightergame.Sound.Sounds;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen. Created on 21-8-2018.
 */
public class CameraFocusMovable extends AbstractJet implements Spectral {
    private static final float MOVE_FACTOR = 100f;
    private static final float ROLL_FACTOR = 3f;

    private boolean didPrint = false;
    private boolean doDraw;

    public CameraFocusMovable(PosVector position, Quaternionf rotation, GameTimer timer, boolean doDraw, SpawnReceiver entityDeposit) {
        super(-1, position, rotation, Material.GLOWING,
                1, 0, 0, 0, 0, 0, 0, 0,
                timer, 0, 0, entityDeposit, null);
        this.doDraw = doDraw;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void applyPhysics(DirVector netForce) {
        float deltaTime = gameTimer.getGameTime().difference();
        if (deltaTime == 0) return;

        controller.update();

        float forward = controller.throttle() * deltaTime * MOVE_FACTOR;
        float toLeft = controller.yaw() * deltaTime * -MOVE_FACTOR;

        if (controller.primaryFire() && !didPrint) {
            PosVector pos = getPosition();
            DirVector dir = DirVector.xVector();
            dir.rotate(rotation);
            RacePathDescription.printCheckpointData(true, pos, dir, 100);
        }
        didPrint = controller.primaryFire();

        if (controller.secondaryFire()) {
            Vector3f yRel = DirVector.yVector().rotate(rotation);
            DirVector up = DirVector.zVector();
            extraRotation.rotateX(up.dot(yRel) * -0.5f * up.angle(yRel));
        }

        // transform velocity to local, reduce drifting, then transform back to global space
        Quaternionf turnBack = getRotation().invert(new Quaternionf());
        extraPosition.rotate(turnBack);
        extraPosition.add(forward, toLeft, 0);
        extraPosition.rotate(rotation);

        float rotLeft = controller.roll() * deltaTime * -ROLL_FACTOR;
        float rotUp = controller.pitch() * deltaTime * ROLL_FACTOR;
        extraRotation.rotate(0, rotUp, rotLeft);
        extraVelocity = DirVector.zeroVector();
    }

    @Override
    public void update() {
        super.update();
        float time = gameTimer.time();
        if (!entityDeposit.isHeadless()) {
            addStatePoint(time, position, rotation);
        }
    }

    @Override
    public void draw(GL2 gl) {
        if (!doDraw) return;

        gl.pushMatrix();
        {
            gl.translate(position);
            Toolbox.draw3DPointer(gl);
        }
        gl.popMatrix();
    }

    @Override
    public float getRange() {
        return 0;
    }

    @Override
    public PosVector getExpectedMiddle() {
        return new PosVector(extraPosition);
    }

    @Override
    protected void updateShape(float deltaTime) {
    }

    @Override
    public Collision checkCollisionWith(Touchable other, float deltaTime) {
        return null;
    }

    @Override
    public PosVector getPilotEyePosition() {
        return getPosition();
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
    }

    @Override
    public EntityFactory getFactory() {
        return new Factory(position, rotation, true);
    }

    @Override
    public String getPlaneDataString() {
        return gameTimer + " > " + this.getClass().getSimpleName();
    }

    @Override
    protected MovingAudioSource getBoosterSound() {
        return new MovingAudioSource(Sounds.booster, this, 0.01f, BOOSTER_GAIN, true);
    }

    public static class Factory extends EntityFactory {
        private boolean doDraw;

        public Factory() {
        }

        public Factory(PosVector pos, Quaternionf rot, boolean doDraw) {
            this.type = EntityClass.SPECTATOR_CAMERA;
            this.position = pos;
            this.rotation = rot;
            this.velocity = new DirVector();
            this.id = -1;
            this.doDraw = doDraw;
        }

        @Override
        protected void writeInternal(DataOutput out) throws IOException {
            super.writeInternal(out);
            out.writeBoolean(doDraw);
        }

        @Override
        protected void readInternal(DataInput in) throws IOException {
            super.readInternal(in);
            doDraw = in.readBoolean();
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
            return new CameraFocusMovable(position, rotation, game.getTimer(), doDraw, game);
        }
    }
}
