package nl.NG.Jetfightergame.Rendering.Particles;

import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Tracked.TrackedVector;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen. Created on 11-8-2018.
 */
public class BoosterLine {
    private final PosVector aRelative;
    private final PosVector bRelative;

    private TrackedVector<PosVector> aSide;
    private TrackedVector<PosVector> bSide;
    private TrackedVector<DirVector> direction;
    private float cooldown;
    private float timeToLive;
    private Color4f color1;
    private Color4f color2;
    private float particleSize;

    private float timeRemaining;

    public BoosterLine(
            PosVector A, PosVector B, DirVector direction,
            float particlesPerSecond, float maxTimeToLive, Color4f color1, Color4f color2, float particleSize
    ) {
        aRelative = A;
        bRelative = B;
        this.aSide = new TrackedVector<>(A);
        this.bSide = new TrackedVector<>(B);
        this.direction = new TrackedVector<>(direction);
        this.cooldown = 1f / particlesPerSecond;
        this.timeToLive = maxTimeToLive;
        this.color1 = color1;
        this.color2 = color2;
        this.particleSize = particleSize;

        this.timeRemaining = cooldown * Toolbox.random.nextFloat();
    }

    /**
     * @param dirNew    the new direction in which the particles move
     * @param deltaTime
     * @return the particles resulting from the update
     */
    public ParticleCloud update(MatrixStack ms, DirVector dirNew, float deltaTime) {
        aSide.update(ms.getPosition(aRelative));
        bSide.update(ms.getPosition(bRelative));
        direction.update(dirNew);

        timeRemaining -= deltaTime;
        if (timeRemaining >= 0) return null;

        ParticleCloud cloud = new ParticleCloud();
        do {
            final float timeFraction = timeRemaining / deltaTime;
            PosVector aPos = aSide.previous().interpolateTo(aSide.current(), timeFraction);
            PosVector bPos = bSide.previous().interpolateTo(bSide.current(), timeFraction);
            PosVector pos = aPos.interpolateTo(bPos, Toolbox.random.nextFloat());
            DirVector dir = direction.current().interpolateTo(direction.previous(), timeFraction);

            Color4f color = color1.interpolateTo(color2, Toolbox.random.nextFloat());
            cloud.addParticle(pos, dir, 0, timeToLive, color, particleSize);
            timeRemaining += cooldown;
        } while (timeRemaining < 0);

        return cloud;
    }

    public void setColor(Color4f color1, Color4f color2) {
        this.color1 = color1;
        this.color2 = color2;
    }
}
