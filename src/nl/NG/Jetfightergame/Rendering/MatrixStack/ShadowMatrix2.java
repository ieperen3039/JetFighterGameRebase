package nl.NG.Jetfightergame.Rendering.MatrixStack;

import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.AxisAngle4f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Stack;

/**
 * @author Geert van Ieperen
 *         created on 16-11-2017.
 */
public class ShadowMatrix2 implements MatrixStack {
    protected PosVector posVec = PosVector.zeroVector();
    protected DirVector xVec = DirVector.xVector();
    protected DirVector yVec = DirVector.yVector();
    protected DirVector zVec = DirVector.zVector();

    /**
     * the matrix stack
     */
    private Stack<ShadowMatrix2> stack = new Stack<>();
    private DirVector temp = new DirVector();

    /**
     * copies the state of another matrix to this matrix
     */
    protected void setStateTo(ShadowMatrix2 master) {
        this.posVec = new PosVector(master.posVec);
        this.xVec = new DirVector(master.xVec);
        this.zVec = new DirVector(master.zVec);
        this.yVec = new DirVector(master.yVec);
    }

    @Override
    public void rotate(float angle, float x, float y, float z) {
        Quaternionf rotation = new Quaternionf(new AxisAngle4f(angle, x, y, z));
        rotate(rotation);
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
     * inverse of {@link #getPosition(PosVector)}
     * @param p position relative to reference frame of instantiation
     * @return coordinates relative to local-space
     */
    public PosVector mapToLocal(PosVector p){
        PosVector newPos = p.sub(posVec, new PosVector());
        float x = newPos.x();
        float y = newPos.y();
        float z = newPos.z();
        return null;
    }

    /**
     * the already stacked matrix will be passed to the new stacked matrix
     */
    @Override
    public void pushMatrix() {
        final ShadowMatrix2 head = new ShadowMatrix2();
        head.setStateTo(this);
        stack.push(head);
    }

    /**
     * the stacked matrix of this stacked matrix will become the new stacked matrix
     */
    @Override
    public void popMatrix() {
        setStateTo(stack.pop());
    }

    @Override
    public void rotate(Quaternionf rotation) {
        Matrix3f matrix = rotation.get(new Matrix3f());
        yVec.mul(matrix);
        xVec.mul(matrix);
        zVec.mul(matrix);
    }

    /**
     * print current state of axis system
     */
    public String toString() {
        return String.format("[O: %s X: %s, Y: %s, Z: %s]", posVec, xVec, yVec, zVec);
    }

    // unsupported operations

    @Override
    public void multiplyAffine(Matrix4f postTransformation) { //TODO look for possibility to implement
        throw new UnsupportedOperationException("ShadowMatrix has not yet implemented multiplyAffine");
    }

    @Override
    public void popAll() {
        // it is error-handling after all
        setStateTo(new ShadowMatrix2());
    }
}
