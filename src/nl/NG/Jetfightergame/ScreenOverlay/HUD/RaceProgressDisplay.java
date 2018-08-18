package nl.NG.Jetfightergame.ScreenOverlay.HUD;

import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ServerNetwork.ClientConnection;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import org.lwjgl.nanovg.NanoVG;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private static final float BOX_SPEED = 0.05f;
    private static final Color4f BOX_FILL_COLOR = new Color4f(1, 1, 1, 0.5f);

    private Map<Integer, Box> boxes;

    private final Supplier<RaceProgress> raceSupplier;
    private final GameTimer timer;
    private final Player myself;

    public RaceProgressDisplay(ClientConnection connection) {
        this.raceSupplier = connection::getRaceProgress;
        this.timer = connection.getTimer();
        this.myself = connection;
        boxes = new HashMap<>();
    }

    @Override
    public void accept(ScreenOverlay.Painter hud) {
        RaceProgress race = raceSupplier.get();
        if (race.getNumCheckpoints() == 0) return;
        List<Integer> ordering = race.raceOrder();
        int nOfPlayers = ordering.size();

        for (int i = 0; i < nOfPlayers; i++) {
            int pInd = ordering.get(i);
            int yPos = MARGIN + HEIGHT * i;
            Box b = boxes.computeIfAbsent(pInd, (ind) -> new Box(MARGIN, yPos));
            b.setIndex(i);
            b.draw(hud, race, pInd);
        }
    }

    private class Box {
        int xPos;
        int yPos;
        float lastUpdateTime;
        private int tgtYPos;

        public Box(int xPos, int yPos) {
            this.xPos = xPos;
            this.yPos = yPos;
        }

        public void setIndex(int tgtIndex) {
            tgtYPos = MARGIN + tgtIndex * HEIGHT;
        }

        private void updatePosition() {
            int diff = tgtYPos - yPos;
            float creep = (timer.time() - lastUpdateTime) * BOX_SPEED;

            if (diff > 0) {
                yPos += Math.min(diff, creep);
            } else {
                yPos += Math.max(diff, -creep);
            }
        }

        public void draw(ScreenOverlay.Painter hud, RaceProgress race, int pInd) {
            updatePosition();

            Player player = race.players().get(pInd);
            if (player == null) return;

            Pair<Integer, Integer> state = race.getState(pInd);
            int roundProgress = (state.right * 100) / race.getNumCheckpoints();
            Color4f color = player.equals(myself) ? Color4f.YELLOW : HUDStyleSettings.HUD_COLOR;

            hud.roundedRectangle(xPos, yPos, WIDTH, HEIGHT, 20, BOX_FILL_COLOR, color, HUD_STROKE_WIDTH);
            hud.text(
                    xPos + HEIGHT / 2, yPos + HEIGHT / 2,
                    TEXT_SIZE, ScreenOverlay.Font.LUCIDA_CONSOLE, NanoVG.NVG_ALIGN_MIDDLE, color,
                    String.format("%s | round %d (%d%%)", player.playerName(), state.left + 1, roundProgress)
            );
        }
    }
}
