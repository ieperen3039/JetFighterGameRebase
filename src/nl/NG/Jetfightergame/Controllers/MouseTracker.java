package nl.NG.Jetfightergame.Controllers;

import nl.NG.Jetfightergame.Engine.GLFWWindow;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedInteger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

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

    private TrackedInteger mouseX, mouseY;

    /** to decide between menu-mode and ingame-mode */
    private BooleanSupplier inPlayMode = () -> false;

    private MouseTracker() {
        inGameMotionListeners = new ArrayList<>();
        inGameClickListeners = new ArrayList<>();
        menuClickListeners = new ArrayList<>();
        inGameScrollListener = new ArrayList<>();
        menuDragListener = new ArrayList<>();
        menuScrollListener = new ArrayList<>();

        menuClickListeners.add(
                (x, y) -> {
                    mouseX = new TrackedInteger(x);
                    mouseY = new TrackedInteger(y);
                });
    }

    public static MouseTracker getInstance() {
        if (instance == null) instance = new MouseTracker();
        return instance;
    }

    /**
     *
     * @param isInGame should return true if the mouse must be captured. and listeners in gamemode should receive notifications
     *                 if it returns false, mouse is not captured, and listeners of menumode will receive notifications
     */
    public void setMenuModeDecision(BooleanSupplier isInGame){
        this.inPlayMode = isInGame;
    }

    // Click listener part

    /**
     * clicklisteners will receive an updateGameLoop whenever there is right-clicked with the mouse
     * @param joiner the class that will receive updates whenever an MouseClickEvent should occur
     * @param inCaptureMode whether this listener should receive his update during or outside capture mode
     */
    public void addClickListener(TrackerClickListener joiner, boolean inCaptureMode){
        if (inCaptureMode) {
            inGameClickListeners.add(joiner);
        } else {
            menuClickListeners.add(joiner);
        }
    }

    public void removeClickListener(TrackerClickListener leaver, boolean inCaptureMode){
        if (inCaptureMode) {
            inGameClickListeners.remove(leaver);
        } else {
            menuClickListeners.remove(leaver);
        }
    }

    public boolean leftButton() {
        return leftMouse;
    }

    public boolean rightButton() {
        return rightMouse;
    }

    public boolean middleButton() {
        return middleMouse;
    }

    public void mousePressed(MouseEvent e) {
        // prepare calling the method
        final Consumer<TrackerClickListener> notify = l -> l.clickEvent(e.getX(), e.getY());

        switch (e.getButton()) {
            case BUTTON_LEFT:
                leftMouse = true;
                if (!inPlayMode.getAsBoolean()) {
                    // call if button is left and in menu-mode
                    menuClickListeners.forEach(notify);
                }
                break;
            case BUTTON_MIDDLE:
                middleMouse = true;
                break;
            case BUTTON_RIGHT:
                rightMouse = true;
                break;
        }
        if (inPlayMode.getAsBoolean()) {
            // call if in game-mode
            inGameClickListeners.forEach(notify);
        }
    }

    public void mouseReleased(MouseEvent e) {
        switch (e.getButton()){
            case BUTTON_LEFT:
                leftMouse = false;
                break;
            case BUTTON_MIDDLE:
                middleMouse = false;
                break;
            case BUTTON_RIGHT:
                rightMouse = false;
                break;
        }
    }

    // Motion listener part

    /**
     * motionListeners will receive an update whenever a mouse event occurs in game_mode
     * @param joiner the class that will receive updates whenever an MouseMotionEvent should occur
     */
    public void addMotionListener(TrackerMoveListener joiner){
        inGameMotionListeners.add(joiner);
    }

    public void removeMotionListener(TrackerMoveListener leaver){
        inGameMotionListeners.remove(leaver);
    }

    /**
     * motionListeners will receive an update whenever a mouse event occurs in game_mode
     * @param joiner the class that will receive updates whenever an MouseMotionEvent should occur
     */
    public void addDragListener(TrackerDragListener joiner) {
        menuDragListener.add(joiner);
    }

    public void removeDragListener(TrackerDragListener leaver) {
        menuDragListener.remove(leaver);
    }

    public void mouseMoved(MouseEvent e) {
        if (inPlayMode.getAsBoolean()){
            passToMoveListeners(e);
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (inPlayMode.getAsBoolean()){
            passToMoveListeners(e);
        } else {
            mouseX.update(e.getX());
            mouseY.update(e.getY());
            menuDragListener.forEach(l -> l.mouseDragged(mouseX.difference(), mouseY.difference()));
        }
    }

    private void passToMoveListeners(MouseEvent mouse) {
        int xMid = mouse.frame.getWidth() / 2;
        int yMid = mouse.frame.getHeight() / 2;

        int deltaX = xMid - mouse.getX();
        int deltaY = yMid - mouse.getY();

        if (deltaX == 0 && deltaY == 0) return;

        inGameMotionListeners.forEach(l -> l.mouseMoved(deltaX, deltaY));
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

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (inPlayMode.getAsBoolean()) {
            inGameScrollListener.forEach(l -> l.mouseWheelMoved(e.getScroll()));
        } else {
            menuScrollListener.forEach(l -> l.mouseWheelMoved(e.getScroll()));
        }
    }

    public enum MouseButton {
        BUTTON_RIGHT, BUTTON_MIDDLE, BUTTON_UNDEFINED, BUTTON_LEFT
    }

    public static class MouseEvent {

        private final GLFWWindow frame;
        private final int x;
        private final int y;
        private final MouseButton button;

        public MouseEvent(GLFWWindow frame, int x, int y, MouseButton pressedButton) {
            this.frame = frame;
            this.x = x;
            this.y = y;
            this.button = pressedButton;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public MouseButton getButton() {
            return button;
        }
    }

    public static class MouseWheelEvent {

        private final float scroll;

        public MouseWheelEvent(float scroll) {
            this.scroll = scroll;
        }

        public MouseWheelEvent(double yScroll) {
            scroll = (float) yScroll;
        }

        public float getScroll() {
            return scroll;
        }
    }
}
