package nl.NG.Jetfightergame.Sound;

import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;
import org.lwjgl.openal.AL10;

import java.nio.IntBuffer;

import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;

/**
 * @author Geert van Ieperen
 * created on 5-2-2018.
 */
public class AudioSource {

    protected int sourceID;

    public AudioSource(AudioFile data, PosVector sourcePos, float pitch, float gain) {
        sourceID = AudioManager.getBuffer();
        Toolbox.checkALError();

        int dataID = data.getID();
        if (dataID == -1) throw new java.lang.NullPointerException("soundfile " + data + " has not been loaded");
        alSourcei(sourceID, AL10.AL_BUFFER, dataID);
        alSourcef(sourceID, AL10.AL_PITCH, pitch);
        alSourcef(sourceID, AL10.AL_GAIN, gain);
        alSourceVec(sourceID, AL10.AL_POSITION, sourcePos);
        alSourceVec(sourceID, AL10.AL_VELOCITY, DirVector.zeroVector());

        Toolbox.checkALError();

        AL10.alSourcePlay(sourceID);
    }

    public void interrupt(){
        AL10.alSourceStop(sourceID);
    }

    /**
     * remove this source from the soundcard
     */
    public void dispose(){
        AL10.alDeleteSources(IntBuffer.wrap(new int[]{sourceID}));
    }

    protected static void alSourceVec(int source, int mode, Vector p) {
        AL10.alSource3f(source, mode, p.x(), p.y(), p.z());
    }
}
