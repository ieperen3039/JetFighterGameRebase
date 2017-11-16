package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;

import java.awt.*;
import java.io.File;
import java.nio.ByteBuffer;

/**
 * whip up a 256x256 luminance alpha texture and divide it into a grid of 8x8. Paint characters in there using some
 * drawing tool like PSP. Then you can just render strings by drawing textured quads, using the character value to
 * look up the coordinates.
 *
 * @author adamprice
 * @author completely revised by Geert van Ieperen
 * Date: Nov 13, 2003
 */
public class TextureFontRenderer implements FontRenderer {

    public static TextureFontRenderer testFont16pt = new TextureFontRenderer(new TextureFont("", 16));

    private final TextureFont font;
    private GL2 gl;
    private Color color;

    public TextureFontRenderer(TextureFont textureFont) {
        this.font = textureFont;
    }

    @Override
    public void draw(String message, int xPos, int yPos) {
        float scale = 1f;

        gl.glListBase(-31+(8*16*font.id));
        gl.setColor(color);
        ByteBuffer scratch = ByteBuffer.allocateDirect(message.getBytes().length);
        scratch.put(message.getBytes());
        scratch.rewind();
        gl.glTranslatef(xPos, yPos,0f);
        gl.pushMatrix();
        {
            gl.glScalef(scale, scale, 1.0f);
            gl.glCallLists(scratch.remaining(), GL.GL_BYTE, scratch);
            gl.glTranslatef(-message.length() * font.fontSize, 0f, 0f);
        }
        gl.popMatrix();
    }

    @Override
    public void setColor(Color newColor) {
        this.color = newColor;
    }

    @Override
    public void beginRendering(GL2 gl, int screenWidth, int screenHeight) {
        this.gl = gl;
    }

    @Override
    public void endRendering() {

    }

    @Override
    public int getFontSize() {
        return font.fontSize;
    }
}

class TextureFont {
    private static int nextFontNumber;
    public final int fontSize;
    public final int id;
    private final String path;

    TextureFont(String path, int fontSize) {
        this.fontSize = fontSize;
        this.path = path;
        id = nextFontNumber++;
    }

    private static void buildTexture(GL2 gl, Texture texture, int id, int fontSize) {
        int listBase = gl.glGenLists(8 * 16 * id);
        int cnt = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 16; x++, cnt++) {
                gl.glNewList(listBase + cnt, GL2.GL_COMPILE);
                drawTexture2D(gl, texture, y, x, fontSize);
                gl.glEndList();
            }
        }
    }

    private static void drawTexture2D(GL2 gl, Texture texture, int y, int x, int fontSize) {
        texture.bind(gl);
        gl.beginEnvironment(GL2.GL_QUADS);
        {
            // Use A Quad For Each Character
            gl.glMultiTexCoord2f(GL2.GL_TEXTURE0, x / 16f, y / 8f);
            gl.glVertex2f(0f, 0f);
            gl.glMultiTexCoord2f(GL2.GL_TEXTURE0, x / 16f, (y + 1) / 8f);
            gl.glVertex2f(0f, fontSize);
            gl.glMultiTexCoord2f(GL2.GL_TEXTURE0, (x + 1) / 16f, (y + 1) / 8f);
            gl.glVertex2f(fontSize, fontSize);
            gl.glMultiTexCoord2f(GL2.GL_TEXTURE0, (x + 1) / 16f, y / 8f);
            gl.glVertex2f(fontSize, 0f);
        }
        gl.endEnvironment();
        gl.glTranslatef(fontSize, 0f, 0f);
    }

    private static Texture loadTexture(String path) {
        Texture result = null;
        try {
            // Try to load from local folder.
            result = TextureIO.newTexture(new File(path), false);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    private void generate(GL2 gl){
        buildTexture(gl, loadTexture(path), id, fontSize);
    }
}
