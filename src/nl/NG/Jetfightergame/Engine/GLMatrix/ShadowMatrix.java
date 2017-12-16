package nl.NG.Jetfightergame.Engine.GLMatrix;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;
import org.joml.Matrix4f;

import static nl.NG.Jetfightergame.Vectors.Vector.getRotationMatrix;

/**
 * @author Geert van Ieperen
 *         created on 16-11-2017.
 */
public class ShadowMatrix implements MatrixStack {
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

    @Override
    public void rotate(float angle, float x, float y, float z) {
        if (angle != 0.0) {
            // transform rotation vector to local space
            DirVector rVec = xVec.scale(x)
                    .add(yVec.scale(y))
                    .add(zVec.scale(z))
                    .normalized();

            float[] rotationMatrix = getRotationMatrix(rVec.x(), rVec.z(), rVec.y(), angle);
            zVec = zVec.multiplyMatrix(rotationMatrix).toDirVector();
            xVec = xVec.multiplyMatrix(rotationMatrix).toDirVector();
            yVec = yVec.multiplyMatrix(rotationMatrix).toDirVector();
        }
    }

    @Override
    public void translate(float x, float y, float z) {
        if (x != 0.0) posVec = posVec.add(xVec.scale(x));
        if (y != 0.0) posVec = posVec.add(yVec.scale(y));
        if (z != 0.0) posVec = posVec.add(zVec.scale(z));
    }

    @Override
    public void scale(float x, float y, float z) {
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
        float x = p.x();
        float y = p.y();
        float z = p.z();
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
        float x = p.x();
        float y = p.y();
        float z = p.z();
        Vector newPos = DirVector.O;
        if (x != 0.0) newPos = newPos.subtract(xVec.scale(1/x));
        if (y != 0.0) newPos = newPos.subtract(yVec.scale(1/y));
        if (z != 0.0) newPos = newPos.subtract(zVec.scale(1/z));
        return newPos.subtract(posVec).toPosVector();
    }

    /**
     * the already stacked matrix will be passed to the new stacked matrix
     */
    @Override
    public void pushMatrix() {
        //save current state of matrix
        stackedMatrix = new ShadowMatrix();
        stackedMatrix.setStateTo(this);
    }

    /**
     * the stacked matrix of this stacked matrix will become the new stacked matrix
     */
    @Override
    public void popMatrix() {
        setStateTo(stackedMatrix);
    }

    /**
     * print current state of axis system
     */
    public void printAll() {
        System.out.printf("xVec: %s, yVec: %s, zVec: %s%n", xVec, yVec, zVec);
        System.out.println("position Vec: " + posVec);
    }

    // unsupported operations

    @Override
    public void multiplyAffine(Matrix4f preTransformation) { //TODO look for possibility to implement
        throw new UnsupportedOperationException("ShadowMatrix has not yet implemented multiplyAffine");
    }

    @Override
    public void popAll() {
        // it is error-handling after all
        setStateTo(new ShadowMatrix());
    }
}
