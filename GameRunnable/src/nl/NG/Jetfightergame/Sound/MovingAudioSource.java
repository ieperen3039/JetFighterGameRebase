package nl.NG.Jetfightergame.Sound;

import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.TemporalEntity;
import org.lwjgl.openal.AL10;

/**
 * @author Geert van Ieperen
 * created on 7-2-2018.
 */
public class MovingAudioSource extends AudioSource {

    private final MovingEntity source;

    public MovingAudioSource(Sounds data, MovingEntity source, float pitch, float gain, boolean repeat) {
        super(data, source.getPosition(), source.getVelocity(), pitch, gain, repeat);
        this.source = source;
    }

    public MovingAudioSource(Sounds data, MovingEntity source, float gain) {
        this(data, source, 1.0f, gain, false);
    }

    @Override
    public void update() {
        set(AL10.AL_POSITION, source.getPosition());
        set(AL10.AL_VELOCITY, source.getVelocity());

        if (TemporalEntity.isOverdue(source)) {
            dispose();
        } else {
            super.update();
        }
    }
}
