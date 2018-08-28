package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.Assets.Entities.FighterJets.AbstractJet;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.EntityGeneral.Hitbox.Collision;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.EntityGeneral.Spectral;
import nl.NG.Jetfightergame.EntityGeneral.StaticEntity;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Rendering.MatrixStack.GL2;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author Geert van Ieperen created on 28-6-2018.
 */
public class RaceProgress {
    private static final float CAPACITY_GROW = 1.2f;
    /** for each player, what its last passed checkpoint is */
    private int[] progressCheckpoint;
    private List<Checkpoint> allPoints;

    private final RaceChangeListener changeListener;
    /** for each player its current round number */
    private int[] progressRound;
    /** all players in the race */
    private Player[] players;
    /** number of registered checkpoints */
    private int nOfCheckpoints;
    /** number of actual valid players in the player array */
    private int nOfPlayers = 0;
    /** number of spots available before re-allocation of arrays is required */
    private int capacity;

    private int thisPlayer = -1;
    private Integer[] raceOrder;
    private int maxRounds = 0;

    private List<Integer> winners;

    /**
     * creates a RaceProgress instance without listener and without players
     */
    public RaceProgress() {
        this(8, (p, c, r) -> {
        });
    }

    /**
     * tracks the progress of all players in the current race. The checkpoints must first be set out be fore the race
     * starts, the players may be added at a later stage as well.
     * If the capacity is less than the given players, then the remaining players are dropped.
     * @param capacity       an expectation of how many players are probably going to participate
     * @param changeListener this class is notified whenever a player crosses a checkpoint
     * @param players        an optional array of initial players. if this array is longer than capacity, the remaining players are dropped
     */
    public RaceProgress(int capacity, RaceChangeListener changeListener, Player... players) {
        this.capacity = capacity;
        this.changeListener = changeListener;
        this.players = Arrays.copyOf(players, capacity);
        this.nOfPlayers = Math.min(players.length, capacity);
        this.allPoints = new ArrayList<>();
        reset();

    }

    /**
     * removes all checkpoints from memory, and resets the ordering of the players. This does not remove any players in
     * the game
     */
    public void reset() {
        raceOrder = new Integer[nOfPlayers];
        setToIndex(raceOrder);

        progressCheckpoint = new int[capacity];
        progressRound = new int[capacity];
        Arrays.fill(progressRound, -1);
        Arrays.fill(progressCheckpoint, -1);
        winners = new ArrayList<>(getNumPlayers());

        nOfCheckpoints = 0;
        allPoints.clear();
    }

    private void setToIndex(Integer[] raceOrder) {
        for (int i = 0; i < raceOrder.length; i++) {
            raceOrder[i] = i;
        }
    }

    public void setThisPlayer(int thisPlayer) {
        this.thisPlayer = thisPlayer;
    }

    public void setMaxRounds(int maxRounds) {
        this.maxRounds = maxRounds;
    }

    /**
     * @param newPlayer a new player to be added to the raceProgress
     * @return the index belonging to this player or its current index if already present
     */
    public int addPlayer(Player newPlayer) {
        int currInd = Arrays.asList(players).indexOf(newPlayer);
        if (currInd != -1) return currInd; // already exists

        nOfPlayers++;
        makeRoomFor(nOfPlayers);

        int pInd = nOfPlayers - 1;
        this.players[pInd] = newPlayer;
        raceOrder = Arrays.copyOf(raceOrder, nOfPlayers);
        raceOrder[pInd] = pInd;
        return pInd;
    }

    private void makeRoomFor(int index) {
        if (index < 0) throw new IllegalArgumentException("negative index: " + index);

        if (index >= capacity) {
            capacity = (int) (index * CAPACITY_GROW) + 1;
            progressRound = Arrays.copyOf(progressRound, capacity);
            progressCheckpoint = Arrays.copyOf(progressCheckpoint, capacity);
            players = Arrays.copyOf(players, capacity);
        }
    }

    public void setPlayer(Player newPlayer, int pIndex) {
        makeRoomFor(pIndex);

        if (players[pIndex] == null) {
            nOfPlayers++;
            raceOrder = Arrays.copyOf(raceOrder, nOfPlayers);
            int newInd = nOfPlayers - 1;
            raceOrder[newInd] = newInd;
        }

        players[pIndex] = newPlayer;
    }

    public Checkpoint addCheckpoint(PosVector position, DirVector direction, float radius, Color4f color) {
        Checkpoint cp = new Checkpoint(
                nOfCheckpoints++, position, direction, radius, color, ClientSettings.CHECKPOINT_ACTIVE_COLOR
        );
        allPoints.add(cp);
        return cp;
    }

