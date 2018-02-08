package nl.NG.Jetfightergame.ShapeCreation;

import nl.NG.Jetfightergame.Tools.MatrixStack.GL2;
import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * a shape with a model that differs from the actual hitbox
 * @author Geert van Ieperen
 * created on 1-2-2018.
 */
public class MultiShape implements Shape {

    private final Shape model;
    private final Shape hitbox;

    public MultiShape(Shape model, Shape hitbox) {
        this.model = model;
        this.hitbox = hitbox;
    }

    @Override
    public Stream<? extends Plane> getPlanes() {
        return hitbox.getPlanes();
    }

    @Override
    public Collection<PosVector> getPoints() {
        return hitbox.getPoints();
    }

    @Override
    public void render(GL2.Painter lock) {
        model.render(lock);
    }
}
