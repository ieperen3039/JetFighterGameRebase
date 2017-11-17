package nl.NG.Jetfightergame.Engine.GLMatrix;

import nl.NG.Jetfightergame.GameObjects.Structures.Shape;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;

import java.awt.*;

/**
 * Created by Geert van Ieperen on 25-1-2017.
 * a class that tracks the position of the origin.
 */
public class AxisBasedGL extends ShadowMatrix {

    private final GL2 delegate;

    public AxisBasedGL(GL2 gl) {
        if (gl instanceof AxisBasedGL) {
            delegate = ((AxisBasedGL) gl).delegate;
        } else {
            delegate = gl;
        }
    }

    @Override
    public void clearColor() {
        delegate.clearColor();
    }

    @Override
    public void pushMatrix() {
        //save current state of matrix
        stackedMatrix = new AxisBasedGL(delegate);
        stackedMatrix.setStateTo(this);
    }

    @Override
    public void setColor(double red, double green, double blue) {
        delegate.setColor(red, green, blue);
    }

    @Override
    public void setLight(int lightNumber, DirVector dir, Color lightColor) {
        delegate.setLight(lightNumber, dir, lightColor);
    }

    @Override
    public void setLight(int lightNumber, PosVector pos, Color lightColor) {
        delegate.setLight(lightNumber, pos, lightColor);
    }

    @Override
    public void setMaterial(Material material) {
        delegate.setMaterial(material);
    }

    @Override
    public void draw(Shape object) {
        delegate.draw(object);
    }
}
