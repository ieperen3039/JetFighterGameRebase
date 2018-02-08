package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Tools.MatrixStack.GL2;
import nl.NG.Jetfightergame.Engine.GameState;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Settings;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 * created on 12-1-2018.
 */
public class ExplosionLaboratory extends GameState {

    private Toolbox.DelayedAction action;

    public ExplosionLaboratory(GameTimer time) {
        super(time);
    }

    @Override
    public void buildScene(Player player) {
        Settings.SPECTATOR_MODE = true;

        final AbstractJet playerJet = player.jet();
        playerJet.set();

        action = new Toolbox.DelayedAction(1000, () -> {
            Toolbox.print("BOOM");
            particles.addAll(playerJet.explode());
            dynamicEntities.remove(playerJet);
        });

        dynamicEntities.add(playerJet);
//        for (int i = 0; i < 20; i++) {
//            final PosVector pos = Vector.random().toPosVector();
//            pos.mul((3 * i) + 20);
//            final DirVector vel = new DirVector(0, 1, 0);
//            pos.negate(vel).add(DirVector.random(), vel);
//            dynamicEntities.add(
//                    new SimpleBullet(pos, vel.reducedTo(10, vel), new Quaternionf(), getTimer())
//            );
//        }

        lights.add(new Pair<>(new PosVector(0, 0, 10), Color4f.RED));
    }

    @Override
    public void drawObjects(GL2 gl) {
        Toolbox.drawAxisFrame(gl);
        super.drawObjects(gl);
    }

    @Override
    public void cleanUp() {
        action.cancel();
        super.cleanUp();
    }

    @Override
    protected DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }
}
