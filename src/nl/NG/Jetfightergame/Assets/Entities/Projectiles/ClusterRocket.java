package nl.NG.Jetfightergame.Assets.Entities.Projectiles;

import nl.NG.Jetfightergame.AbstractEntities.AbstractProjectile;
import nl.NG.Jetfightergame.AbstractEntities.EntityState;
import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityClass;
import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityFactory;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.ArtificalIntelligence.RocketAI;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Particles.BoosterLine;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.Rendering.Particles.Particles;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.PairList;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

import static nl.NG.Jetfightergame.Settings.ClientSettings.EXPLOSION_COLOR_1;
import static nl.NG.Jetfightergame.Settings.ClientSettings.EXPLOSION_COLOR_2;

/**
 * AKA the BUK rocket
 * @author Geert van Ieperen. Created on 24-7-2018.
 */
public class ClusterRocket extends AbstractProjectile {

    public static final int NOF_PELLETS_LAUNCHED = 25;
    public static final float EXPLOSION_POWER = 10f;
    public static final int EXPLOSION_DENSITY = 2000;
    public static final float THRUST_POWER = 400f;
    public static final float TIME_TO_LIVE = 30f;
    public static final float SHOOT_ACCURACY = 0.3f;
    public static final float TURN_ACC = 0.4f;
    public static final float ROLL_ACC = 0.1f;
    public static final float AIR_RESIST = 0.01f;
    public static final float MASS = 10f;
    public static final float THRUST_PARTICLE_PER_SECOND = 10;
    private boolean hasExploded = false;
    private BoosterLine nuzzle;

    /**
     * @param id              unique identifier for this entity
     * @param initialPosition position of spawning (of the origin) in world coordinates
     * @param initialRotation the initial rotation of spawning
     * @param initialVelocity the initial velocity, that is the vector of movement per second in world-space
     * @param entityDeposit   particles are passed here
     * @param gameTimer       the timer that determines the "current rendering time" for {@link
     *                        MovingEntity#interpolatedPosition()}
     * @param sourceEntity    the entity that launched this projectile
     * @param tgt             the target of this bomb
     */
    private ClusterRocket(
            int id, PosVector initialPosition, Quaternionf initialRotation, DirVector initialVelocity,
            SpawnReceiver entityDeposit, GameTimer gameTimer, MovingEntity sourceEntity, MovingEntity tgt
    ) {
        super(
                id, initialPosition, initialRotation, initialVelocity, MASS, Material.GOLD,
                AIR_RESIST, TIME_TO_LIVE, TURN_ACC, ROLL_ACC, THRUST_POWER,
                0.2f, entityDeposit, gameTimer, sourceEntity
        );

        if (tgt != null) {
            this.target = tgt;

            RocketAI con = new RocketAI(this, tgt, 500f, 20f) {
                @Override
                public boolean primaryFire() {
                    return super.primaryFire() && velocity.dot(vecToTarget) > (1 - SHOOT_ACCURACY);
                }
            };
            con.update();
            setController(con);
        }

        PosVector pos = getPosition();
        nuzzle = new BoosterLine(
                pos, pos, DirVector.zeroVector(),
                THRUST_PARTICLE_PER_SECOND, ClientSettings.THRUST_PARTICLE_LINGER_TIME, Color4f.ORANGE, Color4f.RED, ClientSettings.THRUST_PARTICLE_SIZE
        );
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        ms.pushMatrix();
        {
            ms.translate(-0.5f, 0, 0);
            ms.rotate((float) Math.toRadians(90), 0f, 1f, 0f);
            action.accept(GeneralShapes.ARROW);
        }
        ms.popMatrix();
    }

    @Override
    public void draw(GL2 gl) {
        super.draw(gl);

        float deltaTime = gameTimer.getRenderTime().difference();
        PosVector pos = getPosition();
        DirVector back = new DirVector();
        forward.negate(back).reducedTo(controller.throttle(), back).add(forward);

        ParticleCloud cloud = nuzzle.update(pos, pos, back, deltaTime);
        entityDeposit.addParticles(cloud);
    }

    @Override
    public ParticleCloud explode() {
//        new AudioSource(Sounds.explosion, position, 1f, 1f);
        return Particles.explosion(
                interpolatedPosition(), DirVector.zeroVector(),
                EXPLOSION_COLOR_1, EXPLOSION_COLOR_2, EXPLOSION_POWER, EXPLOSION_DENSITY, Particles.FIRE_LINGER_TIME, 0.1f
        );
    }

    @Override
    protected void updateShape(float deltaTime) {
        if (!hasExploded && controller.primaryFire()) {
            timeToLive = 0;
            entityDeposit.addSpawns(AbstractProjectile.createCloud(
                    this, NOF_PELLETS_LAUNCHED, EXPLOSION_POWER,
                    SimpleBullet.Factory::new
            ));
            hasExploded = true;
        }
        timeToLive -= deltaTime;
        // sparkles
    }

    @Override
    public boolean isOverdue() {
        return hasExploded || super.isOverdue();
    }

    @Override
    protected PairList<PosVector, PosVector> calculateHitpointMovement() {
        PairList<PosVector, PosVector> pairs = new PairList<>(1);
        pairs.add(position, extraPosition);
        return pairs;
    }

    @Override
    public EntityFactory getFactory() {
        return new Factory(this);
    }

    @Override
    public float getRange() {
        return 0;
    }

    @Override
    protected void collideWithOther(Touchable other) {
        other.impact(IMPACT_POWER);
    }

    public static class Factory extends RocketFactory {
        public Factory() {
        }

        public Factory(EntityState state, MovingEntity source, MovingEntity target) {
            super(EntityClass.CLUSTER_ROCKET, state, 0, source, target);
        }

        public Factory(ClusterRocket rocket) {
            super(EntityClass.CLUSTER_ROCKET, rocket);
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, MovingEntity src, MovingEntity tgt) {
            return new ClusterRocket(id, position, rotation, velocity, game, game.getTimer(), src, tgt);
        }
    }
}
