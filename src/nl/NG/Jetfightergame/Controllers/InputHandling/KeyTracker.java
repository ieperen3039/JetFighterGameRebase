package nl.NG.Jetfightergame.Controllers.InputHandling;

import nl.NG.Jetfightergame.Rendering.GLFWWindow;
import org.lwjgl.glfw.GLFWKeyCallback;

import java.util.ArrayList;
import java.util.Collection;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Singleton Design
 * a class that provides a passive way of polling the status of keys
 * @author Geert van Ieperen
 *         created on 2-11-2017.
 */
public class KeyTracker {

    private static KeyTracker instance;
    private final Collection<TrackerKeyListener> eventListeners;
    private GLFWWindow window;

    private KeyTracker() {
        eventListeners = new ArrayList<>();
    }

    public static KeyTracker getInstance(){
        if (instance == null){
            instance = new KeyTracker();
        }
        return instance;
    }

    /**
     * keyListeners will receive an update whenever a key event occurs, regardless of gamemode
     * @param joiner the class that will receive updates whenever an MouseMotionEvent should occur
     */
    public void addKeyListener(TrackerKeyListener joiner){
        eventListeners.add(joiner);
    }

    public void removeKeyListener(TrackerKeyListener leaver){
        eventListeners.remove(leaver);
    }

    /**
     * @param key keycode of the key of which the button should be get.
     * @return  true    if the key is pressed
     *          false   if the key is not pressed
     */
    public boolean isPressed(int key) {
        if (key < 0) return false;
        switch (key) {
            case GLFW_MOUSE_BUTTON_LEFT:
            case GLFW_MOUSE_BUTTON_RIGHT:
            case GLFW_MOUSE_BUTTON_MIDDLE:
            case GLFW_MOUSE_BUTTON_4:
            case GLFW_MOUSE_BUTTON_5:
            case GLFW_MOUSE_BUTTON_6:
            case GLFW_MOUSE_BUTTON_7:
            case GLFW_MOUSE_BUTTON_8:
                return window.isMouseButtonPressed(key);
            default:
                return window.isKeyPressed(key);
        }

    }

    public void listenTo(GLFWWindow window) {
        this.window = window;
        window.registerListener(new KeyEventHandler());
    }

    private class KeyEventHandler extends GLFWKeyCallback {
        @Override
        public void invoke(long windowHandle, int keyCode, int scancode, int action, int mods) {
            if (keyCode < 0) return;

            if (action == GLFW_PRESS) {
                eventListeners.forEach(l -> l.keyPressed(keyCode));
//            } else if (action == GLFW_RELEASE) {

            }
        }
    }
}

