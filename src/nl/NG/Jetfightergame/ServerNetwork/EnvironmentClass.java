package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Spawn;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Scenarios.IslandMap;
import nl.NG.Jetfightergame.Assets.Scenarios.PlayerJetLaboratory;
import nl.NG.Jetfightergame.GameState.GameState;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Geert van Ieperen. Created on 4-7-2018.
 */
public enum EnvironmentClass {
    LOBBY,
    PLAYERJET_LABORATORY, ISLAND_MAP;

    private static final EnvironmentClass[] VALUES = values();

    public GameState create() {
        switch (this) {
            case LOBBY:
                return new PlayerJetLaboratory();
            case PLAYERJET_LABORATORY:
                return new PlayerJetLaboratory();
            case ISLAND_MAP:
                return new IslandMap();
            default:
                return new Void();
        }
    }

    public static EnvironmentClass get(int id) {
        if (id >= VALUES.length) throw new IllegalArgumentException("Invalid worldclass identifier " + id);
        else return VALUES[id];
    }

    /**
     * public void... :D
     */
    public static class Void extends GameState {

        @Override
        public MovingEntity.State getNewSpawnPosition() {
            return new MovingEntity.State();
        }

        @Override
        protected Collection<Touchable> createWorld(RaceProgress raceProgress) {
            return Collections.EMPTY_SET;
        }

        @Override
        protected Collection<Spawn> getInitialEntities() {
            return Collections.EMPTY_SET;
        }

        @Override
        public void updateGameLoop(float currentTime, float deltaTime) {
        }

        @Override
        public void setLights(GL2 gl) {
        }

        @Override
        public void drawObjects(GL2 gl) {
        }

        @Override
        public void drawParticles(float currentTime) {
        }

        @Override
        public int getParticleCount(float currentTime) {
            return 0;
        }

        @Override
        public Collection<MovingEntity> getEntities() {
            return Collections.EMPTY_SET;
        }

        @Override
        public void addEntity(MovingEntity entity) {
        }

        @Override
        public void addEntities(Collection<? extends MovingEntity> entities) {
        }

        @Override
        public void removeEntity(MovingEntity entity) {
        }

        @Override
        public void addParticles(ParticleCloud cloud) {
        }

        @Override
        public void cleanUp() {
        }

        @Override
        public Color4f fogColor() {
            return new Color4f(0, 0, 0, 0);
        }

        @Override
        public MovingEntity getEntity(int id) {
            return null;
        }

        @Override
        public PosVector getMiddleOfPath(PosVector position) {
            return PosVector.zeroVector();
        }

        @Override
        public DirVector entityNetforce(MovingEntity entity) {
            return DirVector.zeroVector();
        }
    }
}
