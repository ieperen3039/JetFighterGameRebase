package nl.NG.Jetfightergame.Tools;

/**
 * @author Geert van Ieperen
 *         created on 8-11-2017.
 */
public class Extreme<T extends Comparable<T>> {
    private final boolean max;
    private T extremest = null;

    /**
     * tracks the extremest value that a variable will reach
     * best use is for debugging purposes
     * @param getMax true if the maximum must be tracked, false if the minimum must be tracked
     */
    public Extreme(T initial, boolean getMax) {
        this.extremest = initial;
        this.max = getMax;
    }

    /**
     * tracks the extremest value that a variable will reach
     * best use is for debugging purposes
     * @param getMax true if the maximum must be tracked, false if the minimum must be tracked
     */
    public Extreme(boolean getMax){
        this.max = getMax;
    }

    /**
     * compares the current value with the next, updating the current iff
     * (the current is null or the comparator returns that the new item is strictly extremer than the current)
     * @param newItem a new value, possibly null
     * @return true if the current value was updated, false otherwise
     */
    public boolean check(T newItem) {
        if (extremest != null) {
            int comparision = newItem.compareTo(extremest);
            if (comparision == 0 || (comparision < 0) == max) {
                return false;
            }
        }
        extremest = newItem;
        return true;
    }

    public T get() {
        return extremest;
    }

    public void updateAndPrint(String name, T newItem, String unit) {
        if (check(newItem)) Toolbox.printFrom(2, name + ": " + newItem + " " + unit);
    }

    public void reset(){
        extremest = null;
    }

    public void print(String name, String unit) {
        Toolbox.printFrom(2, name + ": " + get() + " " + unit);
    }
}
