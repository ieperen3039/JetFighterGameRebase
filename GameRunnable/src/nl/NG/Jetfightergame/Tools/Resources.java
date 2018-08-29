package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Sound.OggData;
import nl.NG.Jetfightergame.Sound.WaveData;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

/**
 * @author Jorren
 */
public final class Resources {

    public static String loadText(Path path) throws IOException {
        String result;
        try (
                InputStream in = new FileInputStream(path.toFile());
                Scanner scanner = new Scanner(in, "UTF-8")
        ) {
            result = scanner.useDelimiter("\\A").next();
        } catch (FileNotFoundException e) {
            throw new IOException("Resource not found: " + path);
        }
        return result;
    }

    public static ByteBuffer toByteBuffer(Path path, File resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);
                while (fc.read(buffer) != -1) ;
            }
        } else {
            try (InputStream source = new FileInputStream(resource);
                 ReadableByteChannel rbc = Channels.newChannel(source)) {
                buffer = BufferUtils.createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                    }
                }
            }
        }

        buffer.flip();
        return buffer;
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    /**
     * load the data as if it is a .wav file
     *
     * @param dataID
     * @param audioData
     * @return true iff it was loaded properly
     */
    public static boolean loadWaveData(int dataID, File audioData) {
        // load soundfile to audiostream
        WaveData waveFile;
        try {
            waveFile = WaveData.create(audioData);
            Toolbox.checkALError();

        } catch (IOException | UnsupportedAudioFileException e) {
            System.err.println("Could not load wave file '" + audioData + "'. Continuing without this sound");
            if (ServerSettings.DEBUG) e.printStackTrace();
            return false;
        }

        // load audio into soundcard
        AL10.alBufferData(dataID, waveFile.format, waveFile.data, waveFile.samplerate);
        waveFile.dispose();

        Toolbox.checkALError();
        return true;
    }

    public static boolean loadOggData(int dataID, File audioData) {
        Toolbox.checkALError();

        OggData oggFile;
        try {
            oggFile = OggData.create(audioData.getPath());
            Toolbox.checkALError();

        } catch (IOException e) {
            Logger.WARN.print("Could not load ogg file '" + audioData + "'. Continuing without this sound");
            if (ServerSettings.DEBUG) e.printStackTrace();
            return false;
        }

        AL10.alBufferData(dataID, oggFile.format, oggFile.data, oggFile.samplerate);

        Toolbox.checkALError();
        return true;
    }
}
