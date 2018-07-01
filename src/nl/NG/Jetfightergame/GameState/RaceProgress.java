package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.StaticObject;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;

/**
 * @author Geert van Ieperen created on 28-6-2018.
 */
public class RaceProgress {
    /** for each player, what its last passed checkpoint is */
    private final int[] progressCheckpoint;
    /** for each player its current round number */
    private final int[] progressRound;
    /** all players in the race */
    private final Player[] players;
    private int nOfCheckpoints = 0;

    public RaceProgress(Player[] players) {
        int nOfPlayers = players.length;
//        this.checkpoints = new ArrayList<>();
        this.progressRound = new int[nOfPlayers];
        this.progressCheckpoint = new int[nOfPlayers];
        this.players = players.clone();
    }

    public void addCheckpoint(Vector position, DirVector direction, float radius, Color4f color) {
        new Checkpoint(
                nOfCheckpoints++, position, direction, radius, color, this
        );
    }

    /** returns the player belonging to the given jet, if any */
    public Player identify(AbstractJet entity) {
        for (Player p : players) {
            if (p.jet() == entity) {
                return p;
            }
        }
        return null;
    }

    /** @return the index of the player with p as its jet, or -1 if no such player is registered */
    private int indexIfPlayer(AbstractJet p) {
        for (int i = 0; i < players.length; i++) {
            if (p == players[i].jet()) {
                return i;
            }
        }
        return -1;
    }

    /** get the next checkpoint of the player with the given identity */
    private int nextCheckpointOf(int pInd) {
        int nextCh = progressCheckpoint[pInd] + 1;
        if (nextCh == nOfCheckpoints) {
            nextCh = 0;
        }
        return nextCh;
    }

    /** set the checkpoint of the given player one up */
    private void update(int pInd, int nextCh) {
        // update raceProgress
        progressCheckpoint[pInd] = nextCh;
        if (nextCh == 0) {
            progressRound[pInd]++;
        }
    }

    /**
     * @author Geert van Ieperen created on 28-6-2018.
     */
    public class Checkpoint extends StaticObject {
        private final int id;

        public Checkpoint(int id, Vector position, DirVector direction, float radius, Color4f color, RaceProgress progressTracker) {
            super(GeneralShapes.CHECKPOINTRING, Material.GOLD, color, position, radius, Toolbox.xTo(direction));
            this.id = id;
        }

        @Override
        public void acceptCollision(Collision cause) {
            MovingEntity source = cause.source();
            if (source instanceof AbstractJet) {

                int pInd = indexIfPlayer((AbstractJet) source);
                if (pInd > 0) {
                    int nextCh = nextCheckpointOf(pInd);
                    // check for passing the right checkpoint
                    if (nextCh == id) {
                        update(pInd, nextCh);
                    }
                }
            }
        }
    }
}
