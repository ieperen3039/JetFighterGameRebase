package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Sound.OggData;
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
            buffer = toByteBuffer(path);
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

    private static ByteBuffer toByteBuffer(Path path) throws IOException {
        ByteBuffer buffer;
        try (SeekableByteChannel fc = Files.newByteChannel(path)) {
            buffer = createByteBuffer((int) fc.size() + 1);
            while (fc.read(buffer) != -1) ;
        }
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
            System.err.println("Could not load ogg file '" + audioData + "'. Continuing without this sound");
            if (ServerSettings.DEBUG) e.printStackTrace();
            return false;
        }

        AL10.alBufferData(dataID, oggFile.format, oggFile.data, oggFile.samplerate);
        oggFile.dispose();

        Toolbox.checkALError();
        return true;
    }
}
