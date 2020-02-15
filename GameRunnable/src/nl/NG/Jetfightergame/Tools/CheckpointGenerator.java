package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.GameState.RacePathDescription;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Intersectionf;

/**
 * @author Geert van Ieperen. Created on 22-8-2018.
 */
public final class CheckpointGenerator {
    private static final int DEFAULT_POINT_RADIUS = 300;
    private static final int MAX_OUT_OF_PATH = 2 * DEFAULT_POINT_RADIUS;
    private static final int CHECKPOINT_MAX = 200;
    private static final int CHECKPOINT_MIN = 75;
    private static final int ROADPOINT_MINIMUM = 100;
    private static final float MAX_CHECKPOINT_DISTANCE = 200f;

    private static PosVector previousPos;
    private static DirVector previousDir;
    private static int lastCheckpoint = 0;
    private static final int NUM_CHECKS = 10;

    public static void printStateOfJet(Environment gameState) {
        gameState.getEntities().stream()
                .filter(e -> e instanceof AbstractJet)
                .map(e -> (AbstractJet) e)
                .findFirst()
                .ifPresent(s -> {
                    printCheckpointData(gameState, s);
                });
    }

    public static void printCheckpointData(Environment gameState, AbstractJet s) {
        PosVector pos = s.getPosition();
        DirVector dir = s.getForward();

        if (previousPos == null || previousDir == null) {
            previousPos = pos;
            previousDir = dir;
            RacePathDescription.printCheckpointData(true, pos, dir, 150);
        }

        DirVector orth = DirVector.zVector();
        orth.cross(dir).normalize(DEFAULT_POINT_RADIUS);

        float angle = (float) ((2 * Math.PI) / NUM_CHECKS);
        float min = DEFAULT_POINT_RADIUS;
        float max = 0;
        for (int i = 0; i < NUM_CHECKS; i++) {
            PosVector newPos = pos.add(orth, new PosVector());
            newPos = gameState.rayTrace(pos, newPos);

            float rayLength = newPos.sub(pos).length();
            if (rayLength < min) {
                min = rayLength;
            } else if (rayLength > max) {
                max = rayLength;
            }
            orth.rotateAxis(angle, dir.x, dir.y, dir.z);
        }
        min *= 1.2f;

        boolean reachable = gameState.rayTrace(previousPos, pos).equals(pos);
        boolean sameDirection = previousDir.angle(dir) < (Math.PI / 4);
        PosVector end = previousPos.add(previousDir, new PosVector());
        boolean liesInPath = Intersectionf.distancePointLine(
                pos.x, pos.y, pos.z,
                previousPos.x, previousPos.y, previousPos.z,
                end.x, end.y, end.z
        ) < MAX_OUT_OF_PATH;
        boolean isLargeEnough = max > ROADPOINT_MINIMUM;
        boolean isInCave = max < DEFAULT_POINT_RADIUS - 1;
        boolean isCloseEnough = previousPos.distanceSquared(pos) < MAX_CHECKPOINT_DISTANCE * MAX_CHECKPOINT_DISTANCE;
        boolean allOfTheAbove = reachable && sameDirection && liesInPath && isLargeEnough && isCloseEnough;


        if ((isInCave && (lastCheckpoint < 20)) || ((lastCheckpoint == 0) && reachable) || allOfTheAbove) {
            lastCheckpoint++;
            RacePathDescription.printCheckpointData(false, pos, dir, (int) max);

        } else {
//                        Logger.DEBUG.print(reachable , sameDirection , liesInPath , isLargeEnough , isCloseEnough, isInCave);
            previousPos.set(pos);
            previousDir.set(dir);
            lastCheckpoint = 0;
            int radius = (int) Math.max(Math.min(min, CHECKPOINT_MAX), CHECKPOINT_MIN);
            RacePathDescription.printCheckpointData(true, pos, dir, radius);
        }
    }
}
