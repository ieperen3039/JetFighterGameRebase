package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Rendering.Interpolation.VectorInterpolator;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Geert van Ieperen
 * created on 15-12-2017.
 */
public class VectorInterpolatorTest {
    private VectorInterpolator instance;

    @Before
    public void setUp() throws Exception {
        instance = new VectorInterpolator(10, PosVector.zeroVector());

        instance.add(new PosVector(1, 0, 0), 1);
        instance.add(new PosVector(0, 1, 1), 1.5f);
        instance.add(new PosVector(1, 0, 0), 2);
        instance.add(new PosVector(3, 0, 0), 4);
        instance.add(new PosVector(4, 0, 0), 5);
    }

    @Test
    public void testVectorInterpolator() {
        testValue(new PosVector(0.5f, 0.5f, 0.5f), 1.25f);
    }

    @Test
    public void testRegularUse() {
        testValue(new PosVector(2, 0, 0), 3);
    }

    @Test
    public void testExtrapolation() {
        testValue(new PosVector(5, 0, 0), 6);
    }

    @Test
    public void testExtrapolationReplace(){
        testValue(new PosVector(5, 0, 0), 6);

        instance.add(new PosVector(2, 0, 0), 7);
        testValue(new PosVector(3, 0, 0), 6);
    }

    private void testValue(PosVector expected, float timeStamp) {
        Vector answer = instance.getInterpolated(timeStamp);
        assert answer.equals(expected) : "interpolation on " + timeStamp + " resulted in " + answer + " while it should be " + expected;
    }

}