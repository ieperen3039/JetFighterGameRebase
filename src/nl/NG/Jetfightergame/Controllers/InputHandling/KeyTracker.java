package nl.NG.Jetfightergame.Controllers.InputHandling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;

/**
 * Singleton Design
 * a class that provides a passive way of polling the status of keys
 * @author Geert van Ieperen
 *         created on 2-11-2017.
 */
public class KeyTracker {

    private static KeyTracker instance;
    private final Collection<TrackerKeyListener> eventListeners;
    private Map<Integer, Boolean> registeredKeys;

    private KeyTracker() {
        registeredKeys = new Hashtable<>(8);
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

    public void keyPressed(KeyEvent e) {
        final int keyCode = e.getKeyCode();
//        Toolbox.print("pressed key", keyCode);
        registeredKeys.replace(keyCode, true);
        eventListeners.forEach(l -> l.keyPressed(keyCode));
    }

    public void keyReleased(KeyEvent e) {
        registeredKeys.replace(e.getKeyCode(), false);
    }

    /** @see #isPressed(int) */
    public void addKey(int key) {
        registeredKeys.putIfAbsent(key, false);
    }

    /**
     * @param key keycode of the key of which the button should be get.
     *            The correct code depends on with which code {@link #keyPressed(KeyEvent)} is called
     * @return  true    if the key is pressed
     *          false   if the key is not pressed
     *          null    if the key is not registered by {@link #addKey(int)}
     */
    public Boolean isPressed(int key) {
        return registeredKeys.get(key);
    }

    public static class KeyEvent {

        private final int keyCode;

        public KeyEvent(int keyCode) {
            this.keyCode = keyCode;
        }

        public int getKeyCode() {
            return keyCode;
        }
    }
}

