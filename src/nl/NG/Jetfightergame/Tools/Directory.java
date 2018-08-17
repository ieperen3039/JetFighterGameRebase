package nl.NG.Jetfightergame.Tools;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Geert van Ieperen
 * created on 10-2-2018.
 */
public enum Directory {
    music(Paths.get("res", "music")),
    soundEffects(Paths.get("res", "sounds")),
    fonts(Paths.get("res", "fonts")),
    shaders(Paths.get("res", "shaders")),
    meshes(Paths.get("res", "models")),
    pictures(Paths.get("res", "pictures")),
    recordings(Paths.get("ScreenShots"));

    private final Path directory;

    Directory(Path directory) {
        this.directory = directory;
    }

    public File getFile(String... path) {
        return currentDirectory()
                .resolve(getPath(path))
                .toFile();
    }

    public Path getPath(String... path) {
        Path dir = this.directory;
        for (String p : path) {
            dir = dir.resolve(p);
        }
        return dir;
    }

    public static Path currentDirectory() {
        return Paths.get("").toAbsolutePath();
    }
}
