package nl.NG.Tools;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Geert van Ieperen created on 10-2-2018.
 */
public enum Directory {
    music(Paths.get("res", "music")),
    fonts(Paths.get("res", "fonts")),
    backdrops(Paths.get("res", "pictures")),
    recordings(Paths.get("Recordings")),
    screenShots(Paths.get("ScreenShots")),
    settings(Paths.get("")),
    gameJar(Paths.get("")),
    launcher(Paths.get("")),
    tables("res");

    private final Path directory;

    Directory() {
        this.directory = currentDirectory();
    }

    Directory(Path directory) {
        this.directory = directory;
    }

    Directory(String first, String... directory) {
        this.directory = Paths.get(first, directory);
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

    public File[] getFiles() {
        return getFile("").listFiles();
    }

    public static Path currentDirectory() {
        return Paths.get("").toAbsolutePath();
    }

}
