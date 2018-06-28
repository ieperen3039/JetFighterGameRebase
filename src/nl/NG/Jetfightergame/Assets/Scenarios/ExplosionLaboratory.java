package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Weapons.SimpleBullet;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Identity;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
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

    public ExplosionLaboratory() {
        ClientSettings.SPECTATOR_MODE = true;
    }

    @Override
    protected Collection<Touchable> createWorld() {
        lights.add(new Pair<>(new PosVector(0, 0, 10), Color4f.RED));
        return Collections.EMPTY_LIST;
    }

    @Override
    protected Collection<MovingEntity> setEntities(SpawnReceiver deposit) {
        Collection<MovingEntity> dynamicEntities = new ArrayList<>(21);

        for (int i = 0; i < 20; i++) {
            final PosVector pos = Vector.random().toPosVector();
            pos.mul((3 * i) + 20);
            final DirVector vel = new DirVector(0, 1, 0);
            pos.negate(vel).add(DirVector.random(), vel);
            dynamicEntities.add(
                    new SimpleBullet(Identity.next(), pos, vel.reducedTo(10, vel), new Quaternionf(), deposit.getTimer(), deposit)
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
