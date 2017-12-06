package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GLMatrix.MatrixStack;
import nl.NG.Jetfightergame.GameObjects.Structures.GeneralShapes;
import nl.NG.Jetfightergame.GameObjects.Structures.Shape;
import nl.NG.Jetfightergame.GameObjects.Touchable;
import nl.NG.Jetfightergame.Shaders.Material;

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
        world = GeneralShapes.INVERSE_CUBE;
    }

    @Override
    public void draw(GL2 gl) {
        gl.setMaterial(material);
        // skip a bunch of references
        gl.scale(labSize);
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
