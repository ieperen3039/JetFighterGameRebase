package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityFactory;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.ServerNetwork.ClientConnection;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static nl.NG.Jetfightergame.Settings.ClientSettings.RENDER_DELAY;

/**
 * @author Geert van Ieperen. Created on 21-8-2018.
 */
public class StateReader extends ClientConnection {
    public static final float LOOK_AHEAD = RENDER_DELAY * 2;
    private AbstractJet firstJet;
    private final boolean liveAction;
    private Runnable exitGame;
    private boolean raceIsInProgress = false;

    /**
     * @param file       the file to read
     * @param jet
     * @param exitGame
     * @throws IOException whenever it feels like
     */
    public StateReader(File file, boolean liveAction, EntityFactory jet, Runnable exitGame) throws IOException {
        super("StateReader", file, liveAction ? new GameTimer() : new StaticTimer(ClientSettings.TARGET_FPS), jet, 10);
        this.liveAction = liveAction;
        this.exitGame = exitGame;
        firstJet = super.jet();
    }

    @Override
    public void listen() {
        try {
            while (handleMessage()) {
                float currentTime = getTimer().getRenderTime().current();
                float dt = maxServerTime - currentTime;
                if (!liveAction && dt > LOOK_AHEAD) {
                    float percent = getPercent();
                    if (percent > 100) {
                        System.out.printf("\r[INFO ]: Recorded %.1f sec (Aftermatch)", currentTime);
                    } else {
                        System.out.printf("\r[INFO ]: Recorded %.1f sec (%3.1f%%)", currentTime, percent);
                    }
                    int millis = (int) (500 * dt);
                    Toolbox.waitFor(millis);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            cleanup();
        }

        exitGame.run();
    }

    private float getPercent() {
        RaceProgress race = getRaceProgress();
        int lastPos = race.getNumPlayers() - 1;
        int last = race.raceOrder().get(lastPos);
        Pair<Integer, Integer> state = race.getState(last);
        float nOfRounds = race.getNumRounds();
        float raceProgress = state.left * nOfRounds;
        float roundProgress = (state.right / nOfRounds) / race.getNumCheckpoints();
        return 100 * (raceProgress + roundProgress);
    }

    @Override
    protected void update(float deltaTime) {
        RaceProgress race = getRaceProgress();
        List<Integer> ordering = race.raceOrder();

        if (ordering.isEmpty() || race.thisPlayerHasFinished()) {
            firstJet = super.jet();

        } else {
            if (!raceIsInProgress) {
                focusTheFinish(race);
                raceIsInProgress = true;
            }

            Player first = race.player(ordering.get(0));
            firstJet = first.jet();
            race.setThisPlayer(ordering.get(0));
        }
    }

    private void focusTheFinish(RaceProgress race) {
        RaceProgress.Checkpoint ch = race.nextPointEntityOf(-1, 0);
        Quaternionf chRot = ch.getRotation().invert();
        Vector3f localZ = DirVector.zVector().rotate(chRot);
        chRot.rotateTo(localZ, DirVector.zVector());
        DirVector addition = new DirVector(-100, 0, 0);
        addition.rotate(chRot);
        PosVector pos = ch.getExpectedMiddle();
        pos.add(addition);
        super.jet().set(pos, DirVector.zeroVector(), chRot, 0);
    }

    @Override
    public void pause() {
        getTimer().pause();
        super.pause();
    }

    @Override
    public void unPause() {
        getTimer().unPause();
        super.unPause();
    }

    @Override
    protected void worldSwitch() {

    }

    @Override
    public AbstractJet jet() {
        return firstJet;
    }
}
