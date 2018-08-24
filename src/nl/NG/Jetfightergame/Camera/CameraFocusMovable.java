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
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen. Created on 21-8-2018.
 */
public class CameraFocusMovable extends AbstractJet implements Spectral {
    private static final float MOVE_FACTOR = 100f;
    private static final float ROLL_FACTOR = 2f;

    private boolean didPrint = false;

    private CameraFocusMovable(PosVector position, Quaternionf rotation, GameTimer timer) {
        super(0, position, rotation, Material.GLOWING,
                1, 0, 0, 0, 0, 0, 0, 0,
                timer, 0, 0, null, null);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public void applyPhysics(DirVector netForce) {
        float deltaTime = gameTimer.getGameTime().difference();
        controller.update();

        float forward = controller.throttle() * deltaTime * MOVE_FACTOR;
        float toLeft = controller.yaw() * deltaTime * MOVE_FACTOR;

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
            extraRotation.rotateX(up.dot(yRel) * -up.angle(yRel));
        }

        // transform velocity to local, reduce drifting, then transform back to global space
        Quaternionf turnBack = getRotation().invert(new Quaternionf());
        extraPosition.rotate(turnBack);
        extraPosition.add(forward, toLeft, 0);
        extraPosition.rotate(rotation);


        float rotLeft = controller.roll() * deltaTime * -ROLL_FACTOR;
        float rotUp = controller.pitch() * deltaTime * -ROLL_FACTOR;
        extraRotation.rotate(0, rotUp, rotLeft);
    }

    @Override
    public void draw(GL2 gl) {
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
        return interpolatedPosition();
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
    }

    @Override
    public EntityFactory getFactory() {
        return new Factory(position, rotation);
    }

    public static class Factory extends EntityFactory {
        public Factory() {
        }

        public Factory(PosVector pos, Quaternionf rot) {
            super(EntityClass.INVISIBLE_ENTITY, pos, rot, new DirVector());
        }


        @Override
        public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
            return new CameraFocusMovable(position, rotation, game.getTimer());
        }
    }
}
