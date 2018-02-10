package nl.NG.Jetfightergame.Sound;

import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Resources;
import nl.NG.Jetfightergame.Tools.Toolbox;
import org.lwjgl.openal.AL10;

import java.io.File;
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
    private final File audioData;

    public AudioFile(Directory dir, String filePath) {
        audioData = dir.getFile(filePath);
        load(); // TODO maybe don't load all soundfiles right away?
    }

    /**
     * load this file to memory
     * inverses an action of {@link #dispose()}
     */
    public void load() {
        Toolbox.checkALError();

        // only load if this is not done yet
        if (dataID >= 0) return;

        this.dataID = AL10.alGenBuffers();
        Toolbox.checkALError();

        // register for cleanup, unless it failed to load
        if (Resources.loadWaveData(dataID, audioData)) {
            registeredSoundfiles.add(this);
        } else {
            dataID = -2;
        }
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
        return audioData.getPath();
    }
}
