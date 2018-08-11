package nl.NG.Jetfightergame.Rendering.Particles;

import nl.NG.Jetfightergame.AbstractEntities.EntityState;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Geert van Ieperen. Created on 25-7-2018.
 */
public class ParticleTrail {
    private static final Quaternionf NO_ROT = new Quaternionf();
    private static final float MAX_TTL = 3f;
    private static final float MIN_TTL = 1f;
    private static final float PARTICLE_SIZE = 1f;

    private EntityState trail;
    private int nOfParticles;
    private Color4f color;

    public ParticleTrail(PosVector start, PosVector end, int nOfParticles, DirVector drift, Color4f color) {
        this.color = color;
        this.trail = new EntityState(start, end, NO_ROT, NO_ROT, drift, drift);
        this.nOfParticles = nOfParticles;
    }

    public ParticleTrail(PosVector start, PosVector end, float particlesPerUnit, DirVector drift, Color4f color) {
        this(start, end, Toolbox.randomToInt(start.distance(end) / particlesPerUnit), drift, color);
    }

    public ParticleTrail(EntityState trail, int nOfParticles, Color4f color) {
        this.trail = trail;
        this.nOfParticles = nOfParticles;
        this.color = color;
    }

    public ParticleCloud toCloud() {
        ParticleCloud cloud = new ParticleCloud();

        for (int i = 0; i < nOfParticles; i++) {
            float rand = Toolbox.random.nextFloat();
            cloud.addParticle(
                    trail.position(rand), trail.velocity(rand),
                    0, Toolbox.randomBetween(MIN_TTL, MAX_TTL),
                    color, PARTICLE_SIZE
            );
        }

        return cloud;
    }

    public void writeToStream(DataOutput out) throws IOException {
        trail.writeToStream(out);
        out.writeInt(nOfParticles);
        DataIO.writeColor(out, color);
    }

    public static ParticleTrail readFromStream(DataInput in) throws IOException {
        return new ParticleTrail(
                EntityState.readFromStream(in),
                in.readInt(),
                DataIO.readColor(in)
        );
    }
}
