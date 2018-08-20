package nl.NG.Jetfightergame.Rendering.Particles;

import nl.NG.Jetfightergame.Engine.GameTimer;
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
    private final Color4f baseColor1;
    private final Color4f baseColor2;

    private TrackedVector<PosVector> aSide;
    private TrackedVector<PosVector> bSide;
    private TrackedVector<DirVector> direction;
    private float timeToLive;
    private Color4f color1;
    private Color4f color2;
    private float particleSize;

    private float nextParticleMoment;
    private float colorChangeFallback;
    private final GameTimer gameTimer;
    private boolean colorIsChanged = false;

    public BoosterLine(
            PosVector A, PosVector B, DirVector direction,
            float particlesPerSecond, float maxTimeToLive, Color4f color1, Color4f color2, float particleSize, GameTimer gameTimer
    ) {
        aRelative = A;
        bRelative = B;
        this.aSide = new TrackedVector<>(A);
        this.bSide = new TrackedVector<>(B);
        this.direction = new TrackedVector<>(direction);
        this.timeToLive = maxTimeToLive;
        this.baseColor1 = color1;
        this.baseColor2 = color2;
        this.color1 = color1;
        this.color2 = color2;
        this.particleSize = particleSize;
        this.nextParticleMoment = gameTimer.time() + (1 / particlesPerSecond) * Toolbox.random.nextFloat();
        this.gameTimer = gameTimer;
    }

    /**
     * @param dirNew    the new direction in which the particles move
     * @param jitter the variation of the direction for this time period
     * @param particlesPerSecond the particles per second after the next particle has spawned
     * @return the particles resulting from the update
     */
    public ParticleCloud update(MatrixStack ms, DirVector dirNew, float jitter, float particlesPerSecond) {
        aSide.update(ms.getPosition(aRelative));
        bSide.update(ms.getPosition(bRelative));
        direction.update(dirNew);

        float currentTime = gameTimer.getRenderTime().current();

        if (colorIsChanged && colorChangeFallback < currentTime) {
            setColor(baseColor1, baseColor2, 0);
            colorIsChanged = false;
        }

        float deltaTime = gameTimer.getRenderTime().difference();
        if (nextParticleMoment >= currentTime) return null;
        float cooldown = 1f / particlesPerSecond;

        ParticleCloud cloud = new ParticleCloud();
        do {
            final float timeFraction = deltaTime / (nextParticleMoment - currentTime);
            PosVector aPos = aSide.previous().interpolateTo(aSide.current(), timeFraction);
            PosVector bPos = bSide.previous().interpolateTo(bSide.current(), timeFraction);
            PosVector pos = aPos.interpolateTo(bPos, Toolbox.random.nextFloat());
            DirVector dir = direction.current().interpolateTo(direction.previous(), timeFraction);

            Color4f color = color1.interpolateTo(color2, Toolbox.random.nextFloat());
            cloud.addParticle(pos, dir, jitter, timeToLive, color, particleSize);
            nextParticleMoment += cooldown;
        } while (nextParticleMoment < currentTime);

        return cloud;
    }

    public void setColor(Color4f color1, Color4f color2, float duration) {
        this.color1 = color1;
        this.color2 = color2;
        colorChangeFallback = Math.max(colorChangeFallback, gameTimer.time() + duration);
        colorIsChanged = true;
    }
}
