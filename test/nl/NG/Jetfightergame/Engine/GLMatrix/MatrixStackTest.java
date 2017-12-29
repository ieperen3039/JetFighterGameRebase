package nl.NG.Jetfightergame.Engine.GLMatrix;

import nl.NG.Jetfightergame.Camera.SimpleKeyCamera;
import nl.NG.Jetfightergame.Rendering.GLFWWindow;
import nl.NG.Jetfightergame.Shaders.GouraudShader;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.junit.Test;

import java.io.IOException;

/**
 * @author Geert van Ieperen
 * created on 22-12-2017.
 */
public class MatrixStackTest {
    private MatrixStack ms;

    @Test
    public void SM_Identity() {
        ms = new ShadowMatrix();

        identityTest(ms, new ShadowMatrix());
    }

    private void identityTest(MatrixStack ms, MatrixStack newMatrix) {
        ms.pushMatrix();
        {
            ms.rotate(5, 3, 2, 5);
            ms.scale(9, 8, 4);
            ms.translate(9, 3, 5);
        }
        ms.popMatrix();

        assert ms.toString().equals(newMatrix.toString()) : ms.toString();
    }

    @Test
    public void SM_getPositionTest() {
        ms = new ShadowMatrix();

        testTRSInstance(ms);
    }

    @Test
    public void SM_InversePositionTest() {
        ShadowMatrix sm = new ShadowMatrix();

        sm.translate(2, 2, 2);
        sm.rotate(new DirVector(1, 0, 0), (float) Math.toRadians(90));
        sm.scale(2);

        final PosVector target = new PosVector(3, 3, 3);
        PosVector answer = sm.mapToLocal(target);
        PosVector expected = new PosVector(0.5f, 0.5f, -0.5f);

        assert answer.toString().equals(expected.toString()) : "querying " + target + " resulted in " + answer + " while it should be " + expected;
    }

    @Test
    public void SM_InverseGivesIdentityTest() {
        ShadowMatrix sm = new ShadowMatrix();

        sm.translate(2, 2, 2);
        sm.rotate(new DirVector(1, 0, 0), (float) Math.toRadians(90));
        sm.scale(2);

        final PosVector target = new PosVector(4.75f, 3.5f, 2.25f);
        PosVector answer = sm.getPosition(sm.mapToLocal(target));

        assert answer.toString().equals(target.toString()) : "mapping " + target + " forth and back gives " + answer;
    }

    @Test
    public void MatrixGetPositionTest() throws IOException {
        new GLFWWindow("test");
        ms = new ShaderUniformGL(new GouraudShader(), 1, 1, new SimpleKeyCamera());

        testTRSInstance(ms);
    }

    @Test
    public void MatrixGetIdentityTest() throws IOException {
        new GLFWWindow("test");
        ms = new ShaderUniformGL(new GouraudShader(), 1, 1, new SimpleKeyCamera());

        identityTest(ms, new ShaderUniformGL(new GouraudShader(), 1, 1, new SimpleKeyCamera()));
    }

    private void testTRSInstance(MatrixStack ms) {
        ms.translate(2, 2, 2);
        ms.rotate(new DirVector(1, 0, 0), (float) Math.toRadians(90));
        ms.scale(2);

        testPosition(new PosVector(0, 0, 0), new PosVector(2, 2, 2));
        testPosition(new PosVector(0, 0, 1), new PosVector(2, 0, 2));
        testDirection(new DirVector(1, 0, 1), new DirVector(2, -2, 0));
    }

    private void testDirection(DirVector target, DirVector expected){
        DirVector answer = ms.getDirection(target);
        System.out.println(answer);
        assert answer.toString().equals(expected.toString()) : "querying " + target + " resulted in " + answer + " while it should be " + expected;
    }

    private void testPosition(PosVector target, PosVector expected) {
        PosVector answer = ms.getPosition(target);
        System.out.println(answer);
        assert answer.toString().equals(expected.toString()) : "querying " + target + " resulted in " + answer + " while it should be " + expected;
    }
}