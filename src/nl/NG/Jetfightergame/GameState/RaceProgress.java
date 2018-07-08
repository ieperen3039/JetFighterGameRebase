package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.AbstractEntities.AbstractJet;
import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.Spectral;
import nl.NG.Jetfightergame.AbstractEntities.StaticEntity;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.Arrays;
import java.util.function.Consumer;

/**
 * @author Geert van Ieperen created on 28-6-2018.
 */
public class RaceProgress {
    private static final float CAPACITY_GROW = 1.2f;
    /** for each player, what its last passed checkpoint is */
    private int[] progressCheckpoint;

    private final RaceChangeListener changeListener;
    /** for each player its current round number */
    private int[] progressRound;
    /** all players in the race */
    private Player[] players;
    /** number of registered checkpoints */
    private int nOfCheckpoints = 0;
    /** number of actual valid players in the player array */
    private int nOfPlayers = 0;
    /** number of spots available before re-allocation of arrays is required */
    private int capacity;

    /**
     * creates a RaceProgress instance without listener and without players
     */
    public RaceProgress() {
        this(0, (p, c, r) -> {
        });
    }

    /**
     * tracks the progress of all players in the current race. The checkpoints must first be set out be fore the race
     * starts, the players may be added at a later stage as well.
     * If the capacity is less than the given players, then the remaining players are dropped.
     * @param capacity       an expectation of how many players are probably going to participate
     * @param changeListener this class is notified whenever a player crosses a checkpoint
     * @param players        an optional array of initial players.
     */
    public RaceProgress(int capacity, RaceChangeListener changeListener, Player... players) {
        this.capacity = capacity;
        this.progressRound = new int[capacity];
        this.progressCheckpoint = new int[capacity];
        this.changeListener = changeListener;
        this.players = Arrays.copyOf(players, capacity);
        this.nOfPlayers = Math.min(players.length, capacity);
    }

    public RaceProgress(RaceProgress source) {
        players = source.players.clone();
        nOfPlayers = source.nOfPlayers;
        capacity = source.nOfPlayers;
        changeListener = source.changeListener;

        progressCheckpoint = new int[capacity];
        progressRound = new int[capacity];
        nOfCheckpoints = 0;
    }

    public void addPlayer(Player newPlayer) {
        nOfPlayers++;
        if (nOfPlayers > capacity) {
            capacity = (int) (capacity * CAPACITY_GROW) + 1;
            progressRound = Arrays.copyOf(progressRound, capacity);
            progressCheckpoint = Arrays.copyOf(progressCheckpoint, capacity);
            players = Arrays.copyOf(players, capacity);
        }
        players[nOfPlayers - 1] = newPlayer;
    }

    public Checkpoint addCheckpoint(PosVector position, DirVector direction, float radius, Color4f color) {
        return new Checkpoint(
                nOfCheckpoints++, position, direction, radius, color
        );
    }

    /** returns the index of the player with the given name, or -1 if no such player is registered */
    private int identify(String name) {
        for (int i = 0; i < nOfPlayers; i++) {
            if (name.equals(players[i].playerName())) {
                return i;
            }
        }
        return -1;
    }

    /** @return the index of the player with p as its jet, or -1 if no such player is registered */
    private int indexIfPlayer(AbstractJet p) {
        for (int i = 0; i < nOfPlayers; i++) {
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
        changeListener.playerCheckpointUpdate(players[pInd], nextCh, progressRound[pInd]);
    }

    public void setState(String playerName, int chProg, int i) {
        int pInd = identify(playerName);
        progressCheckpoint[pInd] = chProg;
        progressRound[pInd] = i;
    }

    public Pair<Integer, Integer> getState(Player player) {
        int pInd = Arrays.asList(players).indexOf(player);
        if (pInd == -1) return null;
        return new Pair<>(progressRound[pInd], progressCheckpoint[pInd]);
    }

    public void forEachPlayer(Consumer<Player> action) {
        for (Player p : players) {
            action.accept(p);
        }
    }

    /**
     * @author Geert van Ieperen created on 28-6-2018.
     */
    public class Checkpoint extends StaticEntity implements Spectral {
        private final int checkpointNumber;
        private final PosVector position;
        private final float radius;

        private Checkpoint(int checkpointNumber, PosVector position, DirVector direction, float radius, Color4f color) {
            super(GeneralShapes.CHECKPOINTRING, Material.SILVER, color, position, radius, Toolbox.xTo(direction));
            this.checkpointNumber = checkpointNumber;
            this.position = position;
            this.radius = radius;
        }

        @Override
        public void acceptCollision(Collision cause) {
            MovingEntity source = cause.source();
            if (source instanceof AbstractJet) {
                int pInd = indexIfPlayer((AbstractJet) source);
                if (pInd < 0) {
                    return;
                }

                int nextCh = nextCheckpointOf(pInd);
                // check for passing the right checkpoint
                if (nextCh == checkpointNumber) {
                    update(pInd, nextCh);
                }
            }
        }

        @Override
        public float getRange() {
            return radius;
        }

        @Override
        public PosVector getExpectedMiddle() {
            return position;
        }
    }

    public interface RaceChangeListener {
        void playerCheckpointUpdate(Player player, int checkpointProgress, int roundsProgress);
    }
}
