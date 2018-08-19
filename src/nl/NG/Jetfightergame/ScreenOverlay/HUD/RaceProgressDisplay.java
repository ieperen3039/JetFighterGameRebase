package nl.NG.Jetfightergame.ScreenOverlay.HUD;

import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.Player;
import nl.NG.Jetfightergame.GameState.RaceProgress;
import nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings;
import nl.NG.Jetfightergame.ScreenOverlay.ScreenOverlay;
import nl.NG.Jetfightergame.ServerNetwork.ClientConnection;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import org.joml.Vector2f;
import org.lwjgl.nanovg.NanoVG;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static nl.NG.Jetfightergame.ScreenOverlay.HUDStyleSettings.*;

/**
 * @author Geert van Ieperen created on 24-12-2017.
 */
public class RaceProgressDisplay implements Consumer<ScreenOverlay.Painter> {
    private static final int BOX_WIDTH = 450;
    private static final int BOX_HEIGHT = 60;
    private static final float BOX_SPEED = 75f;
    private static final int OUTER_MARGIN = 60;

    private static final int FINISH_BOX_WIDTH = 700;
    private static final int FINISH_BOX_HEIGHT = 800;
    private static final float FINISH_PANEL_SPEED = 700f;
    private static final int FINISH_BOX_INDENT = 10;
    private static final int FINISH_BOX_MARGIN = 50;
    private static final float MORPH_DELTA = 250f;
    private static final float ALPHA_CHANGE_DURATION = 2f;

    private static final Color4f BOX_FILL_COLOR = new Color4f(1, 1, 1, 0.5f);
    private static final Color4f FINISH_BOX_FILL_COLOR = new Color4f(0, 0, 0, 0.7f);

    private final Supplier<RaceProgress> raceSupplier;
    private final GameTimer timer;
    private final Player myself;

    private Map<Integer, Box> boxes;
    private boolean knowHasFinished = false;
    private float alpha;

    private float lastUpdateTime;
    private float deltaTime;

    public RaceProgressDisplay(ClientConnection connection) {
        this.raceSupplier = connection::getRaceProgress;
        this.timer = connection.getTimer();
        this.myself = connection;
        boxes = new HashMap<>();
    }

    @Override
    public void accept(ScreenOverlay.Painter hud) {
        deltaTime = timer.time() - lastUpdateTime;
        lastUpdateTime = timer.time();
        RaceProgress race = raceSupplier.get();

        if (race.getNumCheckpoints() == 0) return;
        List<Integer> ordering = race.raceOrder();
        int nOfPlayers = ordering.size();

        if (race.thisPlayerHasFinished() != knowHasFinished) {
            boxes.replaceAll(WarpingBox::new);
            knowHasFinished = true;
        }

        if (knowHasFinished) {
            float diff = FINISH_BOX_FILL_COLOR.alpha - alpha;
            alpha += Math.min(diff, deltaTime / ALPHA_CHANGE_DURATION);
            hud.roundedRectangle(
                    (hud.windowWidth - FINISH_BOX_WIDTH) / 2,
                    (hud.windowHeight - FINISH_BOX_HEIGHT) / 2,
                    FINISH_BOX_WIDTH, FINISH_BOX_HEIGHT, FINISH_BOX_INDENT,
                    new Color4f(FINISH_BOX_FILL_COLOR, alpha), HUD_COLOR, 5
            );
        }

        for (int i = 0; i < nOfPlayers; i++) {
            int pInd = ordering.get(i);
            Box b = boxes.computeIfAbsent(pInd, Box::new);
            b.setIndex(i);
            b.draw(hud, race, pInd);
        }
    }

    private class Box {
        Vector2f pos;
        Vector2f dimensions;
        int tgtIndex;

