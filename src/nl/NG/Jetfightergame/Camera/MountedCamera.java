package nl.NG.Jetfightergame.Camera;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Tools.Tracked.ExponentialSmoothVector;
import nl.NG.Jetfightergame.Tools.Tracked.SmoothTrackedVector;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 * created on 25-12-2017.
 */
public class MountedCamera implements Camera {

    private final AbstractJet target;
    private final SmoothTrackedVector<DirVector> eye;

    public MountedCamera(AbstractJet target) {
        this.target = target;
        this.eye = new ExponentialSmoothVector<>(getFocus(target), 0.002f);
    }

    private DirVector getFocus(AbstractJet target) {
        return target.relativeInterpolatedDirection(DirVector.xVector());
    }

    @Override
    public DirVector vectorToFocus() {
        return eye.current();
    }

    @Override
    public void updatePosition(float deltaTime) {
        eye.updateFluent(getFocus(target), deltaTime);
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
