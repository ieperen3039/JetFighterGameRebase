package nl.NG.Jetfightergame.ScreenOverlay.HUD;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.joml.Vector2i;

import java.util.function.Consumer;

/**
 * @author Geert van Ieperen
 * created on 18-2-2018.
 */
public abstract class MovingTarget implements HUDTargetable {
    protected final MovingEntity subject;
    private final ScreenOverlay hud;
    private Consumer<ScreenOverlay.Painter> visual;

    /**
     * create a targeting wrapper for the HUD to target entities.
     * @param subject the entity aimed at
     * @param hud
     *
     */
    protected MovingTarget(MovingEntity subject, ScreenOverlay hud) {
        this.subject = subject;
        this.hud = hud;
        hud.addHudItem(visual);
        visual = this::draw;
    }

    /**
     * @return the coordinate of this vertex on the screen, or null if this vertex is behind the player
     */
    protected Vector2i entityPosition(ScreenOverlay.Painter hud){
        return hud.positionOnScreen(subject.interpolatedPosition());
    }

    protected int iconSize(PosVector cameraPos){
        float dist = distance(cameraPos);
        if (dist < 25) return HUDStyleSettings.ICON_BIG;
        else if (dist < 100) return HUDStyleSettings.ICON_MED;
        else return HUDStyleSettings.ICON_SMALL;
    }

    protected float distance(PosVector cameraPos){
        Vector distance = cameraPos.to(subject.interpolatedPosition(), new DirVector());
        return distance.length();
    }

    protected String targetName(){
        return subject.toString();
    }

    protected abstract void draw(ScreenOverlay.Painter hud);

    @Override
    public void dispose(){
        hud.removeHudItem(visual);
    }
}
