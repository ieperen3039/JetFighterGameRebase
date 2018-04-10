package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.GeneralEntities.SimpleBullet;
import nl.NG.Jetfightergame.Engine.GameState.GameState;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Settings.Settings;
import nl.NG.Jetfightergame.Tools.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * @author Geert van Ieperen
 * created on 12-1-2018.
 */
public class ExplosionLaboratory extends GameState {

    public ExplosionLaboratory(Player player, GameTimer time) {
        super(player, time);
        Settings.SPECTATOR_MODE = true;
    }

    @Override
    protected Collection<Touchable> createWorld() {
        lights.add(new Pair<>(new PosVector(0, 0, 10), Color4f.RED));
        return Collections.EMPTY_LIST;
    }

    @Override
    protected Collection<MovingEntity> setEntities() {

        final AbstractJet playerJet = player.jet();
        playerJet.set();

        Collection<MovingEntity> dynamicEntities = new ArrayList<>(21);
        dynamicEntities.add(playerJet);

        for (int i = 0; i < 20; i++) {
            final PosVector pos = Vector.random().toPosVector();
            pos.mul((3 * i) + 20);
            final DirVector vel = new DirVector(0, 1, 0);
            pos.negate(vel).add(DirVector.random(), vel);
            dynamicEntities.add(
                    new SimpleBullet(pos, vel.reducedTo(10, vel), new Quaternionf(), getTimer(), this)
            );
        }

        return dynamicEntities;
    }

    @Override
    public void drawObjects(GL2 gl) {
        Toolbox.drawAxisFrame(gl);
        super.drawObjects(gl);
    }

    @Override
    public void cleanUp() {
        super.cleanUp();
    }

    @Override
    public DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }

    @Override
    public Color4f fogColor(){
        return new Color4f(0.8f, 0.8f, 0.8f, 0f);
    }
}
