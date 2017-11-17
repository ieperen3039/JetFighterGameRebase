package nl.NG.Jetfightergame.Engine.GLMatrix;

import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.GameObjects.Structures.Shape;
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
    }

    @Override
    public void draw(Shape object) {
        currentShader.setUniform("projectionMatrix", projectionMatrix);
        currentShader.setUniform("modelViewMatrix", modelViewMatrix);
        object.render(new Painter());
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
        currentShader.setUniform4f("Material.ambient", material.diffuse);
        currentShader.setUniform4f("Material.diffuse", material.diffuse);
        currentShader.setUniform4f("Material.specular", material.specular);
        currentShader.setUniform("Material.reflectance", material.shininess);
    }

    @Override
    public void rotate(double angle, float x, float y, float z) {
        currentMatrix.getRotation(new AxisAngle4d(angle, x, y, z));
    }

    @Override
    public void translate(float x, float y, float z) {
        currentMatrix.translate(x, y, z);
    }

    @Override
    public void scale(float x, float y, float z) {
        currentMatrix.scale(x, y, z);
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
