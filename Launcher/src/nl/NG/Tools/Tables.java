package nl.NG.Tools;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Geert van Ieperen. Created on 28-8-2018.
 */
public class Tables {
    private final String[] jets;
    private final String[] worlds;
    private final boolean isLoaded;


    public Tables(File file) throws IOException {
        if (!file.exists()) {
            Logger.WARN.print("Name tables not found at " + file.getName());
            jets = new String[]{"UNKNOWN"};
            worlds = new String[]{"UNKNOWN"};
            isLoaded = false;
            return;
        }

        DataInputStream in = new DataInputStream(new FileInputStream(file));
        {
            int jl = in.readInt();
            jets = new String[jl];
            for (int i = 0; i < jl; i++) {
                jets[i] = in.readUTF();
            }
        }
        {
            int wl = in.readInt();
            worlds = new String[wl];
            for (int i = 0; i < wl; i++) {
                worlds[i] = in.readUTF();
            }
        }
        isLoaded = true;
    }

    public String[] getJets() {
        return jets.clone();
    }

    public String[] getWorlds() {
        return worlds.clone();
    }

    public String findWorld(String island) {
        return findClosest(island, worlds);
    }

    public String findJet(String jetName) {
        return findClosest(jetName, jets);
    }

    private String findClosest(String target, String[] options) {
        int max = 0;
        int lengthOfMax = Integer.MAX_VALUE;
        String best = "";

        for (String world : options) {
            int wordLength = Math.abs(world.length() - target.length());
            int dist = lcs(target, world);

            if (dist > max || (dist == max && wordLength < lengthOfMax)) {
                max = dist;
                lengthOfMax = wordLength;
                best = world;
            }
        }

        return best;
    }

    @SuppressWarnings("Duplicates")
    private int lcs(String a, String b) {
        int m = a.length();
        int n = b.length();
        int[][] cMat = new int[m + 1][n + 1]; // initialized at 0

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                char ca = a.charAt(i - 1);
                char cb = b.charAt(j - 1);
                if (ca == cb) {
                    cMat[i][j] = cMat[i - 1][j - 1] + 1;
                } else {
                    cMat[i][j] = Math.max(cMat[i][j - 1], cMat[i - 1][j]);
                }
            }
        }

        return cMat[m][n];
    }

    public boolean loadedSuccessful() {
        return isLoaded;
    }

    public void reload(File file) {

    }
}
