package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.TemporalEntity;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.Tools.Tracked.ExponentialSmoothVector;
import nl.NG.Jetfightergame.Tools.Tracked.SmoothTrackedVector;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * Implementation of a camera with a position and orientation.
 */
public class SpectatorFollowingCamera implements Camera {
    /** camera settings */
    private static final DirVector eyeRelative = new DirVector(-20, 0, 10);
    private static final DirVector focusRelative = new DirVector(5, 0, 0);
    private static final double EYE_PRESERVE = 0.5f;
    private static final double FOCUS_PRESERVE = 0.1f;
    private static final double UP_PRESERVE = 0.5f;
    private static final float TELEPORT_DISTANCE_SQ = 500f * 500f;

    /**
     * The position of the camera.
     */
    private final SmoothTrackedVector<PosVector> eye;
    private final SmoothTrackedVector<PosVector> focus;
    private final SmoothTrackedVector<DirVector> up;
    private final Player target;
    private final Environment gameState;
    private DirVector velocity;

    public SpectatorFollowingCamera(Player target, Environment gameState) {
        this(
                jetPosition(eyeRelative, target.jet()).toPosVector(), target,
                jetPosition(DirVector.zVector(), target.jet()).toDirVector(),
                target.jet().getPosition(), gameState
        );
    }

    public SpectatorFollowingCamera(PosVector eye, Player playerJet, DirVector up, PosVector focus, Environment gameState) {
        this.eye = new ExponentialSmoothVector<>(eye, EYE_PRESERVE);
        this.focus = new ExponentialSmoothVector<>(focus, FOCUS_PRESERVE);
        this.up = new ExponentialSmoothVector<>(up, UP_PRESERVE);
        this.target = playerJet;
        this.gameState = gameState;
    }

    /**
     * @param relativePosition a position relative to target
     * @param target           a target jet, where DirVector.X points forward
     * @return a new vector with the position translated to world-space
     */
    private static PosVector jetPosition(DirVector relativePosition, MovingEntity target) {
        if (TemporalEntity.isOverdue(target)) return relativePosition.toPosVector();

        PosVector targetPos = target.getPosition();
        if (!targetPos.isScalable()) return PosVector.zeroVector();

        final DirVector relative = target.relativeDirection(relativePosition);
        return targetPos.add(relative, new PosVector());
    }

    /**
     * @param deltaTime the animation time difference
     */
    @Override
    public void updatePosition(float deltaTime) {
        MovingEntity target = this.target.jet();
        final DirVector rawUp = target.relativeDirection(DirVector.zVector());
        final DirVector targetUp = rawUp.normalize(rawUp);

        final PosVector targetEye = jetPosition(eyeRelative, target);
        final PosVector targetFocus = jetPosition(focusRelative, target);
        if (!targetEye.isRegular() || !targetFocus.isRegular()) return;

        if (eye.current().distanceSquared(targetEye) > TELEPORT_DISTANCE_SQ) {
            eye.update(targetEye);
            focus.update(targetFocus);
        } else {
        eye.updateFluent(targetEye, deltaTime);
        focus.updateFluent(targetFocus, deltaTime);
        }

        focus.updateFluent(targetFocus, deltaTime);
        up.updateFluent(targetUp, deltaTime);
        velocity = eye.difference();
        if (deltaTime != 0) velocity.scale(1 / deltaTime);
    }

    @Override
    public DirVector vectorToFocus() {
        return getEye().to(getFocus(), new DirVector());
    }

    @Override
    public PosVector getEye() {
        PosVector pos = target.jet().getPosition();
        return gameState.rayTrace(pos, eye.current());
    }

    @Override
    public PosVector getFocus() {
        return focus.current();
    }

    @Override
    public DirVector getUpVector() {
        return up.current();
    }

    @Override
    public DirVector getVelocity() {
        return velocity;
    }
}
