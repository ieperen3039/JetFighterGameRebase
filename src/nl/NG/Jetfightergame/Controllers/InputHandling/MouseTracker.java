package nl.NG.Jetfightergame.Controllers.InputHandling;

import nl.NG.Jetfightergame.Rendering.GLFWWindow;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedInteger;
import org.joml.Vector2i;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BooleanSupplier;

import static nl.NG.Jetfightergame.Settings.KeyBindings.*;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;

/**
 * singleton design
 *
 * The mousetracker has two modes, a menu mode and a game-mode.
 *
 * Gamemode:
 * Tracks the mouse and prevents it from leaving the screen, mouse is invisible.
 * clicks are recorded and passed to Clicklisteners in Gamemode setting.
 * movements are passed to mouseMoveListeners
 *
 * Menumode:
 * Mouse may leave the screen, and is visible.
 * left-clicks are passed to Clicklisteners in Menumode settings
 * movements are not recorded
 *
 * @author Geert van Ieperen
 *         created on 2-11-2017.
 */
public class MouseTracker {

    private static MouseTracker instance;
    private final Collection<TrackerClickListener> inGameClickListeners;
    private final Collection<TrackerClickListener> menuClickListeners;
    private final Collection<TrackerMoveListener> inGameMotionListeners;
    private final Collection<TrackerScrollListener> inGameScrollListener;
    private final Collection<TrackerScrollListener> menuScrollListener;
    private final Collection<TrackerDragListener> menuDragListener;

    private boolean leftMouse = false;
    private boolean rightMouse = false;
    private boolean middleMouse = false;
    private boolean menuClick = false;
    private TrackedInteger mouseX;
    private TrackedInteger mouseY;

    /** to decide between menu-mode and ingame-mode */
    private BooleanSupplier inPlayMode = () -> false;

    private MouseTracker() {
        inGameMotionListeners = new ArrayList<>();
        inGameClickListeners = new ArrayList<>();
        menuClickListeners = new ArrayList<>();
        inGameScrollListener = new ArrayList<>();
        menuDragListener = new ArrayList<>();
        menuScrollListener = new ArrayList<>();
        mouseX = new TrackedInteger(0);
        mouseY = new TrackedInteger(0);
    }

    public static MouseTracker getInstance() {
        if (instance == null) instance = new MouseTracker();
        return instance;
    }

    /**
     * @param isInGame should return true if the mouse is captured. and listeners in gamemode should receive
     *                 notifications if it returns false, mouse is not captured, and listeners of menumode will receive
     *                 notifications
     */
    public void setGameModeDecision(BooleanSupplier isInGame) {
        this.inPlayMode = isInGame;
    }

    // Click listener part

    /**
     * clicklisteners will receive an updateGameLoop whenever there is right-clicked with the mouse
     * @param joiner        the class that will receive updates whenever an MouseClickEvent should occur
     * @param inCaptureMode whether this listener should receive his update during or outside capture mode
     */
    public void addClickListener(TrackerClickListener joiner, boolean inCaptureMode) {
        if (inCaptureMode) {
            inGameClickListeners.add(joiner);
        } else {
            menuClickListeners.add(joiner);
        }
    }

    public void removeClickListener(TrackerClickListener leaver, boolean inCaptureMode) {
        if (inCaptureMode) {
            inGameClickListeners.remove(leaver);
        } else {
            menuClickListeners.remove(leaver);
        }
    }

    public boolean leftButton() {
        return inPlayMode.getAsBoolean() ? leftMouse : menuClick;
    }

    public boolean rightButton() {
        return rightMouse;
    }

    public boolean middleButton() {
        return middleMouse;
    }

    private void mousePressed(MouseEvent e) {
        if (inPlayMode.getAsBoolean()) {
            switch (e.button) {
                case MOUSE_BUTTON_LEFT:
                    leftMouse = true;
                    break;
                case MOUSE_BUTTON_MIDDLE:
                    middleMouse = true;
                    break;
                case MOUSE_BUTTON_RIGHT:
                    rightMouse = true;
                    break;
            }
            inGameClickListeners.forEach(l -> l.clickEvent(e.x, e.y));

        } else if (e.button == MOUSE_BUTTON_LEFT) {
            menuClick = true;
            // call if button is left and in menu-mode
            menuClickListeners.forEach(l -> l.clickEvent(e.x, e.y));
        }
    }

    private void mouseReleased(MouseEvent e) {
        switch (e.button) {
            case MOUSE_BUTTON_LEFT:
                leftMouse = false;
                menuClick = false;
                break;
            case MOUSE_BUTTON_MIDDLE:
                middleMouse = false;
                break;
            case MOUSE_BUTTON_RIGHT:
                rightMouse = false;
                break;
        }
    }

