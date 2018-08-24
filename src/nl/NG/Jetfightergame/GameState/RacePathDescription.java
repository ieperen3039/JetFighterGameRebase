package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupColor;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupEntity;
import nl.NG.Jetfightergame.EntityGeneral.Powerups.PowerupType;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Resource;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static java.lang.Float.valueOf;

/**
 * @author Geert van Ieperen. Created on 22-8-2018.
 */
public class RacePathDescription {
    public static final String RACEPATH_EXTENSION = ".rpd";
    private final List<CheckpointDescription> checkpoints;
    private final List<PowerupEntity.Factory> powerups;

    public RacePathDescription(Resource res) {
        checkpoints = new ArrayList<>();
        powerups = new ArrayList<>();

        File file = res.getFileAs(RACEPATH_EXTENSION);
        Scanner in;

        try {
            in = new Scanner(file);
        } catch (FileNotFoundException e) {
            Logger.ERROR.print("Could not open checkpoints of " + file.getName());
            if (ServerSettings.DEBUG) e.printStackTrace();
            return;
        }

        String header = in.nextLine();
        Logger.INFO.print("Reading " + res.name() + ": " + header);
        int lineNr = 1;

        while (in.hasNext()) {
            lineNr++;
            String line = in.nextLine();
            String[] parts = line.split(" ");
            switch (parts[0]) {
                case "c":
                    checkpoints.add(new CheckpointDescription(false, 1, parts));
                    break;
                case "r":
                    checkpoints.add(new CheckpointDescription(true, 1, parts));
                    break;
                case "p":
                    powerups.add(readPowerup(1, parts));
                    break;
                case "#": // ignore
                    break;
                default:
                    Logger.WARN.print("Unknown checkpoint identifier on line " + lineNr + ": " + Arrays.toString(parts));
            }
        }
        in.close();
    }

    /** prints the position and direction in the way it should (radius is always 100) */
    public static void printCheckpointData(boolean visible, PosVector pos, DirVector dir, int radius) {
        String prefix = visible ? "c" : "r";
        System.out.printf(Locale.US,
                prefix + " %.0f %.0f %.0f %.2f %.2f %.2f %d\n",
                pos.x, pos.y, pos.z, dir.x, dir.y, dir.z, radius
        );
    }

    public static void printPowerupData(PosVector pos, PowerupType color) {
        System.out.printf(Locale.US,
                "p %.0f %.0f %.0f %s\n",
                pos.x, pos.y, pos.z, color
        );
    }

    public Iterable<RaceProgress.Checkpoint> getCheckpoints(RaceProgress race, Color4f color) {
        return () -> new CheckpointItr(race, color);
    }

    public Iterable<PowerupEntity.Factory> getPowerups() {
        return Collections.unmodifiableList(powerups);
    }

    private PowerupEntity.Factory readPowerup(int startIndex, String... parts) {
        int i = startIndex;
        PosVector pos = new PosVector(valueOf(parts[i++]), valueOf(parts[i++]), valueOf(parts[i++]));
        PowerupColor color = PowerupColor.valueOf(parts[i]);
        return new PowerupEntity.Factory(pos, color);
    }

    public Pair<PosVector, DirVector> getFirstCheckpoint() {
        CheckpointDescription ch = checkpoints.get(0);
        return new Pair<>(ch.position, ch.direction);
    }

    public static class CheckpointDescription {
        private final PosVector position;
        private final DirVector direction;
        private final float radius;
        private final boolean isRoadpoint;

        private CheckpointDescription(boolean isRoadpoint, int startIndex, String... ch) {
            int i = startIndex;
            this.position = new PosVector(valueOf(ch[i++]), valueOf(ch[i++]), valueOf(ch[i++]));
            this.direction = new DirVector(valueOf(ch[i++]), valueOf(ch[i++]), valueOf(ch[i++]));
            this.radius = valueOf(ch[i]);
            this.isRoadpoint = isRoadpoint;
        }

        /**
         * @param race  the raceprogress to add the checkpoints to
         * @param color the color in case of a real checkpoint
         * @return the resulting checkpoint object
         */
        public RaceProgress.Checkpoint create(RaceProgress race, Color4f color) {
            if (isRoadpoint) {
                return race.addRoadpoint(position, direction, radius);
            } else {
                return race.addCheckpoint(position, direction, radius, color);
            }
        }
    }

    private class CheckpointItr implements Iterator<RaceProgress.Checkpoint> {
        private final RaceProgress race;
        private final Color4f color;
        Iterator<CheckpointDescription> ch;

        public CheckpointItr(RaceProgress race, Color4f color) {
            this.race = race;
            this.color = color;
            ch = checkpoints.iterator();
        }

        @Override
        public boolean hasNext() {
            return ch.hasNext();
        }

        @Override
        public RaceProgress.Checkpoint next() {
            return ch.next().create(race, color);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}