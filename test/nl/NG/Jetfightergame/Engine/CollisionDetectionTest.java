package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Engine.GameState.CollisionDetection;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Geert van Ieperen
 * created on 18-3-2018.
 */
@SuppressWarnings("unchecked")
public class CollisionDetectionTest extends CollisionDetection {
    public CollisionDetectionTest() {
        super(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    @Test
    public void insertionSortTest() {
        int id = 1;
        Pair[] nums = {new Pair(1f, id++), new Pair(2f, id++), new Pair(3f, id++), new Pair(2f, id++), new Pair(2f, id++), new Pair(4f, id++)};
        Toolbox.insertionSort(nums, p -> (Float) p.left);
        Toolbox.print(Arrays.asList(nums));
    }
}