package nl.NG.Jetfightergame.Primitives.Surfaces;

import nl.NG.Jetfightergame.AbstractEntities.Collision;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.junit.Before;

/**
 * Created by s152717 on 14-2-2017.
 */
public class PlaneTest {
    Plane instance;

    @Before
    public void setUp() {
        instance = null;
    }

    /** testclass for stopVector
     * @param first startpoint of line
     * @param second endpoint of line
     * @param expected the expected point where it would hit the environment defined in {@code instance}
     */
    void testIntersect(PosVector first, PosVector second, PosVector expected) {
        DirVector dir = first.to(second, new DirVector());
        System.out.println("\nVector " + first + " towards " + second + ": direction is " + dir);
        if (expected == null) System.out.println("Expecting no intersection");

        Collision box = instance.getCollisionWith(first, dir, second);

        PosVector result = new PosVector();

        if (box != null) {
            // get position of the new vector
            first.add(dir.scale((float) box.timeScalar, new DirVector()), result);
            System.out.println("Hitpoint: " + result);
        }

        if (expected == null) {
            assert box == null :
                    String.format("Testcase gave %s where no intersection was expected",
                            result);
        } else {
            assert box != null :
                    String.format("Testcase gave no intersection where %s was expected",
                            expected);

            Float diff = expected.to(result, new DirVector()).length();
            if (!Toolbox.almostZero(diff)) {
                throw new AssertionError(
                        String.format("Testcase gave %s where %s was expected: difference is %f units",
                                result, expected, diff)
                );
            }
        }
    }
}