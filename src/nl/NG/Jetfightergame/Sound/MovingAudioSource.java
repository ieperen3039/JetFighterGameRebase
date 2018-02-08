package nl.NG.Jetfightergame.Sound;

import nl.NG.Jetfightergame.Rendering.Interpolation.VectorInterpolator;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.lwjgl.openal.AL10;

/**
 * @author Geert van Ieperen
 * created on 7-2-2018.
 */
public class MovingAudioSource extends AudioSource {

    private VectorInterpolator movement;
    private Vector lastPosition;
    private float lastTime;

    public MovingAudioSource(AudioFile data, PosVector sourcePos, float pitch, float gain) {
        super(data, sourcePos, pitch, gain);

        movement = new VectorInterpolator(10, sourcePos);
        lastPosition = sourcePos;
    }

    /**
     * adds a position point to the interpolation queue
     */
    public void setPosition(PosVector position, float currentTime){
        movement.add(position, currentTime);
    }

    /**
     * updates the position of the source by interpolating position and velocity
     * @param currentTime
     */
    public void update(float currentTime){
        final Vector position = movement.getInterpolated(currentTime);

        // TODO store velocity
        final DirVector velocity = new DirVector();
        lastPosition.to(position, velocity);
        velocity.scale(currentTime - lastTime, velocity);
        lastPosition = position;
        lastTime = currentTime;

        alSourceVec(sourceID, AL10.AL_POSITION, position);
        alSourceVec(sourceID, AL10.AL_VELOCITY, velocity);
    }
}
