package nl.NG.Jetfightergame.Sound;

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
    cosmicBlack(music, "Cosmic Black.ogg"),

    powerupOne(soundEffects, "powerup_1.wav"),
    powerupTwo(soundEffects, "powerup_2.wav"),

    jet_fire(soundEffects, "qubodupFireLoop.ogg"),
    booster(soundEffects, "loop_2.wav"),
    fizzle(soundEffects, "rocket_booster.ogg"),
    shield(soundEffects, "powerfield.ogg"),
    windOff(soundEffects, "qubodupFireLoop.ogg"),

    explosionMC(soundEffects, "explosion.ogg"),
    explosion2(soundEffects, "explosion_2.ogg"),
    seekerPop(soundEffects, "seeker_pop.ogg"),
    shieldPop(soundEffects, "fizzle.ogg"),
    deathWarning(soundEffects, "explosion.ogg"),

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
