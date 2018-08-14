package nl.NG.Jetfightergame.Assets.Entities;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Rendering.Particles.ParticleCloud;
import nl.NG.Jetfightergame.ShapeCreation.Shape;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen. Created on 13-8-2018.
 */
public class BlackHoleArmor extends AbstractShield {
    public static final float TIME_TO_LIVE = 5f;

    public BlackHoleArmor(int id, AbstractJet jet, GameTimer time, float timeToLive, SpawnReceiver deposit) {
        super(id, jet, time, timeToLive, deposit);
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
    }

    @Override
    public EntityFactory getFactory() {
        return new ShieldFactory() {
            @Override
            public MovingEntity construct(SpawnReceiver game, EntityMapping entities) {
                return new BlackHoleArmor(id, jet, game.getTimer(), TIME_TO_LIVE, game);
            }
        };
    }

    @Override
    public ParticleCloud explode() {
        return null;
    }

    @Override
    public void preDraw(GL2 gl) {

    }
}
