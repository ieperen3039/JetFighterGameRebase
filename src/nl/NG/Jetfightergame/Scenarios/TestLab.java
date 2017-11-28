package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.GameObjects.Structures.CustomShape;
import nl.NG.Jetfightergame.GameObjects.Structures.Shape;
import nl.NG.Jetfightergame.GameObjects.Touchable;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 *         created on 8-11-2017.
 */
public class TestLab implements Touchable {
    public final int labSize;
    private final Shape world;
    private final Material material = Material.PLASTIC;

    public TestLab(int labSize) {
        super();
        this.labSize = labSize;
        world = createBox(labSize);
    }

    private static Shape createBox(int size){
        CustomShape frame = new CustomShape();

        PosVector PPP = new PosVector(size, size, size);
        PosVector PPN = new PosVector(size, size, -size);
        PosVector PNP = new PosVector(size, -size, size);
        PosVector PNN = new PosVector(size, -size, -size);
        PosVector NPP = new PosVector(-size, size, size);
        PosVector NPN = new PosVector(-size, size, -size);
        PosVector NNP = new PosVector(-size, -size, size);
        PosVector NNN = new PosVector(-size, -size, -size);

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
        gl.setMaterial(material);
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
    public String toString() {
        return "Testlab {" +
                "size: " + labSize + "}";
    }
}
