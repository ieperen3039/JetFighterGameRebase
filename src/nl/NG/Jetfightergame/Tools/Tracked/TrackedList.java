package nl.NG.Jetfightergame.Tools.Tracked;

import java.util.ArrayDeque;

/**
 * @author Geert van Ieperen
 * created on 13-12-2017.
 */
public class TrackedList<T> extends TrackedObject<T> {

    /** FIFO queue: last is newest */
    private ArrayDeque<T> olderValues;

    public TrackedList(T current, T previous) {
        super(current, previous);
        olderValues = new ArrayDeque<>();
    }

    public TrackedList(T initial) {
        super(initial);
    }

    @Override
    public void update(T newElement) {
        olderValues.addLast(previous());
        super.update(newElement);
    }

    /**
     * removes values from the queue until target has been reached (always keeps the previous value)
     * @param index the index that must be retrieved. 0 is the newest inserted value
     * @return the object that was inserted {@code index + 1} insertions ago
     */
    public T access(int index){
        if (index == 0) {
            olderValues.clear();
            return current();
        }
        if (index == 1) {
            olderValues.clear();
            return previous();
        }
        for (; index > 2; index--) {
            olderValues.removeFirst();
        }
        return olderValues.peekFirst();
    }
}
