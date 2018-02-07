package nl.NG.Jetfightergame.Sound;

/**
 * @author Geert van Ieperen
 * created on 6-2-2018.
 */
public final class Sounds {
    public static final AudioFile explosion = new AudioFile("res/sounds/explosion.ogg");

    /** @see AudioFile#cleanAll() */
    public static void initAll(){
        explosion.load();
    }
}
