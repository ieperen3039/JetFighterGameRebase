package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Tools.DataStructures.TimedQueue;
import nl.NG.Jetfightergame.Tools.Interpolation.FloatInterpolator;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Geert van Ieperen
 * created on 15-12-2017.
 */
public class FloatInterpolatorTest {
    private FloatInterpolator instance;

    @Before
    public void setUp() throws Exception {
        instance = new FloatInterpolator(10, 0f);
        instance.add(1f, 0);
        instance.add(2f, 1);
        instance.add(4f, 3);
        instance.add(6f, 4);
    }

    @Test
    public void testRegularUse() {
        testValue(3f, 2);
    }

    @Test
    public void testCollision() {
        testValue(2f, 1);
    }

    @Test
    public void testFlatInterpolation() {
        testValue(5f, 3.5f);
    }

    @Test
    public void testExtrapolation() {
        testValue(7f, 4.5f);
    }

    @Test
    public void testExtrapolationEffect() {
        testValue(7f, 4.5f);
        testValue(7f, 4.5f);
        instance.add(4f, 5);
        testValue(5f, 4.5f);
    }

    private void testValue(Float expected, float timeStamp) {
        Float answer = instance.getInterpolated(timeStamp);
        assert answer.equals(expected) : "interpolation on time " + timeStamp + " resulted in " + answer + " while it should be " + expected;
    }

    @Test
    public void testTimedQueue() {
        TimedQueue<Float> queue = instance;
        float[] challenges =      {-1, 0,  1,  2,  3,  4};
        float[] expectedAnswers = {0f, 0f, 2f, 4f, 4f, 6f};

        for (int i = 0; i < challenges.length; i++) {
            Float answer = queue.getActive(challenges[i]);
            Float expected = expectedAnswers[i];
            assert answer.equals(expected) : "polling on time " + challenges[i] + " resulted in " + answer + " while it should be " + expected;
        }

        assert queue.getActive(5) == null : "time = 5 did not return null";
    }

}