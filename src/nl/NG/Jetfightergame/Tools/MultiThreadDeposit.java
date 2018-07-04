package nl.NG.Jetfightergame.Tools;

import javax.annotation.Nonnull;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

/**
 * A producer-consumer system where the producers can add items without any synchronisation to take place. The number of
 * producers is fixed, every new thread will require its own producer spot. For this reason it is not allowed to call
 * the accept method in a parallel stream. Adding a new producer uses internal synchronisation to ensure stability. This
 * class assumes that at most one consumer will ever access this. If this is not the case, external synchronisation is
 * sufficient. This class does not accept null values
 * @author Geert van Ieperen. Created on 4-7-2018.
 */
public class MultiThreadDeposit<T> implements Consumer<T>, Iterable<T> {

    private final int maxItems;
    private Object[][] storage;
    private Thread[] producers;
    private int[] writeIndices;

    private Lock threadRegistration;

    public MultiThreadDeposit(int maxNofProducers, int itemsQueued) {
        maxItems = itemsQueued;
        storage = new Object[maxNofProducers][itemsQueued];
        producers = new Thread[maxNofProducers];
        writeIndices = new int[maxNofProducers];
        threadRegistration = new ReentrantLock();
    }

    /**
     * adds a non-null item to the deposit, blocks if there is no space to do so
     * @param value the new value to be added to the deposit
     */
    @Override
    public void accept(T value) {
        Thread thread = Thread.currentThread();
        int listNr = getOrMake(thread);
        int index = writeIndices[listNr];
        writeIndices[listNr] = (index + 1) % maxItems;

        try {
            // try adding the item, but wait if there is no place yet
            while (storage[listNr][index] != null) Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        storage[listNr][index] = value;
    }

    private int getOrMake(Thread thread) {
        for (int i = 0; i < producers.length; i++) {
            Thread p = producers[i];
            if (p == thread) {
                return i;
            } else if (p == null) {
                // start synchronizing new join
                break;
            }
        }
        // register a new thread
        threadRegistration.lock();
        try {
            for (int i = 0; i < producers.length; i++) {
                if (producers[i] == null) {
                    producers[i] = thread;
                    return i;
                }
            }
            throw new IndexOutOfBoundsException("Too many producers. Capacity: " + producers.length);
        } finally {
            threadRegistration.unlock();
        }
    }

    @Nonnull
    @Override
    public Iterator<T> iterator() {
        return new CarelessIterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void forEach(Consumer<? super T> action) {
        for (int i = 0; i < storage.length; i++) {
            for (int j = 0; j < maxItems; j++) {
                Object o = storage[i][j];
                if (o != null) {
                    storage[i][j] = null;
                    action.accept((T) o);
                }
            }
        }
    }

    private class CarelessIterator implements Iterator<T> {
        int listIndex = 0;
        int itemIndex = 0;

        @Override
        public boolean hasNext() {
            return progress() != null;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T next() {
            Object value = progress();
            storage[listIndex][itemIndex] = null;
            itemIndex++;
            return (T) value;
        }

        private Object progress() {
            if (itemIndex == maxItems) {
                listIndex++;
                if (listIndex == storage.length) return null;
                itemIndex = 0;
            }

            Object o = storage[listIndex][itemIndex];
            if (o == null) {
                itemIndex++;
                return progress();
            }
            return o;
        }
    }
}
