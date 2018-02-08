package nl.NG.Jetfightergame.Sound;

import nl.NG.Jetfightergame.Engine.GLException;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static nl.NG.Jetfightergame.Tools.Toolbox.checkALError;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.openal.EXTEfx.ALC_MAX_AUXILIARY_SENDS;

/**
 * @author Geert van Ieperen
 * created on 5-2-2018.
 */
public class SoundEngine {

    // default device
    private static final long device = ALC10.alcOpenDevice((ByteBuffer) null);

    /**
     * set up openAL environment
     */
    public SoundEngine() {
        // Create a handle for the device capabilities
        ALCCapabilities deviceCaps = ALC.createCapabilities(device);

        IntBuffer contextAttribList = BufferUtils.createIntBuffer(16);
        // default attributes
        int[] attributes = new int[]{ALC_REFRESH, 60, ALC_SYNC, ALC_FALSE, ALC_MAX_AUXILIARY_SENDS, 2, 0};
        contextAttribList.put(attributes);

        // create the context with the provided attributes
        long newContext = ALC10.alcCreateContext(device, contextAttribList);

        if(!ALC10.alcMakeContextCurrent(newContext)) {
            throw new GLException("Failed to make OpenAL context current");
        }

        AL.createCapabilities(deviceCaps);
        checkALError();

        setListenerPosition(PosVector.zeroVector(), DirVector.zeroVector());
        setListenerOrientation(DirVector.xVector(), DirVector.yVector());
    }

    /**
     * set the speed of sound to the specified value
     * @param value speed in m/s
     */
    public void speedOfSound(float value){
        AL11.alSpeedOfSound(value);
    }

    /**
     * set the position and velocity of the listener
     * @param pos position
     * @param vel velocity, does not influence position
     */
    private void setListenerPosition(PosVector pos, DirVector vel) {
        AL10.alListener3f(AL10.AL_POSITION, pos.x(), pos.y(), pos.z());
        AL10.alListener3f(AL10.AL_VELOCITY, vel.x(), vel.y(), vel.z());
    }

    // TODO requires more research
    private void setListenerOrientation(DirVector forward, DirVector up) {
        float[] asArray = {forward.x(), forward.y(), forward.z(), up.x(), up.y(), up.z()};
        FloatBuffer orientation = FloatBuffer.wrap(asArray);

        AL10.alListenerfv(AL10.AL_ORIENTATION, orientation);
    }

    public static void closeDevices() {
        ALC10.alcCloseDevice(device);
        checkALError();
    }

    public static void main(String[] args) throws InterruptedException {
        new SoundEngine();
        checkALError();
        AudioFile expl = Sounds.explosion;
        AudioSource src = new AudioSource(expl, PosVector.zeroVector(), 1f, 1f);
        Thread.sleep(3000);

        src.dispose();
        closeDevices();
    }
}
