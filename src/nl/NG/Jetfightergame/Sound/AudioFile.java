package nl.NG.Jetfightergame.Sound;

import nl.NG.Jetfightergame.Tools.Toolbox;
import org.lwjgl.openal.AL10;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayDeque;
import java.util.Queue;

/**
 * @author Geert van Ieperen
 * created on 6-2-2018.
 */
public class AudioFile {
    private static Queue<AudioFile> registeredSoundfiles = new ArrayDeque<>();

    private int dataID = -1;
    private final String filePath;

    public AudioFile(String filePath) {
        this.filePath = filePath;
        load(); // TODO maybe don't load all soundfiles right away?
    }

    /**
     * load this file to memory
     * inverses an action of {@link #dispose()}
     */
    public void load() {
        // only load if this is not done yet
        if (dataID != -1) return;

        dataID = AudioManager.getBuffer();
        Toolbox.checkALError();

        // load soundfile to audiostream
        WaveData waveFile;
        try {
            waveFile = WaveData.create(filePath);

        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
            System.err.println("Could not load sound file '" + filePath + "'. Continuing without this sound");
            return;
        }

        // load audio into soundcard
        AL10.alBufferData(dataID, waveFile.format, waveFile.data, waveFile.samplerate);
        waveFile.dispose();
        Toolbox.checkALError();

        // register to allow deleting
        registeredSoundfiles.add(this);
    }

    public int getID(){
        return dataID;
    }

    /**
     * remove this soundfile from memory
     * @see #load()
     */
    public void dispose(){
        AL10.alDeleteBuffers(IntBuffer.wrap(new int[]{dataID}));
        dataID = -1;
        registeredSoundfiles.remove(this);
    }

    /**
     * all sounds that have been written to the soundcard will be removed
     * @see #load()
     */
    public static void cleanAll() {
        while (!registeredSoundfiles.isEmpty()){
            registeredSoundfiles.peek().dispose();
            Toolbox.checkALError();
        }
    }

    @Override
    public String toString() {
        return filePath;
    }
}
