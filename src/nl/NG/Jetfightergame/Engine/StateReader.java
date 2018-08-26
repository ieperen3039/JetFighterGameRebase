package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Camera.CameraFocusMovable;
import nl.NG.Jetfightergame.Camera.CameraManager;
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

import static nl.NG.Jetfightergame.Camera.CameraManager.CameraImpl.FollowingCamera;
import static nl.NG.Jetfightergame.Camera.CameraManager.CameraImpl.SpectatorFollowing;
import static nl.NG.Jetfightergame.Settings.ClientSettings.RENDER_DELAY;

/**
 * @author Geert van Ieperen. Created on 21-8-2018.
 */
public class StateReader extends ClientConnection {
    private static final float LOOK_AHEAD = RENDER_DELAY * 2;
    public static final int READER_TPS = 50;
    private AbstractJet focusJet;
    private final CameraManager camera;
    private Runnable exitGame;
    private boolean raceIsInProgress = false;

    private SpectatorModus modus = SpectatorModus.Follow_First;
    private boolean didClick = false;
    private int playerFocus = 0;
    private StaticTimer staticTimer = new StaticTimer(READER_TPS);

    /**
     * @param file       the file to read
     * @param jet
     * @param camera
     * @param exitGame
     * @throws IOException whenever it feels like
     */
    public StateReader(File file, boolean liveAction, EntityFactory jet, CameraManager camera, Runnable exitGame) throws IOException {
        super("StateReader", file, liveAction ? new GameTimer() : new StaticTimer(ClientSettings.TARGET_FPS), jet, READER_TPS);
        this.camera = camera;
        this.exitGame = exitGame;
        focusJet = super.jet();
        staticTimer.unPause();
    }

    @Override
    public void listen() {
        try {
            while (handleMessage()) {
                float currentTime = getTimer().getRenderTime().current();
                float dt = maxServerTime - currentTime;
                if (dt > LOOK_AHEAD) {
                    float percent = getPercent();
                    if (percent > 100) {
                        System.out.printf("\r[INFO ]: Recorded %.1f sec (Aftermatch)", currentTime);
                    } else {
//                        System.out.printf("\r[INFO ]: Recorded %.1f sec (%3.1f%%)", currentTime, percent);
                    }
                    int millis = (int) (500 * dt);
                    Toolbox.waitFor(millis);
                }
            }

            float currentTime = getTimer().getRenderTime().current();
            Toolbox.waitFor((int) (1000 * (maxServerTime - currentTime)));
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
        switch (modus) {
            case Follow_First:
                setJetToFirst();
                break;

            case Players_View:
                boolean next = getInput().primaryFire();
                boolean prev = getInput().secondaryFire();
                boolean doClick = next || prev;

                if (doClick && !didClick) {
                    RaceProgress race = getRaceProgress();
                    if (next) playerFocus++;
                    if (prev) playerFocus--;
                    int i = playerFocus;
                    Player[] players = race.players();
                    focusJet = players[Math.floorMod(i, players.length)].jet();
                }
                didClick = doClick;
                break;

            case Roam_Paused:
                staticTimer.updateGameTime();
                staticTimer.updateRenderTime();
                float dt = staticTimer.getRenderTime().difference();
                camera.updatePosition(dt);
                // continue
            case Free_Roam:
                focusJet.preUpdate(DirVector.zeroVector());
                focusJet.update();
                break;
        }
    }

    private void setJetToFirst() {
        RaceProgress race = getRaceProgress();
        List<Integer> ordering = race.raceOrder();

        if (ordering.isEmpty() || race.thisPlayerHasFinished()) {
            focusJet = super.jet();

        } else {
            if (!raceIsInProgress) {
                focusTheFinish(race);
                raceIsInProgress = true;
            }

            Player first = race.player(ordering.get(0));
            focusJet = first.jet();
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
        if (modus != SpectatorModus.Roam_Paused) {
            getTimer().pause();
        }
        super.pause();
    }

    @Override
    public void unPause() {
        if (modus != SpectatorModus.Roam_Paused) {
            getTimer().unPause();
        }
        super.unPause();
    }

    @Override
    protected void worldSwitch() {

    }

    @Override
    public AbstractJet jet() {
        return focusJet;
    }

    public void setModus(SpectatorModus newModus) {
        switch (modus) {
            // cleanup
            case Roam_Paused:
                focusJet.setController(null);
                super.jet().setController(getInput());
        }

        modus = newModus;

        setJetToFirst();
        PosVector pos = focusJet.getPosition();
        Quaternionf rot = focusJet.getRotation();

        switch (newModus) {
            case Follow_First:
                camera.switchTo(SpectatorFollowing);
                break;

            case Free_Roam:
                focusJet = super.jet();
                focusJet.set(pos, DirVector.zeroVector(), rot, 0);
                camera.switchTo(FollowingCamera);
                break;

            case Roam_Paused:
                super.jet().setController(null);
                focusJet = new CameraFocusMovable(pos, rot, staticTimer, false);
                focusJet.setController(getInput());
                camera.switchTo(FollowingCamera);
                break;

            case Players_View:
                camera.switchTo(FollowingCamera);
                break;
        }
    }

    public enum SpectatorModus {
        Follow_First, Free_Roam, Roam_Paused, Players_View;
    }
}
