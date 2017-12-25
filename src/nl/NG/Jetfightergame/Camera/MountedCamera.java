package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.EntityDefinitions.AbstractJet;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 * created on 25-12-2017.
 */
public class MountedCamera implements Camera {

    private final AbstractJet target;

    public MountedCamera(AbstractJet target) {
        this.target = target;
    }

    @Override
    public DirVector vectorToFocus() {
        return target.relativeInterpolatedDirection(DirVector.xVector());
    }

    @Override
    public void updatePosition(float deltaTime) {

    }

    @Override
    public PosVector getEye() {
        final PosVector dest = new PosVector(target.interpolatedPosition());
        dest.add(target.getPilotEyePosition(), dest);
        return dest;
    }

    @Override
    public PosVector getFocus() {
        final PosVector dest = getEye();
        dest.add(vectorToFocus(), dest);
        return dest;
    }

    @Override
    public DirVector getUpVector() {
        return target.relativeInterpolatedDirection(DirVector.zVector());
    }
}
