package nl.NG.Jetfightergame.Tools;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.Semaphore;

/**
 * @author Geert van Ieperen
 * created on 13-12-2017.
 * A timedQueue that uses ArrayDeque for implementation.
 * Includes synchoronized adding and deletion
 */
public class TimedArrayDeque<T> implements TimedQueue<T> {

    /** prevents race-conditions upon adding and removing */
    private Semaphore changeGuard;

    /** timestamps in seconds. Private, as semaphore must be handled */
    private Queue<Double> timeStamps; //TODO possible copy on write
    private Queue<T> elements;

    /**
     * @param capacity the initial expected maximum number of entries
     */
    public TimedArrayDeque(int capacity){
        timeStamps = new ArrayDeque<>(capacity);
        elements = new ArrayDeque<>(capacity);
        changeGuard = new Semaphore(1);
    }

    @Override
    public void add(T element, double timeStamp) {
        try {
            changeGuard.acquire();
            timeStamps.add(timeStamp);
            elements.add(element);
            changeGuard.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public T getActive(double timeStamp) {
        updateTime(timeStamp);
        // if (activeTimeStamp < timeStamp), there is no element available
        return nextTimeStamp() < timeStamp ? null : nextElement();
    }

    @Override
    public double timeUntilNext(double timeStamp) {
        updateTime(timeStamp);
        return (nextTimeStamp() - timeStamp);
    }

    /**
     * upon returning, nextTimeStamp > timeStamp or there exist no item with such timestamp.
     * @param timeStamp the time until where the state of the queue should be updated.
     */
    protected void updateTime(double timeStamp) {
        while (timeStamps.size() > 1 && timeStamp > nextTimeStamp()) {
            progress();
        }
    }

    /**
     * executes upon removing the head of the queues
     */
    protected void progress() {
        try {
            changeGuard.acquire();
            timeStamps.remove();
            elements.remove();
            changeGuard.release();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /** returns the next queued timestamp in seconds or null if there is none */
    public Double nextTimeStamp(){
        return timeStamps.peek();
    }

    /** returns the next queued element or null if there is none */
    public T nextElement(){
        return elements.peek();
    }

}
