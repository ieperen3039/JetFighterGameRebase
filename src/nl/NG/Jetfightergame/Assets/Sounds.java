package nl.NG.Jetfightergame.Assets;

import nl.NG.Jetfightergame.Sound.AudioFile;

import static nl.NG.Jetfightergame.Tools.Directory.music;
import static nl.NG.Jetfightergame.Tools.Directory.soundEffects;

/**
 * @author Geert van Ieperen
 * created on 6-2-2018.
 */
public final class Sounds {
    public static final AudioFile explosion = new AudioFile(soundEffects, "explosion.ogg");
    public static final AudioFile pulsePower = new AudioFile(music, "Pulse Power.ogg");

    /** @see AudioFile#cleanAll() */
    public static void initAll(){}
}
