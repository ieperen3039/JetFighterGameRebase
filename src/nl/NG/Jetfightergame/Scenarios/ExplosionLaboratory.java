package nl.NG.Jetfightergame.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GameState;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.FighterJets.PlayerJet;
import nl.NG.Jetfightergame.GeneralEntities.SimpleBullet;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.Color4f;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;
import org.joml.Quaternionf;

/**
 * @author Geert van Ieperen
 * created on 12-1-2018.
 */
public class ExplosionLaboratory extends GameState {

    public ExplosionLaboratory(GameTimer time) {
        super(time);
    }

    @Override
    public void buildScene(PlayerJet player) {
        Settings.SPECTATOR_MODE = true;

        player.set();

        dynamicEntities.add(player);
        for (int i = 0; i < 20; i++) {
            final PosVector pos = Vector.random().toPosVector();
            pos.mul((3 * i) + 20);
            final DirVector vel = new DirVector(0, 1, 0);
            pos.negate(vel).add(DirVector.random(), vel);
            dynamicEntities.add(
                    new SimpleBullet(pos, vel.reducedTo(10, vel), new Quaternionf(), getTimer())
            );
        }

        lights.add(new Pair<>(new PosVector(0, 0, 10), Color4f.RED));
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
