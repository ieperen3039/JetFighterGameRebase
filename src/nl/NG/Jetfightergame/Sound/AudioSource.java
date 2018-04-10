package nl.NG.Jetfightergame.Sound;

import nl.NG.Jetfightergame.Settings.Settings;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.lwjgl.openal.AL10;

import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;

/**
 * @author Geert van Ieperen
 * created on 5-2-2018.
 */
public class AudioSource {

    protected int sourceID;

    public AudioSource(AudioFile data, PosVector sourcePos, float pitch, float gain) {

        int dataID = data.getID();
        if (dataID == -1) throw new java.lang.NullPointerException("audio file " + data + " has not been loaded");
        if (dataID == -2) return; // data could not be loaded, and this has already been reported

        sourceID = AL10.alGenSources();
        Toolbox.checkALError();

        alSourcei(sourceID, AL10.AL_BUFFER, dataID);
        alSourcef(sourceID, AL10.AL_PITCH, pitch);
        alSourceVec(sourceID, AL10.AL_POSITION, sourcePos);
        alSourceVec(sourceID, AL10.AL_VELOCITY, DirVector.zeroVector());

        setGain(gain);

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
        AL10.alDeleteSources(sourceID);
    }

    protected static void alSourceVec(int source, int mode, Vector p) {
        AL10.alSource3f(source, mode, p.x(), p.y(), p.z());
    }

    protected void setGain(float newValue){
        alSourcef(sourceID, AL10.AL_GAIN, newValue * Settings.SOUND_MASTER_GAIN);
    }
}
