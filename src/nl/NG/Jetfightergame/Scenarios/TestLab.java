package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.GameObjects.Structures.CustomShape;
import nl.NG.Jetfightergame.GameObjects.Structures.Shape;
import nl.NG.Jetfightergame.GameObjects.Touchable;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 *         created on 8-11-2017.
 */
public class TestLab implements Touchable {
    public static final int SIZE = 100;
    private final Shape world = createBox();
    private final Material material = Material.PLASTIC;

    public TestLab() {
        super();
    }

    private static Shape createBox(){
        CustomShape frame = new CustomShape();

        PosVector PPP = new PosVector(SIZE, SIZE, SIZE);
        PosVector PPN = new PosVector(SIZE, SIZE, -SIZE);
        PosVector PNP = new PosVector(SIZE, -SIZE, SIZE);
        PosVector PNN = new PosVector(SIZE, -SIZE, -SIZE);
        PosVector NPP = new PosVector(-SIZE, SIZE, SIZE);
        PosVector NPN = new PosVector(-SIZE, SIZE, -SIZE);
        PosVector NNP = new PosVector(-SIZE, -SIZE, SIZE);
        PosVector NNN = new PosVector(-SIZE, -SIZE, -SIZE);

        frame.addQuad(PPP, PPN, PNN, PNP, new DirVector(-1, 0, 0));
        frame.addQuad(PPN, NPN, NNN, PNN, new DirVector(0, 0, 1));
        frame.addQuad(NPN, NPP, NNP, NNN, new DirVector(1, 0, 0));
        frame.addQuad(NPP, PPP, PNP, NNP, new DirVector(0, 0, -1));
        frame.addQuad(PPP, PPN, NPN, NPP, new DirVector(0, -1, 0));
        frame.addQuad(PNP, PNN, NNN, NNP, new DirVector(0, 1, 0));

        return frame.wrapUp();
    }

    @Override
    public void draw(GL2 gl) {
        gl.setMaterial(material, Toolbox.COLOR_WHITE);
        // skip a bunch of references
        gl.draw(world);
    }

    @Override
    public void preDraw(GL2 gl) {

    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action, boolean takeStable) {
        // just accept the world bruh
        action.accept(world);
    }

    @Override
    public void toLocalSpace(MatrixStack ms, Runnable action, boolean takeStable) {
        action.run();
    }

    @Override
    public void checkCollisionWith(Touchable other) {
        // Yup, gay.
    }

    @Override
    public String toString() {
        return "Testlab {" +
                "size: " + SIZE + "}";
    }
}
