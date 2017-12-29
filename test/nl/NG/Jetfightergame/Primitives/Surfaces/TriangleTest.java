package nl.NG.Jetfightergame.Primitives.Surfaces;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Geert van Ieperen
 * created on 27-12-2017.
 */
public class TriangleTest extends PlaneTest {
    private PosVector from;
    private PosVector to;

    @Before
    public void setUp() {
        PosVector a = new PosVector(-10, 10, 0);
        PosVector b = new PosVector(10, -10, 0);
        PosVector c = new PosVector(10, 10, 0);
        instance = new Triangle(a, b, c, DirVector.zVector());
    }

    @Test
    public void orthogonalPositive(){
        from = new PosVector(1, 1, 4);
        to = new PosVector(1, 1, 1);
        testIntersect(from, to, null);
    }

    @Test
    public void horizontalPositive(){
        from = new PosVector(2, 2, 3);
        to = new PosVector(4, 4, 3);
        testIntersect(from, to, null);
    }

    @Test
    public void horizontalNegative(){
        from = new PosVector(2, 2, -3);
        to = new PosVector(4, 4, -3);
        testIntersect(from, to, null);
    }

    @Test
    public void angledPositive(){
        from = new PosVector(2, 2, 3);
        to = new PosVector(1, 1, -1);
        testIntersect(from, to, new PosVector(1.25f, 1.25f, 0));
    }

    @Test
    public void cutUnderAngle(){
        from = new PosVector(0, 0, 3);
        to = new PosVector(4, 4, -1);
        testIntersect(from, to, new PosVector(3, 3, 0));
    }

    @Test
    public void cutOnZeroZeroZero (){
        from = new PosVector(2, 2, 2);
        to = new PosVector(-2, -2, -2);
        testIntersect(from, to, new PosVector(0, 0, 0));
    }

    @Test
    public void cutUnderAngleInverse(){
        from = new PosVector(0, 0, -3);
        to = new PosVector(4, 4, 1);
        testIntersect(from, to, null);
    }

    @Test
    public void touchEnd(){
        from = new PosVector(1, 1, 1);
        to = new PosVector(0, 1, 0);
        testIntersect(from, to, new PosVector(0, 1, 0));
    }

    @Test
    public void touchBegin(){
        from = new PosVector(1, 1, 0);
        to = new PosVector(-2, 2, 1);
        testIntersect(from, to, null);
    }

    @Test
    public void cutFromTouch(){
        from = new PosVector(1, 1, 0);
        to = new PosVector(-2, 2, -3);
        testIntersect(from, to, new PosVector(1, 1, 0));
    }

    @Test
    public void upToTouch(){
        from = new PosVector(1, -1, -1);
        to = new PosVector(1, 2, 0);
        testIntersect(from, to, null);
    }

    @Test
    public void nullVector(){
        from = new PosVector(1, 2, 3);
        to = new PosVector(1, 2, 3);
        testIntersect(from, to, null);
    }

    @Test
    public void asidePlane(){
        from = new PosVector(11, 1, 1);
        to = new PosVector(11, -1, -1);
        testIntersect(from, to, null);
    }

    @Test
    public void diagonalHoverAsidePlane(){
        from = new PosVector(0, 0, 5);
        to = new PosVector(15, 15, -1);
        testIntersect(from, to, null);
    }

    @Test
    public void rakeEdge(){
        from = new PosVector(11, 11, 1);
        to = new PosVector(9, 9, -1);
        testIntersect(from, to, new PosVector(10, 10, 0));
    }
}
