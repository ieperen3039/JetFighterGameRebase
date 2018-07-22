package nl.NG.Jetfightergame.Launcher;

import nl.NG.Jetfightergame.Controllers.InputHandling.KeyTracker;
import nl.NG.Jetfightergame.Controllers.InputHandling.MouseTracker;
import nl.NG.Jetfightergame.Engine.JetFighterGame;
import nl.NG.Jetfightergame.Rendering.GLFWWindow;
import nl.NG.Jetfightergame.ScreenOverlay.HudMenu;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Timer;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;

/**
 * @author Geert van Ieperen created on 21-5-2018.
 */
public class JetFighterClient {

    public static final int PROLL_XPOS = 10;
    public static final int PROLL_YPOS = 10;
    private GLFWWindow window;
    private final ScreenOverlay rendering;
    private static final float targetDeltaMillis = 100;

    private AtomicInteger action = new AtomicInteger(NONE);
    private static final int START_GAME = 1;
    private static final int EXIT_CLIENT = 2;
    private static final int NONE = 0;

    public JetFighterClient() throws IOException {
        window = newWindow();

        rendering = new ScreenOverlay(() -> true);
        HudMenu menu = new Menu(this::startGame, this::getWidth, this::getHeight);
        rendering.addMenuItem(menu);
    }

    private Integer getHeight() {
        return window.getHeight();
    }

    private Integer getWidth() {
        return window.getWidth();
    }


    private GLFWWindow newWindow() {
        GLFWWindow window = new GLFWWindow(ServerSettings.GAME_NAME + " Client", 1000, 600, true);
        MouseTracker.getInstance().listenTo(window);
        KeyTracker.getInstance().listenTo(window);
        window.setClearColor(Color4f.WHITE);
        return window;
    }

    private void display() throws Exception {
        while (action.get() != EXIT_CLIENT) {
            action.set(EXIT_CLIENT);
            window.open();
            renderLoop();

            if (action.get() == START_GAME) {
                new JetFighterGame().root();
                window = newWindow();
            }
        }
    }

    private void renderLoop() {
        Timer loopTimer = new Timer();

        try {
            while (!window.shouldClose()) {
                loopTimer.updateLoopTime();

                // draw the menu
                rendering.draw(getWidth(), getHeight(), PROLL_XPOS, PROLL_YPOS, 12);

                // update window and poll for clicks
                window.update();

                // Poll for events
                glfwPollEvents();

                // sleep at least one millisecond
                long remainingTime = Math.max((long) targetDeltaMillis - loopTimer.getTimeSinceLastUpdate(), 1);
                Thread.sleep(remainingTime);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();

        } finally {
            window.cleanup();
        }
    }

    /** schedules the game to start at the next loop */
    private void startGame() {
        try {
            Logger.DEBUG.print("Starting your game...");
            action.set(START_GAME);
            window.close();

        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    public static void main(String[] args) throws Exception {
        new JetFighterClient().display();
    }
}
