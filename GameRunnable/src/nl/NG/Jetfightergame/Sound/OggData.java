package nl.NG.Jetfightergame.Sound;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbisInfo;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.stb.STBVorbis.*;

/**
 * @author Geert van Ieperen
 * created on 12-2-2018.
 */
public class OggData {

    public final ByteBuffer data;
    public final int format;
    public final int samplerate;

    private OggData(ByteBuffer data, int format, int sampleRate) {
        this.data = data;
        this.format = format;
        this.samplerate = sampleRate;
    }

    /**
     * This method reads in the OGG data, and loads it into OpenAL
     * ALBuffer class.
     * Copyright (c) 2014-2017 Sri Harsha Chilakapati
     */
    public static OggData create(String input) throws IOException {

        // Open the vorbis file to get stb_vorbis*
        IntBuffer error = BufferUtils.createIntBuffer(1);
        long handle = stb_vorbis_open_filename(input, error, null);

        if (handle == 0) throw new IOException("Vorbis Error " + error.get(0));

        // Get the information about the OGG header
        STBVorbisInfo info = STBVorbisInfo.malloc();
        stb_vorbis_get_info(handle, info);

        int channels = info.channels();
        int sampleRate = info.sample_rate();

        int format = (channels == 1) ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;

        // Read all the samples once for all
        int numSamples = stb_vorbis_stream_length_in_samples(handle);
        ByteBuffer pcm = BufferUtils.createByteBuffer(numSamples * Short.BYTES);
        stb_vorbis_get_samples_short_interleaved(handle, channels, pcm.asShortBuffer());

        final OggData oggData = new OggData(pcm, format, sampleRate);

        // Close the stb_vorbis* handle
        stb_vorbis_close(handle);
        info.free();
        pcm.clear();

        return oggData;
    }

    public void dispose() {
        data.clear();
    }
}
