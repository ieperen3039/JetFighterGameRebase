package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GameState;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.Primitives.Particles.TriangleParticle;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen
 * created on 12-1-2018.
 */
public class ExplosionLaboratory extends GameState {

    public ExplosionLaboratory(Controller input) {
        super(input);
    }

    @Override
    public void buildScene() {
        Settings.SPECTATOR_MODE = true;
//        PlayerJet target = new PlayerJet(new Controller.EmptyController(), getTimer().getRenderTime());
//        particles.addAll(target.explode(1f));
        Particle p = new TriangleParticle(
                new PosVector(1, 0, 0), new PosVector(0, 1, 0), new PosVector(0, 0, 1),
                PosVector.zeroVector(), DirVector.zeroVector(), new Vector3f(), 0, 100,
                Color4f.GREEN);
        particles.add(p);

        lights.add(new Pair<>(new PosVector(0, 0, 3), Color4f.BLUE));
        lights.add(new Pair<>(new PosVector(3, 3, 3), Color4f.RED));
    }

    @Override
    public void drawObjects(GL2 gl) {
        Toolbox.drawAxisFrame(gl);
        super.drawObjects(gl);
    }

    @Override
    protected DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }
}
