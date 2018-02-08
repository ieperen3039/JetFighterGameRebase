package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.Rendering.GLFWWindow;
import nl.NG.Jetfightergame.Tools.Resource;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import org.joml.Vector2i;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static nl.NG.Jetfightergame.ScreenOverlay.MenuStyleSettings.*;
import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * @author Jorren
 * semi-singleton design to assist debug output to HUD
 */
public class ScreenOverlay {

    private static long vg;
    private static NVGColor color;
    private static NVGPaint paint;

    /** fontbuffer MUST be a field */
    @SuppressWarnings("FieldCanBeLocal")
    private static final ByteBuffer[] fontBuffer = new ByteBuffer[Font.values().length];
    private static Map<String, Integer> imageBuffer = new HashMap<>();

    private static final Collection<Consumer<Painter>> menuDrawBuffer = new ArrayList<>();
    private static final Collection<Consumer<Painter>> hudDrawBuffer = new ArrayList<>();
    private static BooleanSupplier menuMode;

    private static final Lock menuBufferLock = new ReentrantLock();
    private static final Lock hudBufferLock = new ReentrantLock();

    public static boolean isMenuMode() {
        return menuMode.getAsBoolean();
    }

    public enum Font {
        ORBITRON_REGULAR("res/fonts/Orbitron/Orbitron-Regular.ttf"),
        ORBITRON_MEDIUM("res/fonts/Orbitron/Orbitron-Medium.ttf"),
        ORBITRON_BOLD("res/fonts/Orbitron/Orbitron-Bold.ttf"),
        ORBITRON_BLACK("res/fonts/Orbitron/Orbitron-Black.ttf"),
        LUCIDA_CONSOLE("res/fonts/LucidaConsole/lucon.ttf");

        public final String name;
        public final String source;

        Font(String source) {
            this.name = toString();
            this.source = source;
        }
    }

    /**
     * returns the Hud
     *
     * @throws IOException If an error occures during the setup of the Hud.
     * @param menuMode
     */
    public static void initialize(BooleanSupplier menuMode) throws IOException {
        new ScreenOverlay(menuMode);
    }


    private ScreenOverlay(BooleanSupplier menuMode) throws IOException {
        ScreenOverlay.menuMode = menuMode;

        vg = GLFWWindow.antialiasing() ? nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES) : nvgCreate(NVG_STENCIL_STROKES);
        if (vg == NULL) {
            throw new IOException("Could not initialize NanoVG");
        }
        Font[] fonts = Font.values();

        for (int i = 0; i < fonts.length; i++) {
            fontBuffer[i] = Resource.toByteBuffer(fonts[i].source, 96 * 1024);
            if (nvgCreateFontMem(vg, fonts[i].name, fontBuffer[i], 1) == -1) {
                throw new IOException("Could not create font " + fonts[i].name);
            }
        }

