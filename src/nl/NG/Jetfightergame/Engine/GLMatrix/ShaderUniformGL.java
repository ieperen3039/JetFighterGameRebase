package nl.NG.Jetfightergame.Engine.GLMatrix;

import nl.NG.Jetfightergame.Camera.Camera;
import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Shaders.Material;
import nl.NG.Jetfightergame.Shaders.ShaderProgram;
import nl.NG.Jetfightergame.Shaders.shader.PointLight;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Stack;

/**
 * @author Geert van Ieperen
 *         created on 16-11-2017.
 */
public class ShaderUniformGL implements GL2 {

    private Stack<Matrix4f> matrixStack;

    private Matrix4f modelViewMatrix = new Matrix4f();
    private Matrix4f projectionMatrix = new Matrix4f();

    private ShaderProgram currentShader;

    public ShaderUniformGL(ShaderProgram shader) {
        currentShader = shader;
        matrixStack = new Stack<>();
    }

    @Override
    public void draw(Renderable object) {
        currentShader.setUniform("projectionMatrix", projectionMatrix);
        currentShader.setUniform("modelViewMatrix", modelViewMatrix);
        object.render(new Painter());
    }

    @Override
    public void setFustrum(int width, int height) {
        float aspectRatio = (float) width / (float) height;
        projectionMatrix.setPerspective(Settings.FOV, aspectRatio, Settings.Z_NEAR, Settings.Z_FAR);
    }

    @Override
    public void setLight(int lightNumber, DirVector dir, Color lightColor){
        this.setLight(lightNumber, dir.toPosVector(), lightColor);
    }

    @Override
    public void setLight(int lightNumber, PosVector pos, Color lightColor){
        PointLight lamp = new PointLight(Toolbox.colorVector(lightColor), pos.toVector3f(), 1f);
        setLight(lamp, lightNumber);
    }

    @Override
    public void setMaterial(Material material){
        currentShader.setUniform4f("material.ambient", material.diffuse);
        currentShader.setUniform4f("material.diffuse", material.diffuse);
        currentShader.setUniform4f("material.specular", material.specular);
        currentShader.setUniform("material.reflectance", material.shininess);
    }

    @Override
    public void clearColor() {

    }

    @Override
    public void rotate(double angle, float x, float y, float z) {
        modelViewMatrix.rotate(new AxisAngle4f((float) angle, x, y, z));
    }

    @Override
    public void translate(float x, float y, float z) {
        modelViewMatrix.translate(x, y, z);
    }

    @Override
    public void scale(float x, float y, float z) {
        modelViewMatrix.scale(x, y, z);
    }

    @Override
    public PosVector getPosition(PosVector p) {
        Vector3f result = p.toVector3f();
        result.mulPosition(modelViewMatrix);
        return new PosVector(result);
    }

    @Override
    public DirVector getDirection(DirVector v) {
        Vector3f result = v.toVector3f();
        result.mulDirection(modelViewMatrix);
        return new DirVector(result);
    }

    @Override
    public void pushMatrix() {
        matrixStack.push(new Matrix4f(modelViewMatrix));
    }

    @Override
    public void popMatrix() {
        modelViewMatrix = matrixStack.pop();
    }

    @Override
    public void multiplyAffine(Matrix4f preTransformation) {
        // first apply combinedTransformation, then the viewTransformation
        preTransformation.mul(modelViewMatrix, modelViewMatrix);
    }

    @Override
    public void setCamera(Camera activeCamera) {
        projectionMatrix.lookAt(
                activeCamera.getEye().toVector3f(),
                activeCamera.getFocus().toVector3f(),
                activeCamera.getUpVector().toVector3f()
        );
    }

    @Override
    public void setLight(PointLight lamp, int lightNumber) {
        currentShader.setPointLight(lamp, lightNumber);
    }
}
