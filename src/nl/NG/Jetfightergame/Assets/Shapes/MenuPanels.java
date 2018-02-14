package nl.NG.Jetfightergame.Assets.Shapes;

import nl.NG.Jetfightergame.ShapeCreation.CustomShape;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Geert van Ieperen
 * created on 13-2-2018.
 */
public final class MenuPanels {

    private static final float INNER = 0.5f;
    private static Map<Integer, Shape> cache = new HashMap<>();

    /**
     * @param parts how many parts there are in total. If you want 1/6th of a circle, parts = 6
     * @return a shape on the yz plane, that extends to z=1, with front towards positive x.
     */
    @SuppressWarnings("SuspiciousNameCombination")
    public static Shape get(int parts){
        if (cache.containsKey(parts)) return cache.get(parts);

        CustomShape frame = new CustomShape(new PosVector(-1, 0, 0));

        double t = (2 * Math.PI) / parts;
        float x = (float) Math.cos(t);
        float y = (float) Math.sin(t);

        frame.addQuad(
                new PosVector(0, 0, 1f),
                new PosVector(0, 0, INNER),
                new PosVector(0, x * INNER, y * INNER),
                new PosVector(0, x, y)
        );

        final Shape result = frame.wrapUp();
        cache.put(parts, result);

        return result;
    }
}
