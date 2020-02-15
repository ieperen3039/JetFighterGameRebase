package nl.NG.Jetfightergame.Tools;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Geert van Ieperen. Created on 22-8-2018.
 */
public enum Resource {
    GLITCHMAP(Directory.meshes, "maps", "GlitchIsland"),
    CUBEMAP(Directory.meshes, "maps", "map_cube"),
    SEA(Directory.meshes, "background", "water.obj");

    private String localPath;

    Resource(Directory dir, String... path) {
        localPath = dir.getPath(path).toString();
    }

    public File getFileAs(String ext) {
        return new File(localPath + ext);
    }

    public Path getPathAs(String ext) {
        return Paths.get(localPath + ext);
    }
}
