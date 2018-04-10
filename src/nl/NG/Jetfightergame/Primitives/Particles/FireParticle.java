package nl.NG.Jetfightergame.Primitives.Particles;

import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Settings.Settings;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Vector3f;

/**
 * @author Geert van Ieperen
 * created on 2-2-2018.
 */
public class FireParticle implements Particle {

    private Color4f color;
    private float currentRotation;
    private Vector3f angVec;

    /** the three points of this relative to position */
    private final float dx, dy, dz;

    private float x, y, z;
    private float rotationSpeed;
    private float timeToLive;

    public static FireParticle randomParticle(PosVector position, float power, float maxTTL){
        final Color4f fire = new Color4f(1, Settings.random.nextFloat(), 0);
        final float randFloat = Settings.random.nextFloat();
        final DirVector random = DirVector.randomOrb();
        final float rotationSpeed = 2 + (2 / randFloat);

        random.mul(power * randFloat);
        return new FireParticle(position, random, fire, DirVector.random(), rotationSpeed, randFloat * randFloat * randFloat * maxTTL);
    }

    public FireParticle(PosVector position, DirVector movement, Color4f color, Vector3f angVec, float rotationSpeed, float timeToLive) {
        this.color = color;
        this.angVec = angVec;
        this.rotationSpeed = rotationSpeed;
        this.timeToLive = timeToLive;
        x = position.x();
        y = position.y();
        z = position.z();
        dx = movement.x();
        dy = movement.y();
        dz = movement.z();
    }

    public void draw(GL2 gl) {
        gl.setMaterial(Material.GLOWING, color);
        gl.pushMatrix();
        {
            gl.translate(x, y, z);
            gl.rotate(currentRotation, angVec.x(), angVec.y(), angVec.z());
            gl.scale(Settings.FIRE_PARTICLE_SIZE);
            gl.draw(GeneralShapes.TRIANGLE);
        }
        gl.popMatrix();
    }

    @Override
    public void updateRender(float deltaTime) {
        currentRotation += rotationSpeed * deltaTime;
        x += dx * deltaTime;
        y += dy * deltaTime;
        z += dz * deltaTime;
        timeToLive -= deltaTime;
    }

    @Override
    public boolean isOverdue() {
        return timeToLive < 0;
    }
}
