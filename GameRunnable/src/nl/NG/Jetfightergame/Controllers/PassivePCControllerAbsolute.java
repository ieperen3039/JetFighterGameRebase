package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Settings.KeyBinding;

/**
 * @author Geert van Ieperen
 * created on 13-12-2017.
 * this controller considers the mouse to give absolute roll and pitch values.
 * Not moving the mouse will result in not rolling/pitching the plane
 */
public class PassivePCControllerAbsolute extends PassivePCController {
    @Override
    protected float getKeyAxis(KeyBinding keyUp, KeyBinding keyDown) {
        int i = 0;
        if (keyboard.isPressed(keyUp.getKey())) i++;
        if (keyboard.isPressed(keyDown.getKey())) i--;
        return (i);
    }

    @Override
    protected float getMouseY(float modifier) {
        int prev = mouseY;
        mouseY = 0;
        return normalize(prev * modifier);
    }

    @Override
    protected float getMouseX(float modifier) {
        int roll = mouseX;
        mouseX = 0;
        return normalize(roll * modifier);
    }
}
