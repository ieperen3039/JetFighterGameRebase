package nl.NG.Jetfightergame.Tools;

/**
 * @author Geert van Ieperen
 * created on 15-12-2017.
 */
public abstract class LinearInterpolator<T> extends TimedArrayDeque<T> {
    private double activeTime;
    private T activeElement;

    /**
     * @param capacity the initial expected maximum number of entries
     * @param initialItem
     */
    public LinearInterpolator(int capacity, T initialItem) {
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

        Float fraction = (float) ((timeStamp - firstTime) / (secondTime - firstTime));
        if (fraction.isNaN()) return firstElt;

        return interpolate(firstElt, secondElt, fraction);
    }

    /**
     * interpolate using linear interpolation
     * @return firstElt + (secondElt - firstElt) * fraction
     */
    protected abstract T interpolate(T firstElt, T secondElt, float fraction);

    @Override
    protected void progress() {
        activeTime = nextTimeStamp();
        activeElement = nextElement();
        super.progress();
    }
}
