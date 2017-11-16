package nl.NG.Jetfightergame.Engine.Window;

import nl.NG.Jetfightergame.Engine.Settings;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;

import java.nio.DoubleBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * @author Jorren Hendriks
 * <p>
 * A window which initializes GLFW and manages it.
 */
public class GLFWWindow {

    private static final boolean CULL_FACES = false; // TODO cockwise drawing
    private final long primaryMonitor = glfwGetPrimaryMonitor();

    private final String title;
    private final boolean resizable;
    // buffers for mouse input
    private final DoubleBuffer mousePosX;
    private final DoubleBuffer mousePosY;
    private double lastTime = glfwGetTime();
    private int nbFrames = 0;
    private double frametime;

    private long window;
    private int width;
    private int height;
    private boolean fullScreen = false;

    public GLFWWindow(String title) {
        this(title, 960, 720, true);
    }

    public GLFWWindow(String title, int width, int height, boolean resizable) {
        this.title = title;
        this.width = width;
        this.height = height;
        this.resizable = resizable;

        this.mousePosX = BufferUtils.createDoubleBuffer(1);
        this.mousePosY = BufferUtils.createDoubleBuffer(1);

        // Setup error callback, print to System.err
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        // Configure window
        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, resizable ? GL_TRUE : GL_FALSE);
        // Set OpenGL version
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        if (antialiasing()) {
            glfwWindowHint(GLFW_STENCIL_BITS, Settings.ANTIALIAS);
            glfwWindowHint(GLFW_SAMPLES, Settings.ANTIALIAS);
        }

        window = getWindow(this.width, this.height);
        setWindowed();

        if (vSyncEnabled()) {
            // Turn on vSync
            glfwSwapInterval(1);
        }


        GL.createCapabilities();

        // Set clear color to black
        glClearColor(0f, 0f, 0f, 0f);

        // Enable Depth Test
        glEnable(GL_DEPTH_TEST);
        // Enable Stencil Test
        glEnable(GL_STENCIL_TEST);
        // Support transparencies
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        if (CULL_FACES) {
            // Cull backfaces
            glEnable(GL_CULL_FACE);
            glCullFace(GL_BACK);
        }
    }

    public static boolean antialiasing() {
        return Settings.ANTIALIAS > 0;
    }

    /**
     * @modifies window
     * @param width
     * @param height
     */
    private long getWindow(int width, int height) {
        // Create window
        long newWindow = glfwCreateWindow(width, height, this.title, NULL, NULL);
        if (newWindow == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }
        if (this.resizable) {
            // Setup resize callback
            glfwSetFramebufferSizeCallback(newWindow, (window, newWidth, newHeight) -> {
                this.width = newWidth;
                this.height = newHeight;
            });
        }

        // Make GL context current
        glfwMakeContextCurrent(newWindow);
        return newWindow;
    }

    /**
     * update the {@link GLFWWindow}. This will deal with basic OpenGL formalities. Besides it will also poll for events
     * which occurred on the window. Finally returns whether the window should close.
     *
     * @return true if the {@link GLFWWindow} should continue running, false otherwise
     */
    public boolean update() {
        // Measure speed
        double currentTime = glfwGetTime();
        nbFrames++;
        if (currentTime - lastTime >= 0.25) { // If last prinf() was more than 1 sec ago
            // printf and reset timer
            frametime = (250.0 / ((double) (nbFrames)));
            nbFrames = 0;
            lastTime += 0.25;
        }
        // Swap buffers
        glfwSwapBuffers(window);

        // Poll for events
        glfwPollEvents();

        if (glfwWindowShouldClose(window)) {
            // Release window and window callbacks when window is closed
            glfwFreeCallbacks(window);
            glfwDestroyWindow(window);

            return false;
        }

        clear();

        return true;
    }

    /**
     * Close the window
     */
    public void close() {
        glfwSetWindowShouldClose(window, true);
    }

    private void open() {
        // Show window
        glfwShowWindow(window);
    }

    /**
     * Terminate GLFW and release GLFW error callback
     */
    public void cleanup() {
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }


    /**
     * Set the color which is used for clearing the window.
     *
     * @param red   The red value (0.0 - 1.0)
     * @param green The green value (0.0 - 1.0)
     * @param blue  The blue value (0.0 - 1.0)
     * @param alpha The alpha value (0.0 - 1.0)
     */
    public void setClearColor(float red, float green, float blue, float alpha) {
        glClearColor(red, green, blue, alpha);
    }

    /**
     * Check whether a certain key is pressed.
     *
     * @param keyCode The keycode of the key.
     * @return Whether the key with requested keyCode is pressed.
     */
    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(window, keyCode) == GLFW_PRESS;
    }

    /**
     * Check whether a certain mouse button is pressed.
     *
     * @param button The button of the mouse.
     * @return Whether the requested button is pressed.
     */
    public boolean isMouseButtonPressed(int button) {
        return glfwGetMouseButton(window, button) == GLFW_PRESS;
    }

    /**
     * Get the current position of the mouse.
     *
     * @return The position of the mouse.
     */
    public Vector2i getMousePosition() {
        glfwGetCursorPos(window, mousePosX, mousePosY);
        return new Vector2i((int) mousePosX.get(0), (int) mousePosY.get(0));
    }

    /**
     * Get whether the window should close.
     *
     * @return Whether the window should close.
     */
    public boolean shouldClose() {
        return glfwWindowShouldClose(window);
    }

    /**
     * Get the title of the window.
     *
     * @return The title of the window.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the width of the window.
     *
     * @return The width of the window.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the height of the window.
     *
     * @return The height of the window.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get whether resizing the window is allowed.
     *
     * @return Whether resizing the window is allowed.
     */
    public boolean resizeEnabled() {
        return resizable;
    }

    /**
     * Get whether vSync is currently enabled.
     *
     * @return Whether vSync is enabled.
     */
    public boolean vSyncEnabled() {
        return Settings.V_SYNC;
    }

    /**
     * Clear the window.
     */
    public void clear() {
        // Clear framebuffer
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT | GL_STENCIL_BUFFER_BIT);
    }

    /**
     * Register a listener for window events.
     *
     * @param callback The callback function which is called on event firing.
     */
    public void registerListener(Object callback) {
        if (callback instanceof GLFWKeyCallbackI) {
            glfwSetKeyCallback(window, (GLFWKeyCallbackI) callback);
        }
        if (callback instanceof GLFWMouseButtonCallbackI) {
            glfwSetMouseButtonCallback(window, (GLFWMouseButtonCallbackI) callback);
        }
        if (callback instanceof GLFWCursorPosCallbackI) {
            glfwSetCursorPosCallback(window, (GLFWCursorPosCallbackI) callback);
        }
        if (callback instanceof GLFWScrollCallbackI) {
            glfwSetScrollCallback(window, (GLFWScrollCallbackI) callback);
        }
    }

    public void setFullScreen(){
        GLFWVidMode vidmode = glfwGetVideoMode(primaryMonitor);
        glfwSetWindowMonitor(window, primaryMonitor, 0, 0, vidmode.width(), vidmode.height(), Settings.TARGET_FPS);
        fullScreen = true;
    }

    public void setWindowed() {
        // Get primary display resolution
        GLFWVidMode vidmode = glfwGetVideoMode(primaryMonitor);
        // Center window on display
        glfwSetWindowPos(
                window,
                (vidmode.width() - this.width) / 2,
                (vidmode.height() - this.height) / 2
        );
        fullScreen = false;
    }

    public void toggleFullScreen(){
        if (fullScreen) setWindowed();
        else setFullScreen();
    }

}

