package nl.NG.Jetfightergame.Tools;

/**
 * a Collection that accepts floats and can only return the average of the last n entries (in constant time)
 * @author Geert van Ieperen
 * created on 5-2-2018.
 */
public class AveragingQueue {
    private float[] entries;
    private float sum = 0;
    private int head = 0;
    private int capacity;

    public AveragingQueue(int capacity) {
        this.entries = new float[capacity];
        this.capacity = capacity;
    }

    public void add(float entry){
        sum -= entries[head];
        entries[head] = entry;
        sum += entry;
        head = (head + 1) % capacity;
    }

    public float average(){
        return sum / capacity;
    }
}
