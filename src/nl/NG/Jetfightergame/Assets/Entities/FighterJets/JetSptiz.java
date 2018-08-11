package nl.NG.Jetfightergame.Assets.Entities.FighterJets;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.EntityMapping;
import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityClass;
import nl.NG.Jetfightergame.AbstractEntities.Factories.EntityFactory;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Assets.Shapes.CustomJetShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Particles.BoosterLine;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.DataStructures.PairList;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen. Created on 11-8-2018.
 */
public class JetSptiz extends AbstractJet {

    public static final float LIFT_FACTOR = 1f;
    public static final float THROTTLE_POWER = 800f;
    public static final float BRAKE_POWER = 3f; // air resist is multiplied with this
    public static final float MASS = 40f;
    public static final Material MATERIAL = Material.SILVER;
    public static final float YAW_POWER = 2f;
    public static final float PITCH_POWER = 3f;
    public static final float ROLL_POWER = 3f;
    public static final float AIR_RESISTANCE_COEFFICIENT = 0.1f;
    private static final Color4f THRUST_COLOR_1 = Color4f.ORANGE;
    private static final Color4f THRUST_COLOR_2 = Color4f.WHITE;

    private final PosVector shapeMiddle;
    private final float shapeRange;
    private List<BoosterLine> nuzzle;

    private JetSptiz(
            int id, PosVector initialPosition, Quaternionf initialRotation, GameTimer renderTimer,
            SpawnReceiver entityDeposit, EntityMapping entities
    ) {
        super(
                id, initialPosition, initialRotation, 0.5f,
                MATERIAL, MASS, LIFT_FACTOR, AIR_RESISTANCE_COEFFICIENT, THROTTLE_POWER, BRAKE_POWER,
                YAW_POWER, PITCH_POWER, ROLL_POWER,
                0.7f, renderTimer, 0.3f, 0.5f, entityDeposit, entities
        );

        Pair<PosVector, Float> minimalCircle = CustomJetShapes.SPITZ.getMinimalCircle();
        shapeMiddle = minimalCircle.left;
        shapeRange = minimalCircle.right;

        PairList<PosVector, PosVector> boosters = CustomJetShapes.spitzBoosters;
        float pps = ClientSettings.THRUST_PARTICLES_PER_SECOND / boosters.size();

        nuzzle = new ArrayList<>(boosters.size());
        for (int i = 0; i < boosters.size(); i++) {
            nuzzle.add(new BoosterLine(
                    boosters.left(i), boosters.right(i),
                    DirVector.zeroVector(), pps, ClientSettings.THRUST_PARTICLE_LINGER_TIME,
                    THRUST_COLOR_1, THRUST_COLOR_2, ClientSettings.THRUST_PARTICLE_SIZE
            ));
        }
    }

    @Override
    public PosVector getPilotEyePosition() {
        return new PosVector(3, 0, 0.5f);
    }

    @Override
    protected void updateShape(float deltaTime) {

    }

    @Override
    public void draw(GL2 gl) {
        super.draw(gl);

        DirVector back = new DirVector();
        DirVector forward = getForward();
        forward.negate(back).reducedTo(controller.throttle(), back).add(forward);
        float deltaTime = gameTimer.getRenderTime().difference();

        PairList<PosVector, PosVector> boosters = CustomJetShapes.spitzBoosters;
        for (int i = 0; i < boosters.size(); i++) {
            PosVector aNew = relativeInterpolatedDirection(boosters.left(i).toDirVector()).toPosVector();
            PosVector bNew = relativeInterpolatedDirection(boosters.right(i).toDirVector()).toPosVector();
            nuzzle.get(i).update(aNew, bNew, back, deltaTime);
        }
    }

    @Override
    public EntityFactory getFactory() {
        return new Factory(this);
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        action.accept(CustomJetShapes.SPITZ);
    }

    @Override
    public float getRange() {
        return shapeRange;
    }

    @Override
    public PosVector getExpectedMiddle() {
        return extraPosition.add(shapeMiddle, new PosVector());
    }

    public static class Factory extends EntityFactory {
        public Factory() {
            super();
        }

        public Factory(PosVector position, DirVector direction, DirVector velocity) {
            super(EntityClass.JET_SPITZ, position, Toolbox.xTo(direction), velocity);
        }

        public Factory(JetSptiz jet) {
            super(EntityClass.JET_SPITZ, jet);
        }

        @Override
        public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
            return new JetSptiz(id, position, rotation, game.getTimer(), game, entities);
        }
    }
}
