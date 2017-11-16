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

    public boolean update(T newItem) {
        int larger = max ? 1 : -1;
        if (extremest == null || (newItem.compareTo(extremest) == larger)){
            extremest = newItem;
            return true;
        }
        return false;
    }

    public T get() {
        return extremest;
    }

    public void updateAndPrint(String name, T newItem, String unit) {
        if (update(newItem)) Toolbox.printFrom(2, name + ": " + newItem + " " + unit);
    }

    public void reset(){
        extremest = null;
    }

    public void print(String name, String unit) {
        Toolbox.printFrom(2, name + ": " + get() + " " + unit);
    }
}
