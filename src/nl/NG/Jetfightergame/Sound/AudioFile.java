package nl.NG.Jetfightergame.Sound;

import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Resources;
import nl.NG.Jetfightergame.Tools.Toolbox;
import org.lwjgl.openal.AL10;

import java.io.File;
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
    private final FileType type;

    /**
     * create an unloaded audio file handle
     * @param dir
     * @param filePath
     */
    public AudioFile(Directory dir, String filePath) {
        audioData = dir.getFile(filePath);

        if (filePath.endsWith(".ogg")){
            type = FileType.ogg;
        } else if (filePath.endsWith(".wav")){
            type = FileType.wave;
        } else {
            System.err.println("Filetype of '" + filePath + "' is not supported.");
            type = null;
        }
    }

    /**
     * load this file to memory
     * inverses an action of {@link #dispose()}
     */
    public void load() {
        // only load if this is not done yet
        if (dataID >= 0) return;

        this.dataID = AL10.alGenBuffers();

        boolean success = false;
        switch (type){
            case wave:
                success = Resources.loadWaveData(dataID, audioData);
                break;
            case ogg:
                success = Resources.loadOggData(dataID, audioData);
                break;
        }

        // register for cleanup, unless it failed to load
        if (success) {
            registeredSoundfiles.add(this);
        } else {
            dataID = -2;
        }

        Toolbox.checkALError();
    }

    public int getID(){
        return dataID;
    }

    /**
     * remove this soundfile from memory
     * @see #load()
     */
    public void dispose(){
        AL10.alDeleteBuffers(dataID);
        dataID = -1;

        registeredSoundfiles.remove(this);
    }

    /**
     * all sounds that have been written to the soundcard will be removed
     * @see #load()
     */
    @SuppressWarnings("ConstantConditions")
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

    private enum FileType {
        wave, ogg
    }
}
