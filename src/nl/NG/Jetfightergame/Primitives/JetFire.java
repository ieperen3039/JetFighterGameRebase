package nl.NG.Jetfightergame.Primitives;

import nl.NG.Jetfightergame.Primitives.Surfaces.Plane;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.Renderable;
import nl.NG.Jetfightergame.Tools.Updatable;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Arrays;

/**
 * @author Geert van Ieperen
 * created on 8-2-2018.
 */
public class JetFire implements Updatable, Renderable {
    private final Plane plane;
    private final Flame[] sparks; // optionally use 4 arrays for maximum efficiency
    private final float power;

    public JetFire(Plane sourcePlane) {
        this(sourcePlane, 10, 5f);
    }

    public JetFire(Plane sourcePlane, int flameCount, float power) {
        this.plane = sourcePlane;
        this.sparks = new Flame[flameCount];
        this.power = power;
        PosVector middle = plane.getMiddle();
        Arrays.fill(sparks, new Flame(middle, plane.getNormal()));
    }

    public void update(float deltaTime){
        PosVector middle = plane.getMiddle();

        for (int i = 0; i < sparks.length; i++) {
            sparks[i].update(middle, deltaTime);
            if (sparks[i].isGone()) sparks[i] = new Flame(middle, plane.getNormal());
        }
    }

    @Override
    public void render(GL2.Painter lock) {

    }

    @Override
    public void dispose() {

    }

    private class Flame {
        private float timeToLive;
        private PosVector point;

        public Flame(PosVector middle, DirVector normal) {
            point = new PosVector();
            DirVector offSet = DirVector.random();
            offSet.add(normal).mul(0.1f);
            middle.add(offSet, point);

            timeToLive = 1/(offSet.dot(normal) + 0.1f);
        }

        public void update(PosVector source, float deltaTime) {
            DirVector dir = source.to(point, new DirVector());
            dir.reducedTo(power * deltaTime, dir);
            point.add(dir);
        }

        public boolean isGone(){
            return timeToLive < 0;
        }
    }
}
