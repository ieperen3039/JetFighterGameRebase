package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedFloat;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * refers to the entity as visible in the world
 * @author Geert van Ieperen. Created on 9-7-2018.
 */
public class PowerupEntity implements Touchable {
    private static final float DESPAWN_TIME = 0.5f;
    private final PowerupType.Primitive type;
    private PosVector position;
    private RaceProgress race;
    private boolean isOverdue = false;
    private float scaling = 10f;
    private final GameTimer timer;
    private Shape shape;

    /**
     * creates a collectible power-up on the given fixed position in the world
     * @param type     type of this powerup
     * @param position position in the world
     * @param race     the current race, to provide players
     * @param timer    the timer (used for rendering animations)
     */
    public PowerupEntity(PowerupType.Primitive type, PosVector position, RaceProgress race, GameTimer timer) {
        this.type = type;
        this.position = position;
        this.race = race;
        this.timer = timer;
        shape = GeneralShapes.CUBE;
    }

    @Override
    public void create(MatrixStack ms, Consumer<Shape> action) {
        if (isOverdue) return;
        ms.scale(10);
        action.accept(GeneralShapes.CUBE);
    }

    @Override
    public void draw(GL2 gl) {
        // TODO model and animation
        if (scaling == 0) return;
        preDraw(gl);
        gl.pushMatrix();
        {
            gl.translate(position);

            TrackedFloat renderTime = timer.getRenderTime();
            float time = renderTime.current();
            gl.rotate(DirVector.zVector(), time);

            if (isOverdue) {
                float dTime = renderTime.difference();
                scaling = Math.max(0, scaling - (dTime / DESPAWN_TIME));
                gl.scale(scaling);
            }

            gl.draw(shape);
        }
        gl.popMatrix();
    }

    @Override
    public void toLocalSpace(MatrixStack ms, Runnable action) {
        ms.pushMatrix();
        ms.translate(position);
        action.run();
        ms.popMatrix();
    }

    @Override
    public void preDraw(GL2 gl) {
        gl.setMaterial(Material.GLOWING, type.color);
    }

    @Override
    public void acceptCollision(Collision cause) {
        if (isOverdue) return;

        MovingEntity source = cause.source();
        if (source instanceof AbstractJet) {
            Collection<Player> players = race.players();

            for (Player player : players) {
                if (player.jet() == source) {
                    // if the player accepts it, remove from field
                    isOverdue = player.addPowerup(type);
                    return;
                }
            }
        }
    }

    @Override
    public float getRange() {
        return isOverdue ? 0 : ServerSettings.POWERUP_COLLECTION_RANGE;
    }

    @Override
    public PosVector getExpectedMiddle() {
        return new PosVector(position);
    }
}
