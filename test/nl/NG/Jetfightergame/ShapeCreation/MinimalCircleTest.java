package nl.NG.Jetfightergame.ShapeCreation;

import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author Geert van Ieperen created on 21-6-2018.
 */
public class MinimalCircleTest {

    private static final double errorMargin = 1E-6;

    @Test
    public void getMinimalCircleTest1() {
        PosVector[] input = new PosVector[]{
                new PosVector(5, -1, 0),
                new PosVector(4, 3, 0),
                new PosVector(-3, -1, 0),
                new PosVector(-2, -5, 0),
        };
        Pair<PosVector, Float> expected = new Pair<>(new PosVector(1, -1, 0), 5f);

        Pair<PosVector, Float> result = Shape.getMinimalCircle(Arrays.asList(input));

        Assert.assertEquals(expected, result);
    }

    @Test
    public void getMinimalCircleTest2() {
        PosVector[] input = new PosVector[]{
                new PosVector(9, 10, 13), // 8^2 + 9^2 + 12^2 = 17^2
                new PosVector(1, 1, 1),
                new PosVector(9, 10, 0), // angle is larger than (1/2 pi)
                new PosVector(3, 4, 5),
        };

        Pair<PosVector, Float> result = Shape.getMinimalCircle(Arrays.asList(input));

        System.out.println(result);
        PosVector mid = result.left;
        float range = result.right;

        float midTo0 = mid.to(input[0], new DirVector()).length();
        float midTo1 = mid.to(input[1], new DirVector()).length();
        float midTo2 = mid.to(input[2], new DirVector()).length();
        float midTo3 = mid.to(input[3], new DirVector()).length();
        System.out.println(midTo0);
        System.out.println(midTo1);
        System.out.println(midTo2);
        System.out.println(midTo3);

        Assert.assertEquals(midTo0, range, errorMargin);
        Assert.assertEquals(midTo1, range, errorMargin);
        Assert.assertEquals(midTo2, range, errorMargin);
        Assert.assertTrue(midTo3 < range);
    }
}