        public Box(int tgtIndex) {
            pos = new Vector2f(OUTER_MARGIN, OUTER_MARGIN + tgtIndex * BOX_HEIGHT);
            dimensions = new Vector2f(BOX_WIDTH, BOX_HEIGHT);
            lastUpdateTime = timer.time();
            this.tgtIndex = tgtIndex;
        }

        public void setIndex(int tgtIndex) {
            this.tgtIndex = tgtIndex;
            creep(pos, OUTER_MARGIN, OUTER_MARGIN + tgtIndex * dimensions.y, BOX_SPEED);
        }

        protected void creep(Vector2f pos, float tgtXPos, float tgtYPos, float speed) {
            Vector2f diff = new Vector2f(tgtXPos, tgtYPos).sub(pos);

            float movement = speed * deltaTime;
            if (diff.lengthSquared() < movement * movement) {
                pos.set(tgtXPos, tgtYPos);

            } else {
                diff.normalize(movement);
                pos.add(diff);
            }
        }

        public void draw(ScreenOverlay.Painter hud, RaceProgress race, int pInd) {
            Player player = race.player(pInd);
            if (player == null) return;

            Pair<Integer, Integer> state = race.getState(pInd);
            int roundProgress = (state.right * 100) / race.getNumCheckpoints();
            Color4f color = player.equals(myself) ? Color4f.YELLOW : HUDStyleSettings.HUD_COLOR;

            hud.roundedRectangle(
                    (int) pos.x, (int) pos.y, (int) dimensions.x, (int) dimensions.y,
                    20, BOX_FILL_COLOR, color, HUD_STROKE_WIDTH
            );
            float textMargin = dimensions.y;
            hud.text(
                    (int) (pos.x + textMargin / 2), (int) (pos.y + textMargin / 2),
                    TEXT_SIZE, ScreenOverlay.Font.LUCIDA_CONSOLE, NanoVG.NVG_ALIGN_MIDDLE, color,
                    player.playerName()
            );
            hud.text(
                    (int) ((pos.x + dimensions.x) - textMargin / 2), (int) (pos.y + textMargin / 2),
                    TEXT_SIZE, ScreenOverlay.Font.LUCIDA_CONSOLE, NanoVG.NVG_ALIGN_MIDDLE | NanoVG.NVG_ALIGN_RIGHT, color,
                    String.format("round %d %5s", state.left + 1, "(" + roundProgress + "%)")
            );
        }
    }

    private class WarpingBox extends Box {

        private final int finishPanelHeight;
        private final int finishPanelWidth;

        public WarpingBox(int ind, Box box) {
            super(ind);
            pos.x = box.pos.x;
            pos.y = box.pos.y;
            final int finishBoxArea = FINISH_BOX_HEIGHT - (2 * FINISH_BOX_MARGIN) + FINISH_BOX_MARGIN;
            finishPanelHeight = (finishBoxArea / raceSupplier.get().getNumPlayers()) - FINISH_BOX_MARGIN;
            finishPanelWidth = FINISH_BOX_WIDTH - 2 * FINISH_BOX_MARGIN;
        }

        @Override
        public void setIndex(int tgtIndex) {
            this.tgtIndex = tgtIndex;
        }

        @Override
        public void draw(ScreenOverlay.Painter hud, RaceProgress race, int pInd) {
            int windowWidth = hud.windowWidth;
            int windowHeight = hud.windowHeight;

            int tgtXPos = (windowWidth - FINISH_BOX_WIDTH) / 2 + FINISH_BOX_MARGIN;
            int finishPanelY = (windowHeight - FINISH_BOX_HEIGHT) / 2;
            int tgtYPos = finishPanelY + FINISH_BOX_MARGIN + (finishPanelHeight + FINISH_BOX_MARGIN) * tgtIndex;

            creep(pos, tgtXPos, tgtYPos, FINISH_PANEL_SPEED);
            creep(dimensions, finishPanelWidth, finishPanelHeight, MORPH_DELTA);

            super.draw(hud, race, pInd);
        }
    }
}
