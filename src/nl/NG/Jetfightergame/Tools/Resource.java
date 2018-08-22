package nl.NG.Jetfightergame.Tools;

import java.io.File;
import java.nio.file.Path;

/**
 * @author Geert van Ieperen. Created on 22-8-2018.
 */
public enum Resource {
    GLITCHMAP(Directory.meshes, "maps", "GlitchIsland");

    Path localPath;

    Resource(Directory dir, String... path) {
        localPath = dir.getPath(path);
    }

    public File getFile() {
        return localPath.toFile();
    }

    public File getWithExtension(String ext) {
        return new File(localPath.toString() + ext);
    }

    public Path getPath() {
        return localPath;
    }
}
