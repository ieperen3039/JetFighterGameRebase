package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.Assets.Scenarios.CubeWorld;
import nl.NG.Jetfightergame.Assets.Scenarios.IslandMap;
import nl.NG.Jetfightergame.Assets.Scenarios.PlayerJetLaboratory;
import nl.NG.Jetfightergame.GameState.Environment;
import nl.NG.Jetfightergame.GameState.GameState;

/**
 * @author Geert van Ieperen. Created on 4-7-2018.
 */
public enum EnvironmentClass {
    LOBBY, VOID,
    CUBE_MAP, ISLAND_MAP;

    private static final EnvironmentClass[] VALUES = values();
    public static EnvironmentClass[] raceWorlds = {ISLAND_MAP, CUBE_MAP};

    public GameState create() {
        switch (this) {
            case LOBBY:
                return new PlayerJetLaboratory();
            case CUBE_MAP:
                return new CubeWorld();
            case ISLAND_MAP:
                return new IslandMap();
            default:
                return new Environment.Void();
        }
    }

    public static EnvironmentClass get(int id) {
        if (id >= VALUES.length) throw new IllegalArgumentException("Invalid worldclass identifier " + id);
        else return VALUES[id];
    }

    public static String asString(int id) {
        return id < VALUES.length ? get(id).toString() : id + " (Invalid world id)";
    }
}
