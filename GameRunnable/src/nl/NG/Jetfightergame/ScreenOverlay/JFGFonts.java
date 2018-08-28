package nl.NG.Jetfightergame.ScreenOverlay;

import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Resources;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;

import static nl.NG.Jetfightergame.Tools.Directory.fonts;

/**
 * @author Geert van Ieperen. Created on 23-8-2018.
 */
public enum JFGFonts {
    ORBITRON_REGULAR(fonts, "Orbitron", "Orbitron-Regular.ttf"),
    ORBITRON_MEDIUM(fonts, "Orbitron", "Orbitron-Medium.ttf"),
    ORBITRON_BOLD(fonts, "Orbitron", "Orbitron-Bold.ttf"),
    ORBITRON_BLACK(fonts, "Orbitron", "Orbitron-Black.ttf"),
    LUCIDA_CONSOLE(fonts, "LucidaConsole", "lucon.ttf");

    public final String name;
    public final String source;
    private ByteBuffer byteFormat;
    private Font awtFormat;

    JFGFonts(Directory dir, String... filepath) {
        this.name = toString().toLowerCase().replace("_", " ");
        this.source = dir.getPath(filepath).toString();
        File file = Directory.fonts.getFile(filepath);
        Path path = Directory.fonts.getPath(filepath);

        try {
            byteFormat = Resources.toByteBuffer(path, file, 96 * 1024);
            awtFormat = Font.createFont(Font.TRUETYPE_FONT, file);

        } catch (IOException | FontFormatException e) {
            if (ServerSettings.DEBUG) {
                e.printStackTrace();
            } else {
                Logger.ERROR.print("Error loading " + name + ": " + e.getMessage());
            }
        }
    }

    ByteBuffer asByteBuffer() {
        return byteFormat;
    }

    public java.awt.Font asAWTFont(float size) {
        return awtFormat.deriveFont(size);
    }
}
