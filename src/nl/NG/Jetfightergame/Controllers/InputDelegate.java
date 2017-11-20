package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Engine.GLFWWindow;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Jorren Hendriks
 * @author Geert van Ieperen
 *
 * Handles the input of a certain {@link GLFWWindow} and notifies attached listeners of the input events.
 */
public class InputDelegate {

    private GLFWWindow window;
    private KeyTracker keyListener;
    private MouseTracker mouseListener;

    /**
     * connect the GLFWWindow with the Trackers, afterwards this object can be ignored.
     * The trackers do not have to be bound to the window in any other way.
     * @param window a window whose inputs must be delegated to the KeyTracker and MouseTracker
     */
    public InputDelegate(GLFWWindow window) {
        this.window = window;
        keyListener = KeyTracker.getInstance();
        mouseListener = MouseTracker.getInstance();

        window.registerListener(new KeyEventHandler());
        window.registerListener(new MouseButtonEventHandler());
        window.registerListener(new MouseMoveEventHandler());
        window.registerListener(new MouseScrollEventHandler());
    }

    private class KeyEventHandler extends GLFWKeyCallback {
        @Override
        public void invoke(long windowHandle, int keyCode, int scancode, int action, int mods) {
            if (keyCode < 0) return;

            KeyTracker.KeyEvent event = new KeyTracker.KeyEvent(keyCode);
            if (action == GLFW_PRESS) {
                keyListener.keyPressed(event);
            } else if (action == GLFW_RELEASE) {
                keyListener.keyReleased(event);
            }
        }
    }

    private class MouseButtonEventHandler extends GLFWMouseButtonCallback {
        @Override
        public void invoke(long windowHandle, int button, int action, int mods) {
            Vector2i pos = window.getMousePosition();

            MouseTracker.MouseButton eventButton;
            switch (button){
                case GLFW_MOUSE_BUTTON_1:
                    eventButton = MouseTracker.MouseButton.BUTTON_LEFT;
                    break;
                case GLFW_MOUSE_BUTTON_2:
                    eventButton = MouseTracker.MouseButton.BUTTON_MIDDLE;
                    break;
                case GLFW_MOUSE_BUTTON_3:
                    eventButton = MouseTracker.MouseButton.BUTTON_RIGHT;
                    break;
                default:
                    eventButton = MouseTracker.MouseButton.BUTTON_UNDEFINED;
            }

            MouseTracker.MouseEvent event = new MouseTracker.MouseEvent(window, pos.x(), pos.y(), eventButton);
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
            MouseTracker.MouseEvent event = new MouseTracker.MouseEvent(window, (int) xPos, (int) yPos, MouseTracker.MouseButton.BUTTON_UNDEFINED);
            if (mouseListener.leftButton()){
                mouseListener.mouseDragged(event);
            } else {
                mouseListener.mouseMoved(event);
            }
        }
    }

    private class MouseScrollEventHandler extends GLFWScrollCallback {
        @Override
        public void invoke(long windowHandle, double xScroll, double yScroll) {
            MouseTracker.MouseWheelEvent event = new MouseTracker.MouseWheelEvent(yScroll);
            mouseListener.mouseWheelMoved(event);
        }
    }
}


