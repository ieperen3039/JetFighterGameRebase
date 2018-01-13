package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GameState;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.Primitives.Particles.Particles;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

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
//        PlayerJet target = new PlayerJet(new Controller.EmptyController(), getTimer().getRenderTime());
//        particles.addAll(target.explode(1f));
        Particle p = Particles.generateParticle(
                PosVector.zVector(), PosVector.zeroVector(), new PosVector(1, 0, 1), DirVector.random(), 1000
        );
        particles.add(p);
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