    // Motion listener part

    /**
     * motionListeners will receive an update whenever a mouse event occurs in game_mode
     * @param joiner the class that will receive updates whenever an MouseMotionEvent should occur
     */
    public void addMotionListener(TrackerMoveListener joiner) {
        inGameMotionListeners.add(joiner);
    }

    public void removeMotionListener(TrackerMoveListener leaver) {
        inGameMotionListeners.remove(leaver);
    }

    /**
     * dragListeners will receive an update whenever a mouse event occurs in game_mode while a button is held
     * @param joiner the class that will receive updates whenever an MouseMotionEvent should occur
     */
    public void addDragListener(TrackerDragListener joiner) {
        menuDragListener.add(joiner);
    }

    public void removeDragListener(TrackerDragListener leaver) {
        menuDragListener.remove(leaver);
    }

    private void mouseMoved(MouseEvent e) {
        mouseX.update(e.x);
        mouseY.update(e.y);
        Integer dx = mouseX.difference();
        Integer dy = mouseY.difference();

        // mostly for mouse capture
        if ((dx == 0) && (dy == 0)) return;

        if (inPlayMode.getAsBoolean()) {
            inGameMotionListeners.forEach(l -> l.mouseMoved(dx, dy));

        } else if (e.button == MOUSE_BUTTON_LEFT) { // note that only left is enabled in menu (yet)
            menuDragListener.forEach(l -> l.mouseDragged(dx, dy));
        }
    }

    // scroll listener part

    /**
     * scrollListeners will receive an update whenever a scroll event occurs in the appropriate mode
     * @param joiner the class that will receive updates whenever an MouseWheelMovedEvent should occur
     * @param inCaptureMode whether this listener should receive his update during or outside capture mode
     */
    public void addScrollListener(TrackerScrollListener joiner, boolean inCaptureMode){
        if (inCaptureMode){
            inGameScrollListener.add(joiner);
        } else {
            menuScrollListener.add(joiner);
        }
    }

    public void removeScrollListener(TrackerScrollListener leaver, boolean inCaptureMode){
        if (inCaptureMode){
            inGameScrollListener.remove(leaver);
        } else {
            menuScrollListener.remove(leaver);
        }
    }

    private void mouseWheelMoved(float scroll) {
        if (inPlayMode.getAsBoolean()) {
            inGameScrollListener.forEach(l -> l.mouseWheelMoved(scroll));
        } else {
            menuScrollListener.forEach(l -> l.mouseWheelMoved(scroll));
        }
    }

    /** starts listening to the given windows until the window is disposed */
    public void listenTo(GLFWWindow window) {
        window.registerListener(new MouseButtonEventHandler());
        window.registerListener(new MouseMoveEventHandler());
        window.registerListener(new MouseScrollEventHandler());

        Vector2i pos = window.getMousePosition();
        mouseX.update(pos.x);
        mouseY.update(pos.y);
    }

    /**
     * represents a mouse event.
     */
    public static class MouseEvent {

        /** position relative to the upper left corner of the active area */
        public final int x, y;
        public final int button;

        /**
         * an event of the mouse, either movement or click or both
         * @param x pixels from left; if in capture mode, relative to previous, otherwise relative to upperleft corner
         * @param y pixels from top; if in capture mode, relative to previous, otherwise relative to upperleft corner
         */
        public MouseEvent(int x, int y, int pressedButton) {
            this.x = x;
            this.y = y;
            this.button = pressedButton;
        }
    }

    private class MouseButtonEventHandler extends GLFWMouseButtonCallback {
        @Override
        public void invoke(long windowHandle, int button, int action, int mods) {
            MouseEvent event = new MouseEvent(mouseX.current(), mouseY.current(), button);

            if (action == GLFW_PRESS) {
                mousePressed(event);
            } else if (action == GLFW_RELEASE) {
                mouseReleased(event);
            }
        }
    }

    private class MouseMoveEventHandler extends GLFWCursorPosCallback {
        @Override
        public void invoke(long windowHandle, double xPos, double yPos) {
            int button;
            if (leftButton()) {
                button = MOUSE_BUTTON_LEFT;
            } else if (rightMouse) {
                button = MOUSE_BUTTON_RIGHT;
            } else if (middleMouse) {
                button = MOUSE_BUTTON_MIDDLE;
            } else {
                button = MOUSE_BUTTON_NONE;
            }

            mouseMoved(new MouseEvent((int) xPos, (int) yPos, button));
        }
    }

    private class MouseScrollEventHandler extends GLFWScrollCallback {
        @Override
        public void invoke(long windowHandle, double xScroll, double yScroll) {
            mouseWheelMoved((float) yScroll);
        }
    }
}
