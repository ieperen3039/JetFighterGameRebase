package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 19-6-2018.
 */
public class ElasticSail {
    private static final int RELAXATION_CYCLES = 10;
    private Map<PosVector, PosVector[]> points;

    private ElasticSail(int expectedSize) {
        // equality of pointers
        points = new IdentityHashMap<>(expectedSize);
    }

    /** adds a point to the collection */
    public void add(PosVector point) {
        points.putIfAbsent(point, new PosVector[]{new PosVector(point)});
    }

    /**
     * adds all planes of the given shape to the collection. Borders are added only in the direction provided by {@link
     * Plane#getBorder()} Holes must be separately added by linking (but this is often not desired)
     */
    public void addShape(Shape target) {
        for (Plane plane : target.getPlanes()) {
            Iterator<PosVector> vertices = plane.getBorder().iterator();
            PosVector last = vertices.next();

            while (vertices.hasNext()) {
                PosVector vertex = vertices.next();
                linkOneWay(last, vertex);
                last = vertex;
            }
        }
    }

    public void addGrid(PosVector[][] grid) {
        for (int x = 0; x < grid.length; x++) {
            PosVector[] rows = grid[x];
            for (int y = 0; y < rows.length; y++) {
                PosVector point = rows[y];

                add(point);
                // link to all available directions
                if (y > 0) linkOneWay(point, rows[y - 1]);
                if ((y + 1) < rows.length) linkOneWay(point, rows[y + 1]);
                if (x > 0) linkOneWay(point, grid[x - 1][y]);
                if ((x + 1) < grid.length) linkOneWay(point, grid[x + 1][y]);
            }
        }
    }

    /**
     * hooks two points together.
     * @see #linkOneWay(PosVector, PosVector)
     */
    public void link(PosVector A, PosVector B) {
        linkOneWay(A, B);
        linkOneWay(B, A);
    }

    /**
     * hooks point A to B, but not the other way around. Point A will move to the average of its original point and all
     * linked points
     */
    public void linkOneWay(PosVector A, PosVector B) {
        add(A);
        PosVector[] links = points.get(A); // Maybe make this a set
        int n = links.length;
        links = Arrays.copyOf(links, n + 1);
        links[n] = B;
        points.put(A, links);
    }

    /** repeatedly relax the given points, moving the linked points together */
    public void process() {
        for (int i = 0; i < RELAXATION_CYCLES; i++) {
            points.forEach(ElasticSail::relax);
        }
    }

    private static void relax(PosVector point, PosVector[] pulls) {
        int n = pulls.length;
        if (n == 1) return;

        float x = 0;
        float y = 0;
        float z = 0;

        for (PosVector pull : pulls) {
            x += pull.x;
            y += pull.y;
            z += pull.z;
        }

        x /= n;
        y /= n;
        z /= n;

        point.set(x, y, z);
    }
}
