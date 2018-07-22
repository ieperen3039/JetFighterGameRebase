package nl.NG.Jetfightergame.Assets.Entities.Projectiles;

import nl.NG.Jetfightergame.AbstractEntities.AbstractProjectile;
import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.ArtificalIntelligence.RocketAI;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.joml.Quaternionf;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen. Created on 17-7-2018.
 */
public class Seeker extends AbstractProjectile {
    public static final String TYPE = "Seeker";

    public static final int THRUST_POWER = 100;
    public static final float TURN_ACC = 0.2f;
    public static final float AIR_RESIST = 0.001f;
    public static final float TIME_TO_LIVE = 3f;
    public static final float MASS = 1f;
    public static final Color4f COLOR_1 = new Color4f(0.1f, 0, 0);
    public static final Color4f COLOR_2 = new Color4f(0.6f, 0.1f, 0.1f);
    public static final int NOF_PARTICLES = 5;
    public static final float PARTICLE_SIZE = 0.2f;
    public static final float EXPLOSION_CLOUD_POWER = 10f;
    public static final float POWER = 1f;
    private Touchable sourceJet;

    /**
     * enables the use of 'Seeker'
     */
    public static void init() {
        addConstructor(TYPE, (id, position, rotation, velocity, game) ->
                new Seeker(id, position, rotation, velocity, game.getTimer(), game));
    }

    private Seeker(int id, PosVector position, Quaternionf rotation, DirVector velocity, GameTimer timer, SpawnReceiver game) {
        super(
                id, position, rotation, velocity,
                1f, MASS, Material.GLOWING, AIR_RESIST, TIME_TO_LIVE, TURN_ACC, THRUST_POWER,
                game, timer
        );
    }

    public void setTarget(MovingEntity target) {
        setController(new RocketAI(this, target, THRUST_POWER / AIR_RESIST));
    }

    @Override
    public ParticleCloud explode() {
        PosVector position = interpolatedPosition();
        ParticleCloud result = new ParticleCloud();

        for (int i = 0; i < NOF_PARTICLES; i++) {
            Color4f color = Color4f.randomBetween(COLOR_1, COLOR_2);

            final float randFloat = Toolbox.random.nextFloat();
            final DirVector random = DirVector.randomOrb();

            final float rotationSpeed = 2 + (2 / randFloat);
            random.mul(EXPLOSION_CLOUD_POWER * randFloat);

            // random positions
            PosVector A = Vector.random().scale(PARTICLE_SIZE).toPosVector();
            PosVector B = Vector.random().scale(PARTICLE_SIZE).toPosVector();
            PosVector C = A.add(B, new PosVector()).scale(-0.5f); // C = -1 * (A + B)/2

            A.add(position);
            B.add(position);
            C.add(position);

            result.addParticle(A, B, C, DirVector.zeroVector(), random, color, rotationSpeed, TIME_TO_LIVE);
        }
        return result;
    }

    @Override
    protected void collideWithOther(Touchable other, Collision collision) {
        if (other != sourceJet) {
            timeToLive = 0;
            other.impact(POWER);
        }
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        //TODO create shape
        action.accept(GeneralShapes.ARROW);
    }

    public void setSource(Touchable sourceJet) {
        this.sourceJet = sourceJet;
    }
}