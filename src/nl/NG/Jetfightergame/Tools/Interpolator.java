package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;

/**
 * @author Geert van Ieperen
 * created on 15-12-2017.
 */
public abstract class Interpolator<T> extends TimedArrayDeque<T> {
    private double activeTime;
    private T activeElement;

    /**
     * @param capacity the initial expected maximum number of entries
     * @param initialItem
     */
    public Interpolator(int capacity, T initialItem) {
        super(capacity);
        add(initialItem, -1);
        add(initialItem, 0);
    }

    /**
     * @return the interpolated object defined by implementation
     */
    public T getInterpolated(float timeStamp){
        updateTime(timeStamp);
        if (nextTimeStamp() == null) throw new IllegalStateException("interpolator has less than two entries");

        double firstTime = activeTime;
        T firstElt = activeElement;
        double secondTime = nextTimeStamp();
        T secondElt = nextElement();

        return interpolate(timeStamp, firstTime, firstElt, secondTime, secondElt, new PosVector());
    }

    /**
     * interpolate using linear interpolation
     * @return firstElt + (secondElt - firstElt) * ((timeStamp - firstTime) / (secondTime - firstTime))
     */
    protected abstract T interpolate(float timeStamp, double firstTime, T firstElt, double secondTime, T secondElt, Vector dest);

    @Override
    protected void progress() {
        activeTime = nextTimeStamp();
        activeElement = nextElement();
        super.progress();
    }
}
