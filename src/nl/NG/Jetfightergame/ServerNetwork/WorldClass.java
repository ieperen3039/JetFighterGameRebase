package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.Assets.Scenarios.PlayerJetLaboratory;
import nl.NG.Jetfightergame.GameState.GameState;

/**
 * @author Geert van Ieperen. Created on 4-7-2018.
 */
public enum WorldClass {
    LOBBY,
    PLAYERJET_LABORATORY;

    public GameState get() {
        switch (this) {
            case LOBBY:
                return new PlayerJetLaboratory();
            case PLAYERJET_LABORATORY:
                return new PlayerJetLaboratory();
            default:
                return null;
        }
    }

    public WorldClass identify(GameState world) {
        if (world instanceof PlayerJetLaboratory) {
            return PLAYERJET_LABORATORY;
        }
        return LOBBY;
    }
}
