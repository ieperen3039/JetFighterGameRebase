package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.Engine.GLFWWindow;
import nl.NG.Jetfightergame.Tools.Resource;
import nl.NG.Jetfightergame.Vectors.Color4f;
import org.joml.Vector2i;
import org.lwjgl.nanovg.NVGColor;
import org.lwjgl.nanovg.NVGPaint;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import static org.lwjgl.nanovg.NanoVG.*;
import static org.lwjgl.nanovg.NanoVGGL3.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * @author Jorren
 */
public class ScreenOverlay {

    private long vg;
    private NVGColor color;
    private NVGPaint paint;
    private ByteBuffer[] fontBuffer;
    private Map<String, Integer> imageBuffer;

    private Collection<Consumer<Painter>> menuDrawBuffer;
    private Collection<Consumer<Painter>> hudDrawBuffer;
    private final BooleanSupplier menuMode;
    private Consumer<Painter> menuInitialisation;
    private Consumer<Painter> hudInitialisation;

    public enum Font {
        ORBITRON_REGULAR("res/fonts/Orbitron-Regular.ttf"),
        ORBITRON_MEDIUM("res/fonts/Orbitron-Medium.ttf"),
        ORBITRON_BOLD("res/fonts/Orbitron-Bold.ttf"),
        ORBITRON_BLACK("res/fonts/Orbitron-Black.ttf");

        public final String name;
        public final String source;

        Font(String source) {
            this.name = toString();
            this.source = source;
        }
    }

    /**
     * Initialize the Hud.
     *
     * @param menuMode
     * @throws IOException If an error occures during the setup of the Hud.
     */
    public ScreenOverlay(BooleanSupplier menuMode) throws IOException {
        this.menuMode = menuMode;
        vg = GLFWWindow.antialiasing() ? nvgCreate(NVG_ANTIALIAS | NVG_STENCIL_STROKES) :
                nvgCreate(NVG_STENCIL_STROKES);
        if (this.vg == NULL) {
            throw new IOException("Could not initialize NanoVG");
        }

        fontBuffer = new ByteBuffer[Font.values().length];
        int i = 0;
        for (Font font : Font.values()) {
            fontBuffer[i] = Resource.toByteBuffer(font.source, 96 * 1024);
            if (nvgCreateFontMem(vg, font.name, fontBuffer[i], 1) == -1) {
                throw new IOException("Could not create font " + font.name);
            }
            i++;
        }

        imageBuffer = new HashMap<>();
        color = NVGColor.create();
        paint = NVGPaint.create();

        menuDrawBuffer = new ArrayList<>();
        hudDrawBuffer = new ArrayList<>();
    }

    /**
     * Create something for the hud to be drawn. Package the NanoVG drawObjects commands inside a
     * {@link Consumer<Painter>} which will execute {@link Painter} commands once the Hud is ready to draw
     *
     * @param render The code for drawing inside the hud.
     */
    public void addMenuItem(Consumer<Painter> render) {
        menuDrawBuffer.add(render);
    }

    /**
     * Remove an existing drawObjects handler from the Hud.
     *
     * @param render The handler to remove.
     */
    public void removeMenuItem(Consumer<Painter> render) {
        menuDrawBuffer.remove(render);
    }

    /** clear the menu drawBuffer */
    public void removeMenuItems() {
        menuDrawBuffer.clear();
    }

    public void setMenuInitialisation(Consumer<Painter> init){
        menuInitialisation = init;
    }

    /**
     * Create something for the hud to be drawn. Package the NanoVG drawObjects commands inside a
     * {@link Consumer<Painter>} which will execute {@link Painter} commands once the Hud is ready to draw
     *
     * @param render The code for drawing inside the hud.
     */
    public void addHudItem(Consumer<Painter> render) {
        hudDrawBuffer.add(render);
    }

    /**
     * Remove an existing drawObjects handler from the Hud.
     *
     * @param render The handler to remove.
     */
    public void removeHudItem(Consumer<Painter> render) {
        hudDrawBuffer.remove(render);
    }

    /** clear the hud drawBuffer */
    public void removeHudItems(){
        hudDrawBuffer.clear();
    }

    public void setHudInitialisation(Consumer<Painter> init){
        hudInitialisation = init;
    }

    public class Painter {

        Color4f textColor = Color4f.BLACK;

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

        /**
         * {@link #rgba(float, float, float, float)}
         */
        private NVGColor rgba(Color4f color) {
            return rgba(color.red, color.green, color.blue, color.alpha);
        }


        public void rectangle(int x, int y, int width, int height) {
            nvgBeginPath(vg);
            nvgRect(vg, x, y, width, height);
        }

        public void roundedRectangle(int x, int y, int width, int height, int indent) {
            int xMax = x + width;
            int yMax = y + height;


            polygon(
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

        public void circle(int x, int y, int radius) {
            nvgBeginPath(vg);
            nvgCircle(vg, x, y, radius);
        }

        public void polygon(Vector2i... points) {
            if (points.length == 0) {
                throw new IllegalArgumentException("Must pass at least 2 points");
            }
            nvgBeginPath(vg);

            nvgMoveTo(vg, points[points.length - 1].x, points[points.length - 1].y);
            for (Vector2i point : points) {
                nvgLineTo(vg, point.x, point.y);
            }
        }

        public void text(int x, int y, float size, Font font, int align, String text) {
            nvgFontSize(vg, size);
            nvgFontFace(vg, font.name);
            nvgTextAlign(vg, align);
            nvgFillColor(vg, rgba(textColor));
            nvgText(vg, x, y, text);
        }

        public void setFillColor(float red, float green, float blue, float alpha) {
            nvgFillColor(vg, rgba(red, green, blue, alpha));
            nvgFill(vg);
        }

        public void setFillColor(Color4f color){
            setFillColor(color.red, color.green, color.blue, color.alpha);
        }

        public void setStrokeColor(float red, float green, float blue, float alpha) {
            nvgStrokeColor(vg, rgba(red, green, blue, alpha));
            nvgStroke(vg);
        }

        public void setStrokeWidth(int width) {
            nvgStrokeWidth(vg, width);
        }

        public void setStrokeColor(Color4f color) {
            setStrokeColor(color.red, color.green, color.blue, color.alpha);
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

        public void setTextColor(Color4f textColor) {
            this.textColor = textColor;
        }
    }

    /**
     * draws the buffered objects on the gl context.
     * If menumode evaluates to true, draws the menu buttons, otherwise it draws the hud.
     * @param windowWidth
     * @param windowHeight
     */
    public void draw(int windowWidth, int windowHeight) {
        // Begin NanoVG frame
        nvgBeginFrame(vg, windowWidth, windowHeight, 1);

        Painter vanGogh = new Painter();
        // Draw the right drawhandlers
        if (menuMode.getAsBoolean()) {
            menuInitialisation.accept(vanGogh);
            menuDrawBuffer.forEach(m -> m.accept(vanGogh));
        } else {
            hudInitialisation.accept(vanGogh);
            hudDrawBuffer.forEach(m -> m.accept(vanGogh));
        }

        // End NanoVG frame
        nvgEndFrame(vg);

        // restore window state
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_STENCIL_TEST);
    }

}
