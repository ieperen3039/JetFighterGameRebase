package nl.NG.Jetfightergame.Assets;

import nl.NG.Jetfightergame.Sound.AudioFile;
import nl.NG.Jetfightergame.Tools.Directory;

import static nl.NG.Jetfightergame.Tools.Directory.music;
import static nl.NG.Jetfightergame.Tools.Directory.soundEffects;

/**
 * @author Geert van Ieperen
 * created on 6-2-2018.
 */
public enum Sounds {
    button(soundEffects, "toggle_button.ogg"),
    pulsePower(music, "Pulse Power.ogg"),

    explosion1(soundEffects, "explosion.ogg"),
    shieldPop(soundEffects, "fizzle.ogg"),
    booster(soundEffects, "loop_2.wav"),
    shield(soundEffects, "powerfield.ogg"),
    powerupOne(soundEffects, "powerup_1.wav"),
    powerupTwo(soundEffects, "powerup_2.wav"),

    testSound(soundEffects, "fizzle.ogg");

    private AudioFile audioFile;

    Sounds(Directory dir, String name) {
        audioFile = new AudioFile(dir, name);
    }

    public AudioFile get() {
        if (!audioFile.isLoaded()) return AudioFile.emptyFile;
        return audioFile;
    }

    public static void initAll(){
        for (Sounds sounds : values()) {
            sounds.audioFile.load();
        }
    }
}
