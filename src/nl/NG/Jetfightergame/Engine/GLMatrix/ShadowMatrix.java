package nl.NG.Jetfightergame.Engine.GLMatrix;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import static nl.NG.Jetfightergame.Vectors.Vector.getRotationMatrix;

/**
 * @author Geert van Ieperen
 *         created on 16-11-2017.
 */
public class ShadowMatrix implements MatrixStack {
    protected DirVector zVec = DirVector.zVector();
    protected DirVector xVec = DirVector.xVector();
    protected DirVector yVec = DirVector.yVector();
    protected PosVector posVec = PosVector.zeroVector();
    /**
     * the matrix pushed on the stack
     */
    protected ShadowMatrix stackedMatrix;
    private DirVector temp = new DirVector();

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
            DirVector rVec = new DirVector();
            xVec
                    .scale(x, temp)
                    .add(yVec.scale(y, temp), rVec)
                    .add(zVec.scale(z, temp), rVec)
                    .normalized(rVec);

            Matrix3f rotationMatrix = getRotationMatrix(rVec.x(), rVec.z(), rVec.y(), angle);
            zVec.mul(rotationMatrix, zVec);
            zVec.mul(rotationMatrix, zVec);
            zVec.mul(rotationMatrix, zVec);
        }
    }

    @Override
    public void translate(float x, float y, float z) {
        if (x != 0.0) posVec.add(xVec.scale(x, temp), posVec);
        if (y != 0.0) posVec.add(yVec.scale(y, temp), posVec);
        if (z != 0.0) posVec.add(zVec.scale(z, temp), posVec);
    }

    @Override
    public void scale(float x, float y, float z) {
        xVec.scale(x, xVec);
        yVec.scale(y, yVec);
        zVec.scale(z, zVec);
    }

    /**
     * do not use this method for directions!
     * @see #getDirection(DirVector)
     * @param p coordinate in local space
     * @return p relative to reference frame of instantiation
     */
    @Override
    public PosVector getPosition(PosVector p){
        PosVector newPos = new PosVector(posVec);

        if (p.x != 0.0) newPos.add(xVec.scale(p.x, temp), newPos);
        if (p.y != 0.0) newPos.add(yVec.scale(p.y, temp), newPos);
        if (p.z != 0.0) newPos.add(zVec.scale(p.z, temp), newPos);
        return newPos;
    }

    /**
     * @param v vector in local space
     * @return direction in global space
     */
    @Override
    public DirVector getDirection(DirVector v){
        DirVector newDir = DirVector.zeroVector();
        if (v.x != 0.0) newDir.add(xVec.scale(v.x, temp), newDir);
        if (v.y != 0.0) newDir.add(yVec.scale(v.y, temp), newDir);
        if (v.z != 0.0) newDir.add(zVec.scale(v.z, temp), newDir);
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
        PosVector newPos = PosVector.zeroVector();
        if (x != 0.0) newPos.subtract(xVec.scale(1/x, temp), newPos);
        if (y != 0.0) newPos.subtract(yVec.scale(1/y, temp), newPos);
        if (z != 0.0) newPos.subtract(zVec.scale(1/z, temp), newPos);
        return newPos.subtract(posVec, newPos);
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
    public String toString() {
        return String.format("[O: %s X: %s, Y: %s, Z: %s]", posVec, xVec, yVec, zVec);
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