        color = NVGColor.create();
        paint = NVGPaint.create();
    }

    /**
     * Create something for the hud to be drawn. Package the NanoVG drawObjects commands inside a
     * {@link Consumer<Painter>} which will execute {@link Painter} commands once the Hud is ready to draw
     *
     * @param render The code for drawing inside the hud.
     */
    public static void addMenuItem(Consumer<Painter> render) {
        menuBufferLock.lock();
        menuDrawBuffer.add(render);
        menuBufferLock.unlock();
    }

    /**
     * Remove an existing drawObjects handler from the Hud.
     *
     * @param render The handler to remove.
     */
    public static void removeMenuItem(Consumer<Painter> render) {
        menuBufferLock.lock();
        try {
            menuDrawBuffer.remove(render);
        } finally {
            menuBufferLock.unlock();
        }
    }

    /** clear the menu drawBuffer */
    public static void removeMenuItem() {
        menuBufferLock.lock();
        menuDrawBuffer.clear();
        menuBufferLock.unlock();
    }

    /**
     * Create something for the hud to be drawn. Package the NanoVG drawObjects commands inside a
     * {@link Consumer<Painter>} which will execute {@link Painter} commands once the Hud is ready to draw
     *
     * @param render The code for drawing inside the hud.
     */
    public static void addHudItem(Consumer<Painter> render) {
        hudBufferLock.lock();
        hudDrawBuffer.add(render);
        hudBufferLock.unlock();
    }

    /**
     * Remove an existing drawObjects handler from the Hud.
     *
     * @param render The handler to remove.
     */
    public static void removeHudItem(Consumer<Painter> render) {
        hudBufferLock.lock();
        try {
            hudDrawBuffer.remove(render);
        } finally {
            hudBufferLock.unlock();
        }
    }

    /** clear the hud drawBuffer */
    public static void removeHudItem(){
        hudBufferLock.lock();
        hudDrawBuffer.clear();
        hudBufferLock.unlock();
    }

    public static class Painter {
        private static final int PRINTROLLSIZE = 24;
        private final int yPrintRoll = PRINTROLLSIZE + 5;
        private final int xPrintRoll = 5;
        private int printRollEntry = 0;

        /**
         * Get an instance of NVGColor with the correct values. All color values are floating point numbers supposed to be
         * between 0f and 1f.
         *
         * @param red   The red component.
         * @param green The green component.
         * @param blue  The blue component.
         * @param alpha The alpha component.
         * @return an instance of NVGColor.
         */
        public NVGColor rgba(float red, float green, float blue, float alpha) {
            color.r(red);
            color.g(green);
            color.b(blue);
            color.a(alpha);

            return color;
        }

        private NVGColor rgba(Color4f color) {
            return rgba(color.red, color.green, color.blue, color.alpha);
        }

        public void rectangle(int x, int y, int width, int height) {
            rectangle(x, y, width, height, MENU_FILL_COLOR, MENU_STROKE_WIDTH, MENU_STROKE_COLOR);
        }

        public void rectangle(int x, int y, int width, int height, Color4f fillColor, int strokeWidth, Color4f strokeColor) {
            nvgBeginPath(vg);
            nvgRect(vg, x, y, width, height);

            fill(fillColor);
            stroke(strokeWidth, strokeColor);
        }

        public void roundedRectangle(int x, int y, int width, int height, int indent) {
            roundedRectangle(x, y, width, height, indent, MENU_FILL_COLOR, MENU_STROKE_COLOR, MENU_STROKE_WIDTH);
        }

        public void roundedRectangle(int x, int y, int width, int height, int indent, Color4f fillColor, Color4f strokeColor, int strokeWidth) {
            int xMax = x + width;
            int yMax = y + height;

            polygon(
                    fillColor, strokeColor, strokeWidth,
                    new Vector2i(x + indent, y),
                    new Vector2i(xMax - indent, y),
                    new Vector2i(xMax, y + indent),
                    new Vector2i(xMax, yMax - indent),
                    new Vector2i(xMax - indent, yMax),
                    new Vector2i(x + indent, yMax),
                    new Vector2i(x, yMax - indent),
                    new Vector2i(x, y + indent)
            );
        }

        /** @see #circle(int, int, int, Color4f, int, Color4f)  */
        public void circle(int x, int y, int radius) {
            circle(x, y, radius, MENU_FILL_COLOR, MENU_STROKE_WIDTH, MENU_STROKE_COLOR);
        }

        /**
         * draws a circle.
         * x and y are the circle middle.
         * x, y and radius are in screen coordinates.
         */
        public void circle(int x, int y, int radius, Color4f fillColor, int strokeWidth, Color4f strokeColor) {
            nvgBeginPath(vg);
            nvgCircle(vg, x, y, radius);

            fill(fillColor);
            stroke(strokeWidth, strokeColor);
        }

        public void polygon(Vector2i... points) {
            polygon(MENU_FILL_COLOR, MENU_STROKE_COLOR, MENU_STROKE_WIDTH, points);
        }

        public void polygon(Color4f fillColor, Color4f strokeColor, int strokeWidth, Vector2i... points) {
            nvgBeginPath(vg);

            nvgMoveTo(vg, points[points.length - 1].x, points[points.length - 1].y);
            for (Vector2i point : points) {
                nvgLineTo(vg, point.x, point.y);
            }

            fill(fillColor);
            stroke(strokeWidth, strokeColor);
        }

        /**
         * draw a line along the coordinates, when supplied in (x, y) pairs
         * @param points (x, y) pairs of screen coordinates
         */
        public void line(int strokeWidth, Color4f strokeColor, int... points) {
            nvgBeginPath(vg);

            int i = 0;
            nvgMoveTo(vg, points[i++], points[i++]);
            while (i < points.length) {
                nvgLineTo(vg, points[i++], points[i++]);
            }

            stroke(strokeWidth, strokeColor);
        }

        // non-shape defining functions

        public void text(int x, int y, float size, Font font, int align, Color4f color, String text) {
            nvgFontSize(vg, size);
            nvgFontFace(vg, font.name);
            nvgTextAlign(vg, align);
            nvgFillColor(vg, rgba(color));
            nvgText(vg, x, y, text);
        }

        public void printRoll(String text){
            int y = yPrintRoll + ((PRINTROLLSIZE + 5) * printRollEntry);

            text(xPrintRoll, y, PRINTROLLSIZE, Font.LUCIDA_CONSOLE, NVG_ALIGN_LEFT, Color4f.WHITE, text);
            printRollEntry++;
        }

        private void fill(float red, float green, float blue, float alpha) {
            nvgFillColor(vg, rgba(red, green, blue, alpha));
            nvgFill(vg);
        }

        private void fill(Color4f color) {
            fill(color.red, color.green, color.blue, color.alpha);
        }

        private void stroke(int width, float red, float green, float blue, float alpha) {
            nvgStrokeWidth(vg, width);
            nvgStrokeColor(vg, rgba(red, green, blue, alpha));
            nvgStroke(vg);
        }

        private void stroke(int width, Color4f color) {
            stroke(width, color.red, color.green, color.blue, color.alpha);
        }

        public void image(String filename, int x, int y, int width, int height, float alpha) throws IOException {
            image(filename, x, y, width, height, 0f, alpha, NVG_IMAGE_GENERATE_MIPMAPS);
        }

        public void image(String fileName, int x, int y, int width, int height, float angle, float alpha, int imageFlags) throws IOException {
            int img = getImage(fileName, imageFlags);
            NVGPaint p = nvgImagePattern(vg, x, y, width, height, angle, img, alpha, paint);

            rectangle(x, y, width, height);

            nvgFillPaint(vg, p);
            nvgFill(vg);
        }

        private int getImage(String filename, int imageFlags) throws IOException {
            if (imageBuffer.containsKey(filename)) {
                return imageBuffer.get(filename);
            }
            ByteBuffer image = Resource.toByteBuffer(filename, 1);
            int img = nvgCreateImageMem(vg, imageFlags, image);
            imageBuffer.put(filename, img);
            return img;
        }
    }

    public synchronized static void draw(int windowWidth, int windowHeight) {
        // Begin NanoVG frame
        nvgBeginFrame(vg, windowWidth, windowHeight, 1);

        Painter vanGogh = new Painter();
        // Draw the right drawhandlers
        if (isMenuMode()) {
            menuBufferLock.lock();
            try {
                menuDrawBuffer.forEach(m -> m.accept(vanGogh));
            } finally {
                menuBufferLock.unlock();
            }
        } else {
            hudBufferLock.lock();
            try {
                hudDrawBuffer.forEach(m -> m.accept(vanGogh));
            } finally {
                hudBufferLock.unlock();
            }
        }

        // End NanoVG frame
        nvgEndFrame(vg);

        // restore window state
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
    }
}
