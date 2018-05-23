package nl.NG.Jetfightergame.Tools.DataStructures;

/**
 * @author Geert van Ieperen
 * created on 13-12-2017.
 * a queue that allows a producer to queue timed objects (e.g. positions) while a consumer takes the next item from a
 * specified timestamp onwards.
 *
 * In contrary of what you may think, a float is only accurate up to 2^24 = 16 777 216 integer values.
 * This means that using a float will result in errors after 194 days of non-stop gaming.
 * we could use a double for timestamp instead, giving us 2^53 seconds = 9.00719925 * 10^15 sec = 285 420 920 year
 */
public interface TimedQueue<T> {

    /**
     * add an element to be accessible in the interval [the timeStamp of the previous item, the given timestamp]
     * @param element the element that will be returned upon calling {@link #getActive(double)}
     * @param timeStamp the timestamp in seconds from where this element becomes active
     */
    void add(T element, double timeStamp);

    /**
     * Receive the first element that is accessible, or null if no item is available on the period.
     * All elements with a timestamp before the returned item are no longer accessible.
     * One should not try to call this method with a timeStamp less than any previous call of this instance.
     *
     * @param timeStamp the timestamp in seconds where the returned element is active
     * @return the first element with a timestamp after the given timestamp.
     */
    T getActive(double timeStamp);

    /**
     * @return the difference in timestamp of the given parameter and the next item.
     * All elements with a timestamp before the returned item are no longer accessible, even for {@link #getActive(double)}
     * if this item does not exist, it returns a negative value equal to minus the time since the last item
     * @param timeStamp the timestamp in seconds from where the next element is active
     */
    double timeUntilNext(double timeStamp);


}
