package nl.NG.Jetfightergame.ScreenOverlay.HUD;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;

import java.util.function.Consumer;

import static nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings.*;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_LEFT;
import static org.lwjgl.nanovg.NanoVG.NVG_ALIGN_RIGHT;

/**
 * @author Geert van Ieperen
 * created on 24-12-2017.
 */
public class GravityHud implements Consumer<ScreenOverlay.Painter> {

    private static final float FPV_SENSITIVITY = 600f;
    private static final float BS_SENSITIVITY = 2000f;
    private final AbstractJet target;
    private final Camera camera;

    public GravityHud(AbstractJet target, Camera camera) {
        this.target = target;
        this.camera = camera;
    }

    @Override
    public void accept(ScreenOverlay.Painter hud) {
        final int width = hud.windowWidth;
        final int height = hud.windowHeight;
        final int xMid = width / 2;
        final int yMid = height / 2;
        final float barMargin = (1 - ALTVEL_BAR_SIZE) / 2;
        final float inverseBarMargin = 1 - barMargin;
        final DirVector lookDirection = new DirVector(camera.vectorToFocus());
        final DirVector upVector = new DirVector(camera.getUpVector());
        final DirVector lookRight = lookDirection.cross(upVector, new DirVector());
        final DirVector lookUp = lookRight.cross(lookDirection, upVector);

        lookDirection.normalize();
        lookRight.normalize();
        lookUp.normalize();

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
                int yPos = (int) (((-1 * tick * (ALTVEL_BAR_SIZE / 2)) + 0.5f) * height);
                hud.line(HUD_STROKE_WIDTH, HUD_COLOR, xPosVelocityBar, yPos, xPosVelocityBar + TICKSIZE, yPos);
            }

            float[] markTicks = ticks(currentVelocity, 100, 100);
            for (float tick : markTicks) {
                int yPos = (int) (((-1 * tick * (ALTVEL_BAR_SIZE / 2)) + 0.5f) * height);
                hud.line(HUD_STROKE_WIDTH, HUD_COLOR, xPosVelocityBar + TICKSIZE, yPos, xPosVelocityBar + 2 * TICKSIZE, yPos);
            }

            hud.text(
                    xPosVelocityBar - (10 + TEXT_SIZE_LARGE), yMid,
                    TEXT_SIZE_LARGE, FONT, NVG_ALIGN_RIGHT, HUD_COLOR,
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
                int yPos = (int) (((-1 * tick * (ALTVEL_BAR_SIZE / 2)) + 0.5f) * height);
                hud.line(HUD_STROKE_WIDTH, HUD_COLOR, xPosAltitudeBar, yPos, xPosAltitudeBar + TICKSIZE, yPos);
            }

            float[] markTicks = ticks(currentAltitude, 1000, 100);
            for (float tick : markTicks) {
                int yPos = (int) (((-1 * tick * (ALTVEL_BAR_SIZE / 2)) + 0.5f) * height);
                hud.line(HUD_STROKE_WIDTH, HUD_COLOR, xPosAltitudeBar + TICKSIZE, yPos, xPosAltitudeBar + 2 * TICKSIZE, yPos);
            }

            hud.text(
                    xPosAltitudeBar + (10 + TEXT_SIZE_LARGE), yMid,
                    TEXT_SIZE_LARGE, FONT, NVG_ALIGN_LEFT, HUD_COLOR,
                    Integer.toString((int) currentAltitude)
            );
        }

        { // boresight / direction
            final DirVector forward = target.interpolatedForward();

            final float xComp = lookRight.dot(forward);
            final float yComp = -(lookUp.dot(forward));

            int BSX = (int) (BS_SENSITIVITY * xComp) + xMid;
            int BSY = (int) (BS_SENSITIVITY * yComp) + yMid;
            hud.line(
                    HUD_STROKE_WIDTH, HUD_COLOR,
                    BSX - BORESIGHT_SIZE, BSY + BORESIGHT_SIZE,
                    BSX, BSY,
                    BSX + BORESIGHT_SIZE, BSY + BORESIGHT_SIZE
            );
        }

        { // Flight Path Vector
            final DirVector velocity = target.interpolatedVelocity().normalize(new DirVector());

            final float xComp = lookRight.dot(velocity);
            final float yComp = -(lookUp.dot(velocity));

            int FPVX = (int) (FPV_SENSITIVITY * xComp) + xMid;
            int FPVY = (int) (FPV_SENSITIVITY * yComp) + yMid;
            hud.circle(FPVX, FPVY, 10, Color4f.INVISIBLE, HUD_STROKE_WIDTH, HUD_COLOR);
            hud.line(HUD_STROKE_WIDTH, HUD_COLOR, FPVX - 25, FPVY, FPVX - 10, FPVY);
            hud.line(HUD_STROKE_WIDTH, HUD_COLOR, FPVX + 25, FPVY, FPVX + 10, FPVY);
            hud.line(HUD_STROKE_WIDTH, HUD_COLOR, FPVX, FPVY - 25, FPVX, FPVY - 10);
        }

        { // Angle markings

        }
    }

    /**
     * calculates the positions of ticks relative to the given value
     * @param current current value
     * @param stepSize distance between tick marks, assuming 0 is a tick
     * @param range the two-sided range of which ticks must be returned.
     * @return an array of {@code (2 * range) / stepSize)} indices that gives the positions of ticks that are within [-range, range],
     * normalized to [-1, 1], where -1 is the minimum and 0 the current value.
     */
    private static float[] ticks(float current, float stepSize, float range){
        float minimum = current - range;

        // we start with the tick just below the minimum. take negative values into account
        float tick = minimum - (minimum % stepSize);
        if (minimum > 0) tick += stepSize;

        int nrOfTicks = (int) ((2 * range) / stepSize);
        if ((tick + (nrOfTicks * stepSize)) < (current + range)) nrOfTicks++;
        float[] output = new float[nrOfTicks];

        for (int i = 0; i < nrOfTicks; i++) {
            // store relative position
            output[i] = ((tick - current) / range);
            tick += stepSize;
        }

        return output;
    }

}
