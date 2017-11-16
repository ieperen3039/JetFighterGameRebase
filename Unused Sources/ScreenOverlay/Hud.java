package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Engine.GameLoop;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Tools.AutomatedCollection;
import nl.NG.Jetfightergame.Vectors.ScreenCoordinate;

import java.awt.*;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Geert van Ieperen
 * template by Jorren Hendriks
 *
 * provides a way of creating figures on the window above the 3D environment
 */
public class Hud {

    public final AutoFont writer18pt = AutoFont.SansSherif18;
    public final AutoFont writer32pt = AutoFont.arial12pt;
    public final AutoFont writer48pt = AutoFont.SansSherif48;
    public final AutoPainter painter;

    private final List<AutoFont> fonts;

    public Hud() throws IOException, FontFormatException {
        fonts = new LinkedList<>();
        fonts.add(writer18pt);
        fonts.add(writer32pt);
        fonts.add(writer48pt);

        painter = new AutoPainter(new Painter());
    }

    /**
     * cleans the screen of all items
     */
    public void clear(){
        painter.clear();
        fonts.forEach(AutomatedCollection::clear);
    }


    /**
     * draw all buffered elements
     * @param gl
     */
    public void draw(GL2 gl, int screenWidth, int screenHeight) {

        gl.glDisable(GL_LIGHTING);
        gl.glDisable(GL_DEPTH_TEST);

        // store current matrix and set it to identity TODO needed?
        gl.pushMatrix();
        gl.glLoadIdentity();

        // set to viewport
        gl.glMatrixMode(GL_PROJECTION);
        gl.pushMatrix();
        gl.glLoadIdentity();

        // allow 2d rendering (i dont know quite where the text is drawn, 0 or 1, so we keep all options open
        gl.glOrtho(0, screenWidth, screenHeight, 0, -1, 2);

        painter.draw(gl);
        GameLoop.reportGLError(gl);

        // write text
        AutoFont.setFrameDimensions(screenWidth, screenHeight);
        fonts.forEach(f -> f.draw(gl));
        GameLoop.reportGLError(gl);

        // restore previous state
        gl.popMatrix();
        gl.glMatrixMode(GL_MODELVIEW);
        gl.popMatrix();

        gl.glEnable(GL_LIGHTING);
        gl.glEnable(GL_DEPTH_TEST);
    }

    public class AutoPainter extends AutomatedCollection<Painter> {

        /** only the Hud can have a painter */

        protected AutoPainter(Painter executer) {
            super(executer);
        }

        @Override
        protected void drawItems(GL2 gl) {
            gl.beginEnvironment(GL_QUADS);
            {
                executer.setGL(gl);
                executer.setStroke(Settings.MENU_STROKE_WIDTH, Settings.MENU_STROKE_COLOR);
                executer.setFill(Settings.MENU_FILL_COLOR);
                execute();
            }
            gl.endEnvironment();
        }
    }

    public class Painter {

        private int strokeWidth;
        private Color strokeColor;
        private Color fillColor;
        private GL2 gl;

        public void setFill(Color color) {
            fillColor = color;
        }

        public void setStroke(int width, Color color) {
            strokeWidth = width;
            strokeColor = color;
        }

        public Color getFillColor(){
            return fillColor;
        }

        /** @param indent how much of the edges is cut off. results may be unwanted if indent is less than strokewidth */
        public void roundedRectangle(int x, int y, int width, int height, int indent) {
            final int xMax = x + width;
            final int yMax = y + height;

            ScreenCoordinate upLeft1 = new ScreenCoordinate(x, y + indent);
            ScreenCoordinate upLeft2 = new ScreenCoordinate(x + indent, y);
            ScreenCoordinate upRight1 = new ScreenCoordinate(xMax - indent, y);
            ScreenCoordinate upRight2 = new ScreenCoordinate(xMax, y + indent);
            ScreenCoordinate downRight1 = new ScreenCoordinate(xMax, yMax - indent);
            ScreenCoordinate downRight2 = new ScreenCoordinate(xMax - indent, yMax);
            ScreenCoordinate downLeft1 = new ScreenCoordinate(x + indent, yMax);
            ScreenCoordinate downLeft2 = new ScreenCoordinate(x, yMax - indent);

            ScreenCoordinate upLeft1m = upLeft1.add(strokeWidth, strokeWidth);
            ScreenCoordinate upLeft2m = upLeft2.add(strokeWidth, strokeWidth);
            ScreenCoordinate upRight1m = upRight1.add(-strokeWidth, strokeWidth);
            ScreenCoordinate upRight2m = upRight2.add(-strokeWidth, strokeWidth);
            ScreenCoordinate downRight1m = downRight1.add(-strokeWidth, -strokeWidth);
            ScreenCoordinate downRight2m = downRight2.add(-strokeWidth, -strokeWidth);
            ScreenCoordinate downLeft1m = downLeft1.add(strokeWidth, -strokeWidth);
            ScreenCoordinate downLeft2m = downLeft2.add(strokeWidth, -strokeWidth);

            gl.setColor(strokeColor);
            glRectangle(upLeft1, upLeft2, upLeft2m, upLeft1m);
            glRectangle(upLeft2, upRight1, upRight1m, upLeft2m);
            glRectangle(upRight1, upRight2, upRight2m, upRight1m);
            glRectangle(upRight2, downRight1, downRight1m, upRight2m);
            glRectangle(downRight1, downRight2, downRight2m, downRight1m);
            glRectangle(downRight2, downLeft1, downLeft1m, downRight2m);
            glRectangle(downLeft1, downLeft2, downLeft2m, downLeft1m);
            glRectangle(downLeft2, upLeft1, upLeft1m, downLeft2m);

            gl.setColor(fillColor);
            glRectangle(upLeft2m, upLeft1m, upRight2m, upRight1m);
            glRectangle(upLeft1m, upRight2m, downRight1m, downLeft2m);
            glRectangle(downLeft2m, downLeft1m, downRight2m, downRight1m);
        }

        public void image(String filename, int x, int y, int width, int height) throws IOException {
            // TODO
        }

        private void glRectangle(ScreenCoordinate lu, ScreenCoordinate ru, ScreenCoordinate rd, ScreenCoordinate rl){
            gl.glVertex2i(lu.x, lu.y);
            gl.glVertex2i(ru.x, ru.y);
            gl.glVertex2i(rd.x, rd.y);
            gl.glVertex2i(rl.x, rl.y);
        }

        public void setGL(GL2 gl) {
            this.gl = gl;
        }
    }
}
