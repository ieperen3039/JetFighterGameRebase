package nl.NG.Jetfightergame.Rendering;

import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import org.joml.Vector2i;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_SMOOTH_LINE_WIDTH_RANGE;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * @author Jorren Hendriks
 * <p>
 * A window which initializes GLFW and manages it.
 */
public class GLFWWindow {
    private static final boolean GL_DEBUG_MESSAGES = false;
    private final long primaryMonitor;

    private final String title;
    private final boolean resizable;
    // buffers for mouse input
    private final DoubleBuffer mousePosX;
    private final DoubleBuffer mousePosY;

    private long window;
    private int width;
    private int height;
    private boolean fullScreen = false;
    private boolean mouseIsCaptured;

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
            throw new GLException("Unable to initialize GLFW");
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
            glfwWindowHint(GLFW_STENCIL_BITS, ClientSettings.ANTIALIAS);
            glfwWindowHint(GLFW_SAMPLES, ClientSettings.ANTIALIAS);
        }

        window = getWindow(this.width, this.height);
        primaryMonitor = glfwGetPrimaryMonitor();

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

        final FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(2);
        glGetFloatv(GL_SMOOTH_LINE_WIDTH_RANGE, floatBuffer);
        ClientSettings.HIGHLIGHT_LINE_WIDTH = Math.max(floatBuffer.get(), ClientSettings.HIGHLIGHT_LINE_WIDTH);
        ClientSettings.HIGHLIGHT_LINE_WIDTH = Math.min(floatBuffer.get(), ClientSettings.HIGHLIGHT_LINE_WIDTH);

        if (ServerSettings.DEBUG && GL_DEBUG_MESSAGES) {
            glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
            GLUtil.setupDebugMessageCallback();
        }

        ServerSettings.RENDER_ENABLED = true;
        Toolbox.checkGLError();
    }

    public static boolean antialiasing() {
        return ClientSettings.ANTIALIAS > 0;
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
     */
    public void update() {
        // Swap buffers
        glfwSwapBuffers(window);

        // Poll for events
        glfwPollEvents();

        clear();
    }

    /**
     * saves a copy of the front buffer (the display) to disc
     * @param filename the file to save to
     * @return
     */
    @SuppressWarnings("NumericOverflow")
    public boolean printScreen(String filename) {
        glReadBuffer(GL11.GL_FRONT);
        int bpp = 4; // Assuming a 32-bit display with a byte each for red, green, blue, and alpha.
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
        glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer );
        Toolbox.checkGLError();

        File file = new File("ScreenShots/" + filename + ".png"); // The file to save to.
        boolean success = file.mkdirs();
        if (!success) return false;

        String format = "PNG";
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int i = (x + (width * y)) * bpp;
                int r = buffer.get(i) & 0xFF;
                int g = buffer.get(i + 1) & 0xFF;
                int b = buffer.get(i + 2) & 0xFF;
                image.setRGB(x, height - (y + 1), (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
        }

        try {
            ImageIO.write(image, format, file);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * hints the window to close
     */
    public void close() {
        glfwSetWindowShouldClose(window, true);
    }

    public void open() {
        // Show window
        glfwShowWindow(window);
    }

    /**
     * Terminate GLFW and release GLFW error callback
     */
    public void cleanup() {
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);
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
     * @return the position of the cursor, in screen coordinates, relative to the upper-left corner of the client area of the specified window
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
        return ClientSettings.V_SYNC;
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
        glfwSetWindowMonitor(window, primaryMonitor, 0, 0, vidmode.width(), vidmode.height(), GLFW_DONT_CARE);
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

    /**
     * sets mouse to invisible and restrict movement
     */
    public void capturePointer(){
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        mouseIsCaptured = true;
    }

    /**
     * sets mouse to visible
     */
    public void freePointer(){
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
        mouseIsCaptured = false;
    }

    public boolean isMouseCaptured(){
        return mouseIsCaptured;
    }

    public void setClearColor(Color4f color4f) {
        setClearColor(color4f.red, color4f.green, color4f.blue, color4f.alpha);
    }

}

