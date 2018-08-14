package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.Assets.Entities.SimpleRocket;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Touchable;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;

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
    protected Collection<Touchable> createWorld(RaceProgress raceProgress, GameTimer timer) {
        lights.add(new Pair<>(new PosVector(0, 0, 10), Color4f.RED));
        return Collections.EMPTY_LIST;
    }

    @Override
    protected Collection<EntityFactory> getInitialEntities() {
        Collection<EntityFactory> dynamicEntities = new ArrayList<>(20);

        for (int i = 0; i < 20; i++) {
            final PosVector pos = Vector.random().toPosVector();
            pos.mul((3 * i) + 20);
            final DirVector vel = new DirVector();
            pos.negate(vel).add(DirVector.random(), vel);

            SimpleRocket.Factory rocket = new SimpleRocket.Factory();
            rocket.set(new EntityState(pos, new DirVector(), vel.reducedTo(10, vel)), 0);

            dynamicEntities.add(rocket);
        }

        return dynamicEntities;
    }

    @Override
    public void drawObjects(GL2 gl) {
        Toolbox.drawAxisFrame(gl);
        super.drawObjects(gl);
    }

    @Override
    public DirVector entityNetforce(MovingEntity entity) {
        return DirVector.zeroVector();
    }

    @Override
    public Color4f fogColor(){
        return new Color4f(0.8f, 0.8f, 0.8f, 0f);
    }

    @Override
    public EntityState getNewSpawnPosition() {
        return new EntityState();
    }
}
