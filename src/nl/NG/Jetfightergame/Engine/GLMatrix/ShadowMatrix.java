package nl.NG.Jetfightergame.Engine.GLMatrix;

import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;

import java.awt.*;

import static nl.NG.Jetfightergame.Vectors.Vector.getRotationMatrix;

/**
 * @author Geert van Ieperen
 *         created on 16-11-2017.
 */
public class ShadowMatrix implements GL2 {
    protected DirVector zVec = DirVector.Z;
    protected DirVector xVec = DirVector.X;
    protected DirVector yVec = DirVector.Y;
    protected PosVector posVec = PosVector.O;
    /**
     * the matrix pushed on the stack
     */
    protected ShadowMatrix stackedMatrix;

    /**
     * set the state of this matrix to that of another matrix
     */
    protected void setStateTo(ShadowMatrix master) {
        this.zVec = master.zVec;
        this.xVec = master.xVec;
        this.yVec = master.yVec;
        this.posVec = master.posVec;
        this.stackedMatrix = master.stackedMatrix;
    }

    /**
     * @param angle in DEGREES
     */
    public void rotate(double angle, double x, double y, double z) {
        if (angle != 0.0) {
            // transform rotation vector to local space
            DirVector rVec = xVec.scale(x)
                    .add(yVec.scale(y))
                    .add(zVec.scale(z))
                    .normalized();

            double[] rotationMatrix = getRotationMatrix(rVec.x(), rVec.y(), rVec.z(), angle);
            zVec = zVec.multiplyMatrix(rotationMatrix).toDirVector();
            xVec = xVec.multiplyMatrix(rotationMatrix).toDirVector();
            yVec = yVec.multiplyMatrix(rotationMatrix).toDirVector();
        }
    }

    /**
     */
    public void translate(double x, double y, double z) {
        if (x != 0.0) posVec = posVec.add(xVec.scale(x));
        if (y != 0.0) posVec = posVec.add(yVec.scale(y));
        if (z != 0.0) posVec = posVec.add(zVec.scale(z));
    }

    public void translate(Vector v) {
        translate(v.x(), v.y(), v.z());
    }

    /**
     */
    public void scale(double x, double y, double z) {
        xVec = xVec.scale(x);
        yVec = yVec.scale(y);
        zVec = zVec.scale(z);
    }

    /**
     * do not use this method for directions!
     * @see #getDirection(DirVector)
     * @param p coordinate in local space
     * @return p relative to reference frame of instantiation
     */
    @Override
    public PosVector getPosition(PosVector p){
        double x = p.x();
        double y = p.y();
        double z = p.z();
        PosVector newPos = posVec;
        if (x != 0.0) newPos = newPos.add(xVec.scale(x));
        if (y != 0.0) newPos = newPos.add(yVec.scale(y));
        if (z != 0.0) newPos = newPos.add(zVec.scale(z));
        return newPos;
    }

    /**
     * @param v vector in local space
     * @return direction in global space
     */
    @Override
    public DirVector getDirection(DirVector v){
        DirVector newDir = xVec.scale(v.x());
        newDir = newDir.add(yVec.scale(v.y()));
        newDir = newDir.add(zVec.scale(v.z()));
        return newDir;
    }

    /**
     * @param p vector relative to reference frame if instantiation
     * @return coordinates relative to local-space
     */
    public PosVector getReversePosition(PosVector p){
        double x = p.x();
        double y = p.y();
        double z = p.z();
        Vector newPos = DirVector.O;
        if (x != 0.0) newPos = newPos.subtract(xVec.scale(1/x));
        if (y != 0.0) newPos = newPos.subtract(yVec.scale(1/y));
        if (z != 0.0) newPos = newPos.subtract(zVec.scale(1/z));
        return newPos.subtract(posVec).toPosVector();
    }

    @Override
    public void pushMatrix() {
        //save current state of matrix
        stackedMatrix = new ShadowMatrix();
        stackedMatrix.setStateTo(this);
    }

    /**
     * the stacked matrix of this stacked matrix will become the new stacked matrix
     */
    public void popMatrix() {
        setStateTo(stackedMatrix);
    }


    @Override
    public void matrixMode(int matrix) {
        throw new UnsupportedOperationException("ShadowMatrix can't switch matrix mode");
    }

    /**
     * print current state of axis system
     */
    public void printAll() {
        System.out.printf("xVec: %s, yVec: %s, zVec: %s%n", xVec, yVec, zVec);
        System.out.println("position Vec: " + posVec);
    }

    @Override
    public void setColor(double red, double green, double blue) {
        throw new UnsupportedOperationException("ShadowMatrix can't do rendering operations!");
    }

    @Override
    public void setLight(int lightNumber, DirVector dir, Color lightColor) {
        throw new UnsupportedOperationException("ShadowMatrix can't do rendering operations!");
    }

    @Override
    public void setLight(int lightNumber, PosVector pos, Color lightColor) {
        throw new UnsupportedOperationException("ShadowMatrix can't do rendering operations!");
    }

    @Override
    public void setMaterial(Material material) {
        throw new UnsupportedOperationException("ShadowMatrix can't do rendering operations!");
    }

    @Override
    public void clearColor() {
        throw new UnsupportedOperationException("ShadowMatrix can't do rendering operations!");
    }
}
