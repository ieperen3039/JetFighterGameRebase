package nl.NG.Jetfightergame.Controllers;

/**
 * @author Geert van Ieperen
 *         created on 31-10-2017.
 * may be called to check the current actions of this controller
 * at least one implementation will concern player input
 */
public interface Controller {

    /**
     * the amount of throttle requested by this controller.
     * Values out of the range [-1, 1] do not occur (should be taken care of in implementation).
     * @return the desired throttle as fraction [-1, 1]
     *          if (return < 0) the controller wants to brake
     *          if (return = 0) the controller wants to hold speed
     *          if (return > 0) the controller wants to accelerate
     */
    float throttle();

    /**
     * the amount of pitch requested by this controller.
     * a positive pitch makes the nose move up
     * Values out of the range [-1, 1] do not occur (should be taken care of in implementation).
     * @return the desired roll as fraction [-1, 1]
     *          if (return < 0) the controller wants to pitch up
     *          if (return = 0) the controller wants to hold direction
     *          if (return > 0) the controller wants to pitch down
     */
    float pitch();

    /**
     * the amount of yaw requested by this controller.
     * a positive yaw makes the plane turn right
     * Values out of the range [-1, 1] do not occur (should be taken care of in implementation).
     * @return the desired pitch as fraction [-1, 1]
     *          if (return < 0) the controller wants to roll clockwise
     *          if (return = 0) the controller wants to hold rotation
     *          if (return > 0) the controller wants to roll counterclockwise
     */
    float yaw();

    /**
     * the amount of clockwise roll requested by this controller.
     * Values out of the range [-1, 1] do not occur (should be taken care of in implementation).
     * @return the desired yaw as fraction [-1, 1]
     *          if (return < 0) the controller wants to roll clockwise
     *          if (return = 0) the controller wants to hold rotation
     *          if (return > 0) the controller wants to roll counterclockwise
     */
    int roll();

    /**
     * @return whether primary fire should be activated
     */
    boolean primaryFire();

    /**
     *
     * @return whether primary fire should be activated
     */
    boolean secondaryFire();
}
