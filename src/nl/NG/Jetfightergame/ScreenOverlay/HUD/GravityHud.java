package nl.NG.Jetfightergame.ScreenOverlay.HUD;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;

import java.util.function.Consumer;
import java.util.function.IntSupplier;

import static nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings.HUD_COLOR;
import static nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings.HUD_STROKE_WIDTH;
import static nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay.Font.LUCIDA_CONSOLE;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_RIGHT;

/**
 * @author Geert van Ieperen
 * created on 24-12-2017.
 */
public class GravityHud {

    private static final float FPV_SENSITIVITY = 50f;
    private static final float BS_SENSITIVITY = 100f;
    public final Consumer<ScreenOverlay.Painter> display;

    public GravityHud(IntSupplier windowWidth, IntSupplier windowHeight, final AbstractJet target, Camera camera) {

        display = hud -> {
            final int width = windowWidth.getAsInt();
            final int height = windowHeight.getAsInt();
            final int xMid = width / 2;
            final int yMid = height / 2;
            final float barMargin = (1 - HUDStyleSettings.ALTVEL_BAR_SIZE) / 2;
            final float inverseBarMargin = 1 - barMargin;
            final DirVector lookDirection = new DirVector(camera.vectorToFocus());
            lookDirection.normalize();

            { // velocity bar
                final int xPosVelocityBar = (int) (width * barMargin);
                hud.line(
                        HUD_STROKE_WIDTH, HUD_COLOR,
                        xPosVelocityBar, (int) (height * barMargin),
                        xPosVelocityBar, (int) (height * inverseBarMargin)
                );

                final float currentVelocity = target.interpolatedVelocity().length();

                float[] velocityTicks = ticks(currentVelocity, 25, 100);
                for (float tick : velocityTicks) {
                    int yPos = (int) (((-1 * tick * (HUDStyleSettings.ALTVEL_BAR_SIZE / 2)) + 0.5f) * height);
                    hud.line(HUD_STROKE_WIDTH, HUD_COLOR, xPosVelocityBar, yPos, xPosVelocityBar + HUDStyleSettings.TICKSIZE, yPos);
                }

                hud.text(
                        xPosVelocityBar - (10 + HUDStyleSettings.TEXT_SIZE_LARGE), yMid,
                        HUDStyleSettings.TEXT_SIZE_LARGE, LUCIDA_CONSOLE, NVG_ALIGN_RIGHT, HUD_COLOR,
                        Integer.toString((int) currentVelocity)
                );
            }

            { // altitude bar
                final int xPosAltitudeBar = (int) (width * inverseBarMargin);
                hud.line(
                        HUD_STROKE_WIDTH, HUD_COLOR,
                        xPosAltitudeBar, (int) (height * barMargin),
                        xPosAltitudeBar, (int) (height * inverseBarMargin)
                );
                final float currentAltitude = target.interpolatedPosition().z() * 10f; // TODO determine altitude

                float[] heightTicks = ticks(currentAltitude, 25, 100);
                for (float tick : heightTicks) {
                    int yPos = (int) (((-1 * tick * (HUDStyleSettings.ALTVEL_BAR_SIZE / 2)) + 0.5f) * height);
                    hud.line(HUD_STROKE_WIDTH, HUD_COLOR, xPosAltitudeBar, yPos, xPosAltitudeBar + HUDStyleSettings.TICKSIZE, yPos);
                }

                hud.text(
                        xPosAltitudeBar + (10 + HUDStyleSettings.TEXT_SIZE_LARGE), yMid,
                        HUDStyleSettings.TEXT_SIZE_LARGE, LUCIDA_CONSOLE, NVG_ALIGN_LEFT, HUD_COLOR,
                        Integer.toString((int) currentAltitude)
                );
            }

            { // boresight / direction
                DirVector dir = project(target.interpolatedForward(), lookDirection);

                int BSX = (int) (BS_SENSITIVITY * dir.x()) + xMid;
                int BSY = (int) (BS_SENSITIVITY * dir.y()) + yMid;
                hud.line(
                        HUD_STROKE_WIDTH, HUD_COLOR,
                        BSX - HUDStyleSettings.BORESIGHT_SIZE, BSY + HUDStyleSettings.BORESIGHT_SIZE,
                        BSX, BSY,
                        BSX + HUDStyleSettings.BORESIGHT_SIZE, BSY + HUDStyleSettings.BORESIGHT_SIZE
                );
            }

            { // Flight Path Vector
                DirVector dir = project(target.interpolatedVelocity(), lookDirection);

                int FPVX = (int) (FPV_SENSITIVITY * dir.x()) + xMid;
                int FPVY = (int) (FPV_SENSITIVITY * dir.y()) + yMid;
                hud.circle(FPVX, FPVY, 10, Color4f.INVISIBLE, HUD_STROKE_WIDTH, HUD_COLOR);
                hud.line(HUD_STROKE_WIDTH, HUD_COLOR, FPVX - 25, FPVY, FPVX - 10, FPVY);
                hud.line(HUD_STROKE_WIDTH, HUD_COLOR, FPVX + 25, FPVY, FPVX + 10, FPVY);
                hud.line(HUD_STROKE_WIDTH, HUD_COLOR, FPVX, FPVY - 25, FPVX, FPVY - 10);
            }

        };
    }

    public void activate(){
        ScreenOverlay.addHudItem(display);
    }

    public void deactivate(){
        ScreenOverlay.removeHudItem(display);
    }

    /**
     * calculates the positions of ticks relative to the given value
     * @param current current value
     * @param stepSize distance between tick marks, assuming 0 is a tick
     * @param range the one-sided range of which ticks must be returned.
     * @return an array of {@code (2 * range) / stepSize)} indices that gives the positions of ticks that are within [-range, range],
     * normalized to [-1, 1], where -1 is the minimum and 0 the current value.
     */
    private static float[] ticks(float current, float stepSize, float range){
        float minimum = current - range;

        int nrOfTicks = (int) ((2 * range) / stepSize);
        float[] output = new float[nrOfTicks];

        // we start with the tick just below the minimum. take negative values into account
        float tick = (minimum > 0) ? (minimum + (minimum % stepSize)) : (minimum - (minimum % stepSize));

        for (int i = 0; i < nrOfTicks; i++) {
            // store relative position
            output[i] = ((tick - current) / range);
            tick += stepSize;
        }

        return output;
    }

    /**
     * maps a direction to 2D space by projecting it on the YZ-plane.
     * @param vector a relative position in world-space of any length
     * @param lookDirection normalized direction of the camera in world-space
     * @return the normalized projection of the vector in the YZ-plane
     */
    public static DirVector project(DirVector vector, DirVector lookDirection){
        if (!vector.isScalable()) return DirVector.zeroVector();

        DirVector proj = new DirVector();
        vector.normalize(proj);

        proj.sub(lookDirection.reducedTo(proj.dot(lookDirection), new DirVector()), proj);
        return proj;
    }
}
