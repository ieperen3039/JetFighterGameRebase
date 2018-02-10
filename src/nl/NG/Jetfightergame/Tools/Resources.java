package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Settings;
import nl.NG.Jetfightergame.Sound.WaveData;
import org.lwjgl.openal.AL10;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.lwjgl.BufferUtils.createByteBuffer;

/**
 * @author Jorren
 */
public final class Resources {

    public static String loadText(String fileName) throws IOException {
        String result;
        try (
                InputStream in = new FileInputStream(fileName);
                Scanner scanner = new Scanner(in, "UTF-8")
        ) {
            result = scanner.useDelimiter("\\A").next();
        } catch(FileNotFoundException e) {
            throw new IOException("Resource not found: " + fileName);
        }
        return result;
    }

    public static List<String> readAllLines(String fileName) throws IOException {
        List<String> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(line);
            }
        }
        return list;
    }

    public static ByteBuffer toByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = Paths.get(resource);
        if (Files.isReadable(path)) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = createByteBuffer((int) fc.size() + 1);
                while (fc.read(buffer) != -1);
            }
        } else {
            try (InputStream source = new FileInputStream(resource);
                 ReadableByteChannel rbc = Channels.newChannel(source)) {
                buffer = createByteBuffer(bufferSize);

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
        ByteBuffer newBuffer = createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    public static BufferedInputStream getInputStream(String filename) throws FileNotFoundException {
        final InputStream fileInputStream = new FileInputStream(filename);
        return new BufferedInputStream(fileInputStream);
    }

    /**
     * load the data as if it is a .wav file
     * @return true iff it was loaded properly
     * @param dataID
     * @param audioData
     */
    public static boolean loadWaveData(int dataID, File audioData) {
        // load soundfile to audiostream
        WaveData waveFile;
        try {
            waveFile = WaveData.create(audioData);

        } catch (IOException | UnsupportedAudioFileException e) {
            System.err.println("Could not load sound file '" + audioData + "'. Continuing without this sound");
            if (Settings.DEBUG) e.printStackTrace();
            return false;
        }

        // load audio into soundcard
        AL10.alBufferData(dataID, waveFile.format, waveFile.data, waveFile.samplerate);
        waveFile.dispose();
        Toolbox.checkALError();
        return true;
    }

    /**
     * signals that one of the resources/files could not be loaded
     */
    public class ResourceException extends IOException{
        public ResourceException(String message) {
            super(message);
        }

        public ResourceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
