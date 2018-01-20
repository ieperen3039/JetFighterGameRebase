package nl.NG.Jetfightergame.Primitives.Particles;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Geert van Ieperen
 * created on 20-1-2018.
 */
@SuppressWarnings("Duplicates")
public class TriangleParticleTest {

    @Before
    public void setUp() {
        combinedTransformation = null;
        partA = new PosVector(1, 0, 0);
        partB = new PosVector(0, 1, 0);
        partC = new PosVector(0, 0, 0);
    }

    private Matrix4f combinedTransformation;
    private PosVector partA;
    private PosVector partB;
    private PosVector partC;

    @Test
    public void testMultiple() {
        for (int i = 0; i < 10; i++) {
            PosVector center = randomPosVec(10);

            PosVector a = center.add(DirVector.random(), new PosVector());
            PosVector b = center.add(DirVector.random(), new PosVector());
            PosVector c = center.add(DirVector.random(), new PosVector());

            setMapping(a, b, c);

            testTransform(a, partA);
            testTransform(b, partB);
            testTransform(c, partC);
        }
    }

    @Test
    public void testMapA() {
        PosVector a = randomPosVec(12);
        PosVector b = randomPosVec(1);
        PosVector c = randomPosVec(1);
        setMapping(a, b, c);
        testTransform(a, partA);
    }

    @Test
    public void testMapB() {
        PosVector a = randomPosVec(1);
        PosVector b = randomPosVec(12);
        PosVector c = randomPosVec(1);
        setMapping(a, b, c);
        testTransform(b, partB);
    }

    @Test
    public void testMapC() {
        PosVector a = randomPosVec(1);
        PosVector b = randomPosVec(1);
        PosVector c = randomPosVec(12);
        setMapping(a, b, c);
        testTransform(c, partC);
    }

    @Test
    public void testIdentity() {
        setMapping(partA, partB, partC);
        testTransform(partA, partA);
        testTransform(partB, partB);
        testTransform(partC, partC);
    }

    private PosVector randomPosVec(float scalar) {
        PosVector vector = DirVector.random().toPosVector();
        vector.scale(scalar, vector);
        return vector;
    }

    private void setMapping(PosVector a, PosVector b, PosVector c) {
        combinedTransformation = TriangleParticle.getMapping(a, b, c);
    }

    private void testTransform(PosVector a, PosVector b) {
        if (combinedTransformation == null) throw new RuntimeException("test did not set transformation");

        b = new PosVector(transform(b));

        System.out.println(a + " transformed to " + b);
        assert a.equals(b): "Incorrect transformation:\n" + combinedTransformation;
    }


    public Vector3f transform(Vector a) {
        return a.mulPosition(combinedTransformation, new Vector3f());
    }
}