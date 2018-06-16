package nl.NG.Jetfightergame.Tools.DataStructures;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Geert van Ieperen
 * created on 13-12-2017.
 * A timedQueue that uses ArrayDeque for implementation.
 * Includes synchoronized adding and deletion
 */
public class BlockingTimedArrayQueue<T> implements TimedQueue<T>, Serializable {

    /** prevents race-conditions upon adding and removing */
    private Lock changeGuard;

    /** timestamps in seconds. Private, as semaphore must be handled */
    private Queue<Double> timeStamps;
    private Queue<T> elements;

    /**
     * @param capacity the initial expected maximum number of entries
     */
    public BlockingTimedArrayQueue(int capacity) {
        timeStamps = new ArrayDeque<>(capacity);
        elements = new ArrayDeque<>(capacity);
        changeGuard = new ReentrantLock();
    }

    @Override
    public void add(T element, double timeStamp) {
        changeGuard.lock();
        timeStamps.add(timeStamp);
        elements.add(element);
        changeGuard.unlock();
    }

    @Override
    public T getActive(double timeStamp) {
        // if (activeTimeStamp < timeStamp), there is no element available
        return (nextTimeStamp() < timeStamp) ? null : nextElement();
    }

    @Override
    public double timeUntilNext(double timeStamp) {
        return (nextTimeStamp() - timeStamp);
    }

    /**
     * upon returning, nextTimeStamp > timeStamp or there exist no item with such timestamp.
     * @param timeStamp the time until where the state of the queue should be updated.
     */
    public void updateTime(double timeStamp) {
        changeGuard.lock();

        while ((timeStamps.size() > 1) && (timeStamp > nextTimeStamp())) {
            progress();
        }
        changeGuard.unlock();
    }

    /**
     * unsafe progression of the queue
     */
    protected void progress() {
        timeStamps.remove();
        elements.remove();
    }

    /** returns the next queued timestamp in seconds or null if there is none */
    public Double nextTimeStamp(){
        return timeStamps.peek();
    }

    /** returns the next queued element or null if there is none */
    public T nextElement(){
        return elements.peek();
    }

    @Override
    public String toString() {
        Iterator<Double> times = timeStamps.iterator();
        Iterator elts = elements.iterator();

        StringBuilder s = new StringBuilder();
        s.append("TimedArray:");
        while (times.hasNext()){
            s.append("\n");
            s.append(String.format("%1.04f", times.next()));
            s.append(" > ");
            s.append(elts.next());
        }

        return s.toString();
    }
}