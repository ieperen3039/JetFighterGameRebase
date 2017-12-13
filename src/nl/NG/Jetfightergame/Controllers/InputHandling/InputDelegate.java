package nl.NG.Jetfightergame.Controllers.InputHandling;

import nl.NG.Jetfightergame.Engine.GLFWWindow;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedInteger;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import static nl.NG.Jetfightergame.Controllers.InputHandling.MouseTracker.*;
import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Jorren Hendriks
 * @author Geert van Ieperen
 *
 * Handles the input of a certain {@link GLFWWindow} and notifies attached listeners of the input events.
 */
public class InputDelegate {

    private GLFWWindow window;
    private KeyTracker keyboard;
    private MouseTracker mouseListener;

    private TrackedInteger mouseXPosition, mouseYPosition;


    /**
     * connect the GLFWWindow with the Trackers, afterwards this object can be ignored.
     * The trackers do not have to be bound to the window in any other way.
     * @param window a window whose inputs must be delegated to the KeyTracker and MouseTracker
     */
    public InputDelegate(GLFWWindow window) {
        this.window = window;
        keyboard = KeyTracker.getInstance();
        mouseListener = MouseTracker.getInstance();

        window.registerListener(new KeyEventHandler());
        window.registerListener(new MouseButtonEventHandler());
        window.registerListener(new MouseMoveEventHandler());
        window.registerListener(new MouseScrollEventHandler());

        Vector2i pos = window.getMousePosition();
        mouseXPosition = new TrackedInteger(pos.x);
        mouseYPosition = new TrackedInteger(pos.y);
    }

    private class KeyEventHandler extends GLFWKeyCallback {
        @Override
        public void invoke(long windowHandle, int keyCode, int scancode, int action, int mods) {
            if (keyCode < 0) return;

            KeyTracker.KeyEvent event = new KeyTracker.KeyEvent(keyCode);
            if (action == GLFW_PRESS) {
                keyboard.keyPressed(event);
            } else if (action == GLFW_RELEASE) {
                keyboard.keyReleased(event);
            }
        }
    }

    private class MouseButtonEventHandler extends GLFWMouseButtonCallback {
        @Override
        public void invoke(long windowHandle, int button, int action, int mods) {
            Vector2i pos = window.getMousePosition();

            MouseButton eventButton;
            switch (button){
                case GLFW_MOUSE_BUTTON_1:
                    eventButton = MouseButton.BUTTON_LEFT;
                    break;
                case GLFW_MOUSE_BUTTON_2:
                    eventButton = MouseButton.BUTTON_MIDDLE;
                    break;
                case GLFW_MOUSE_BUTTON_3:
                    eventButton = MouseButton.BUTTON_RIGHT;
                    break;
                default:
                    eventButton = MouseButton.BUTTON_UNDEFINED;
            }
            // send absolute coordinates, as these are only relevant when mouse is not captured
            MouseEvent event = new MouseEvent(pos.x(), pos.y(), window.getWidth(), window.getHeight(), eventButton);
            if (action == GLFW_PRESS) {
                mouseListener.mousePressed(event);
            } else if (action == GLFW_RELEASE) {
                mouseListener.mouseReleased(event);
            }
        }
    }

    private class MouseMoveEventHandler extends GLFWCursorPosCallback {
        @Override
        public void invoke(long windowHandle, double xPos, double yPos) {

            // Trackers must always be updated, to prevent sudden catchup
            mouseXPosition.update((int) xPos);
            mouseYPosition.update((int) yPos);

            if (window.isMouseCaptured()){

                MouseEvent event = new MouseEvent(
                        mouseXPosition.difference(), mouseYPosition.difference(),
                        window.getWidth(), window.getHeight(),
                        MouseButton.BUTTON_UNDEFINED
                );

                if (mouseListener.leftButton()) {
                    mouseListener.mouseDragged(event);
                } else {
                    mouseListener.mouseMoved(event);
                }
            } else {
                // coordinates are absolute
                MouseEvent event = new MouseEvent((int) xPos, (int) yPos, window.getWidth(), window.getHeight(), MouseButton.BUTTON_UNDEFINED);
                if (mouseListener.leftButton()) {
                    mouseListener.mouseDragged(event);
                } else {
                    mouseListener.mouseMoved(event);
                }
            }
        }
    }

    private class MouseScrollEventHandler extends GLFWScrollCallback {
        @Override
        public void invoke(long windowHandle, double xScroll, double yScroll) {
            MouseWheelEvent event = new MouseWheelEvent(yScroll);
            mouseListener.mouseWheelMoved(event);
        }
    }
}


