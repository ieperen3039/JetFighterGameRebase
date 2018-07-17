package nl.NG.Jetfightergame.Tools;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Geert van Ieperen
 * created on 10-2-2018.
 */
public enum Directory {
    music("res/music/"),
    soundEffects("res/sounds/"),
    fonts("res/fonts/"),
    shaders("res/shaders/"),
    meshes("res/models/"),
    pictures("res/pictures/"),
    recordings("ScreenShots/");

    private final String path;

    Directory(String path) {
        this.path = path;
    }

    public File getFile(String fileName) {
        return new File(pathOf(fileName));
    }

    public Path getPath(String fileName) {
        return Paths.get(pathOf(fileName));
    }

    public boolean exists(String fileName){
        return getFile(fileName).exists();
    }

    public String pathOf(String fileName) {
        return path + fileName;
    }
}
