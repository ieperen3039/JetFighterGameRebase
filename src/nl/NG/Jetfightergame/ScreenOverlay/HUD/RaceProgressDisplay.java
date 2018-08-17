package nl.NG.Jetfightergame.ScreenOverlay.HUD;

import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import org.lwjgl.nanovg.NanoVG;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings.HUD_STROKE_WIDTH;
import static nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings.TEXT_SIZE;

/**
 * @author Geert van Ieperen created on 24-12-2017.
 */
public class RaceProgressDisplay implements Consumer<ScreenOverlay.Painter> {
    private static final int WIDTH = 400;
    private static final int HEIGHT = 80;
    private static final int MARGIN = 50;

    private static final float BOX_SPEED = 20f;
    private static final Color4f BOX_FILL_COLOR = new Color4f(1, 1, 1, 0.5f);

    private List<Box> boxes;

    private final Supplier<RaceProgress> raceSupplier;
    private final GameTimer timer;

    public RaceProgressDisplay(Supplier<RaceProgress> connection, GameTimer timer) {
        raceSupplier = connection;
        this.timer = timer;
        boxes = new ArrayList<>();
    }

    @Override
    public void accept(ScreenOverlay.Painter hud) {
        RaceProgress race = raceSupplier.get();
        if (race.getNumCheckpoints() == 0) return;
        Integer[] raceOrder = race.raceOrder();
        int nOfPlayers = raceOrder.length;

        // add missing boxes
        if (nOfPlayers > boxes.size()) {
            for (int i = boxes.size(); i < nOfPlayers; i++) {
                boxes.add(new Box(MARGIN, MARGIN, i));
            }
        }

        for (int i = 0; i < nOfPlayers; i++) {
            Box b = boxes.get(raceOrder[i]);
            b.setIndex(nOfPlayers - 1 - i);
            b.updatePosition();
            b.draw(hud, race);
        }
    }

    private class Box {
        final int pInd;
        int xPos;
        int yPos;
        int tgtIndex;
        float lastUpdateTime;

        public Box(int xPos, int yPos, int pInd) {
            this.xPos = xPos;
            this.yPos = yPos;
            this.pInd = pInd;
        }

        public void setIndex(int tgtIndex) {
            this.tgtIndex = tgtIndex;
        }

        public void updatePosition() {
            int tgtYPos = MARGIN + tgtIndex * HEIGHT;
            int diff = tgtYPos - yPos;
            float creep = (timer.time() - lastUpdateTime) * BOX_SPEED;

            if (diff > 0) {
                yPos += Math.min(diff, creep);
            } else {
                yPos += Math.max(diff, -creep);
            }
        }

        public void draw(ScreenOverlay.Painter hud, RaceProgress race) {
            Player player = race.players().get(pInd);
            Pair<Integer, Integer> state = race.getState(pInd);
            int roundProgress = (state.right * 100) / race.getNumCheckpoints();
            Color4f color = pInd == 0 ? Color4f.YELLOW : HUDStyleSettings.HUD_COLOR;

            hud.roundedRectangle(xPos, yPos, WIDTH, HEIGHT, 20, BOX_FILL_COLOR, color, HUD_STROKE_WIDTH);
            hud.text(
                    xPos + WIDTH / 2, yPos + HEIGHT / 2,
                    TEXT_SIZE, ScreenOverlay.Font.LUCIDA_CONSOLE, NanoVG.NVG_ALIGN_CENTER, color,
                    String.format("%s | round %d (%d%%)", player.playerName(), state.left + 1, roundProgress)
            );
        }
    }
}
