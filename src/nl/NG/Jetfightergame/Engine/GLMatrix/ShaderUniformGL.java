package nl.NG.Jetfightergame.Engine.GLMatrix;

import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Shaders.ShaderProgram;
import nl.NG.Jetfightergame.Shaders.shader.PointLight;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.AxisAngle4d;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Stack;

/**
 * @author Geert van Ieperen
 *         created on 16-11-2017.
 */
public class ShaderUniformGL implements GL2 {
    // FOV in radians
    public static final float FOV = (float) Math.toRadians(60.0f);
    // z-coordinates relative to the activeCamera.
    public static final float Z_NEAR = 0.05f;
    public static final float Z_FAR = 1000.0f;

    private Stack<Matrix4f> matrixStack;

    private Matrix4f currentMatrix;
    private Matrix4f projectionMatrix = new Matrix4f();
    private Matrix4f modelViewMatrix = new Matrix4f();

    private ShaderProgram currentShader;

    public ShaderUniformGL(ShaderProgram shader) {
        currentShader = shader;
        matrixStack = new Stack<>();
        currentShader.setUniform("projectionMatrix", projectionMatrix);
        currentShader.setUniform("modelViewMatrix", modelViewMatrix);
    }

    /** TODO integrate with matrix
     * rotates the axis frame such that the z-axis points from source to target vector,
     * and translates the system to source
     * if (target == source) the axis will not turn
     * @param source the vector where the axis will have its orgin upon returning
     * @param target the vector in which direction the z-axis will point upon returning
     */
    public void pointFromTo(PosVector source, PosVector target) {
        if (target.equals(source)) return;
        DirVector parallelVector = source.to(target)
                .normalized();

        DirVector M = DirVector.Z.cross(parallelVector);
        double angle = Math.acos(DirVector.Z.dot(parallelVector));// in Radians

        translate(source);
        rotate(M, angle);
    }

    @Override
    public void matrixMode(int matrix) {
        if (matrix == GL_PROJECTION){
            currentMatrix = projectionMatrix;
        } else if (matrix == GL_MODELVIEW){
            currentMatrix = modelViewMatrix;
        } else {
            System.err.println("Received invalid enum of " + Toolbox.getCallingMethod(1) + " (" + matrix + ")");
        }
    }

    @Override
    public void setColor(double red, double green, double blue) {

    }

    public Matrix4f updateProjectionMatrix(int width, int height) {
        float aspectRatio = (float) width / (float) height;
        return projectionMatrix.setPerspective(Settings.FOV, aspectRatio, Z_NEAR, Z_FAR);
    }

    @Override
    public void setLight(int lightNumber, DirVector dir, Color lightColor){
        this.setLight(lightNumber, dir.toPosVector(), lightColor);
    }

    @Override
    public void setLight(int lightNumber, PosVector pos, Color lightColor){
        PointLight lamp = new PointLight(Toolbox.colorVector(lightColor), pos.toVector3f(), 1f);
        currentShader.setPointLight(lamp, lightNumber);
    }

    // TODO add transparency
    @Override
    public void setMaterial(Material material){
        material.setAsMaterial(currentShader);
    }

    @Override
    public void rotate(double angle, double x, double y, double z) {
        currentMatrix.getRotation(new AxisAngle4d(angle, x, y, z));
    }

    @Override
    public void translate(double x, double y, double z) {
        currentMatrix.translate((float) x, (float) y, (float) z);
    }

    @Override
    public void scale(double x, double y, double z) {
        currentMatrix.scale((float) x, (float) y, (float) z);
    }

    @Override
    public PosVector getPosition(PosVector p) {
        Vector3f result = p.toVector3f();
        result.mulPosition(currentMatrix);
        return new PosVector(result);
    }

    @Override
    public DirVector getDirection(DirVector v) {
        Vector3f result = v.toVector3f();
        result.mulDirection(currentMatrix);
        return new DirVector(result);
    }

    @Override
    public void pushMatrix() {
        matrixStack.push(new Matrix4f(currentMatrix));
    }

    @Override
    public void popMatrix() {
        currentMatrix = matrixStack.pop();
    }

    @Override
    public void clearColor() {
        setColor(0, 0, 0);
    }
}
