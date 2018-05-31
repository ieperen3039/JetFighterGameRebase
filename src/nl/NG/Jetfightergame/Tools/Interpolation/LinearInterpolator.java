package nl.NG.Jetfightergame.Tools.Interpolation;

import nl.NG.Jetfightergame.Tools.DataStructures.BlockingTimedArrayQueue;

/**
 * @author Geert van Ieperen
 * created on 15-12-2017.
 */
public abstract class LinearInterpolator<T> extends BlockingTimedArrayQueue<T> {
    private double activeTime;
    protected T activeElement;

    /**
     * @param capacity the initial expected maximum number of entries
     * @param initialItem this item will initially be placed in the queue twice.
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
        double firstTime = activeTime;
        T firstElt = activeElement;
        double secondTime = nextTimeStamp();
        T secondElt = nextElement();

        if (firstElt == null) {
            firstElt = secondElt;
        }

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

    @Override
    public String toString() {
        final String backend = super.toString();
        return backend.replaceFirst("\n", "\n" + String.format("%1.04f", activeTime) + " > " + activeElement + "\n");
    }

    /**
     * @return the derivative of the most recent returned value of getInterpolated()
     */
    public abstract T getDerivative();

    protected float getTimeDifference() {
        return (float) (nextTimeStamp() - activeTime);
    }
}