    public Checkpoint addRoadpoint(PosVector position, DirVector direction, float radius) {
        RoadPoint rp = new RoadPoint(
                nOfCheckpoints++, position, direction, radius
        );
        allPoints.add(rp);
        return rp;
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

    /** get the next checkpoint of the player with the given identity, or -1 if there are no checkpoints */
    private int nextPointOf(int pInd) {
        if (nOfCheckpoints == 0) return -1;
        if (pInd > players.length) throw new IllegalArgumentException("That player is not part of the race");
        int currCh = progressCheckpoint[pInd];
        if (currCh == nOfCheckpoints) return nOfCheckpoints;

        return (currCh + 1) % nOfCheckpoints;
    }

    public Checkpoint nextPointEntityOf(int pInd, int lookAhead) {
        if (pInd < 0) return allPoints.get(0);
        int index = nextPointOf(pInd);
        if (index < 0) return null;
        index = (index + lookAhead) % nOfCheckpoints;
        return allPoints.get(index);
    }

    /** set the checkpoint of the given player one up */
    private void update(int pInd, int nextCh) {
        // update raceProgress
        progressCheckpoint[pInd] = nextCh;
        if (nextCh == 0) {
            progressRound[pInd]++;
        }

        int round = progressRound[pInd];
        if (nextCh == 0 && round == maxRounds) {
            winners.add(pInd);
        }

        changeListener.playerCheckpointUpdate(pInd, nextCh, round);
    }

    /**
     * set the given player to the given state
     * @param pInd    the index of the player as of {@link #getPlayerInd(Player)}
     * @param chProg  the number of the last passed checkpoint
     * @param roundNr the current round number
     */
    public void setState(int pInd, int chProg, int roundNr) {
        progressCheckpoint[pInd] = chProg;
        progressRound[pInd] = roundNr;

        if (chProg == 0 && roundNr == maxRounds) {
            winners.add(pInd);
        }
    }

    public Pair<Integer, Integer> getState(int pInd) {
        if (pInd == -1) return null;
        return new Pair<>(Math.max(progressRound[pInd], 0), Math.max(progressCheckpoint[pInd], 0));
    }

    public Player[] players() {
        return Arrays.copyOf(players, nOfPlayers);
    }

    public Player player(int pInd) {
        return players[pInd];
    }

    /**
     * @return an array with on position i the player on position i in the race.
     */
    public List<Integer> raceOrder() {
        Toolbox.insertionSort(raceOrder, this::playerOrdering);
        return Collections.unmodifiableList(Arrays.asList(raceOrder));
    }

    /**
     * @param pInd index of a player
     * @return a float such that player[pInd] is lower than another player if it is further in the race
     */
    private Float playerOrdering(Integer pInd) {
        float pos = winners.indexOf(pInd);
        if (pos < 0) return -Math.max(progressRound[pInd] * nOfCheckpoints + progressCheckpoint[pInd], 0f);
        else return pos - ((maxRounds + 1) * nOfCheckpoints) - players.length;
    }

    /**
     * @param p any player
     * @return the index of the player, or -1 if the player is not in this list
     */
    public int getPlayerInd(Player p) {
        return Arrays.asList(players).indexOf(p);
    }

    public int getNumCheckpoints() {
        return nOfCheckpoints;
    }

    public int getNumPlayers() {
        return nOfPlayers;
    }

    public boolean thisPlayerHasFinished() {
        return winners.contains(thisPlayer);
    }

    public boolean hasFinished(int pInd) {
        return winners.contains(pInd);
    }

    public boolean allFinished() {
        return (nOfCheckpoints == 0) || (winners.size() == nOfPlayers);
    }

    public Checkpoint nextCheckpointOf(int pInd) {
        return allPoints.get(nextCheckpointIndOf(pInd));
    }

    private int nextCheckpointIndOf(int pInd) {
        int i = nextPointOf(pInd);
        if (i < 0) return -1;
        for (; i < allPoints.size(); i++) {
            Checkpoint ch = allPoints.get(i);
            if (ch.visible()) return i;
        }

        return 0;
    }

    public int getNumRounds() {
        return maxRounds;
    }

    /**
     * @author Geert van Ieperen created on 28-6-2018.
     */
    public class Checkpoint extends StaticEntity implements Spectral {
        private final PosVector position;
        private final float radius;
        private Color4f activeColor;
        final int checkpointNumber;

        private Checkpoint(int pointNumber, PosVector position, DirVector direction, float radius, Color4f color, Color4f activeColor) {
            super(GeneralShapes.CHECKPOINTRING, Material.SILVER, color, position, radius, Toolbox.xTo(direction));
            this.checkpointNumber = pointNumber;
            this.position = position;
            this.radius = radius;
            this.activeColor = activeColor;
        }

        @Override
        public void acceptCollision(Collision cause) {
            MovingEntity source = cause.source();
            if (source instanceof AbstractJet) {
                int pInd = indexIfPlayer((AbstractJet) source);
                if (pInd < 0) {
                    return;
                }

                int nextCh = next(pInd);
                // check for passing the right checkpoint
                if (nextCh == checkpointNumber) {
                    update(pInd, checkpointNumber);
                }
            }
        }

        protected boolean visible() {
            return true;
        }

        protected int next(int pInd) {
            return nextCheckpointIndOf(pInd);
        }

        @Override
        public void preDraw(GL2 gl) {
            Color4f color = this.color;
            if (thisPlayer != -1 && nextCheckpointIndOf(thisPlayer) == checkpointNumber) {
                color = activeColor;
            }

            gl.setMaterial(material, color);
        }

        @Override
        public float getRange() {
            return radius;
        }

        @Override
        public PosVector getExpectedMiddle() {
            return new PosVector(position);
        }
    }

    private class RoadPoint extends Checkpoint {
        public RoadPoint(int pointNumber, PosVector position, DirVector direction, float radius) {
            super(pointNumber, position, direction, radius, Color4f.INVISIBLE, Color4f.WHITE);
        }

        @Override
        protected int next(int pInd) {
            return nextPointOf(pInd);
        }

        @Override
        public void preDraw(GL2 gl) {
        }

        @Override
        public void draw(GL2 gl) {
        }

        @Override
        protected boolean visible() {
            return false;
        }
    }

    public interface RaceChangeListener {
        void playerCheckpointUpdate(int pInd, int checkpointProgress, int roundsProgress);
    }
}
