package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Assets.FighterJets.BasicJet;
import nl.NG.Jetfightergame.Assets.GeneralEntities.FallingCube;
import nl.NG.Jetfightergame.Assets.GeneralEntities.SimpleBullet;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.GameState.EntityReceiver;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

/**
 * @author Geert van Ieperen created on 10-5-2018.
 */
public enum  EntityClass {
    BASIC_JET, SIMPLE_BULLET, FALLING_CUBE;

    /**
     * @param id a number n corresponing to an enum ordinal
     * @return the enum e such that {@code e.ordinal() == n}
     * @throws IllegalArgumentException if the id does not correspond to a valid message
     */
    public static EntityClass get(int id) {
        if (id >= values().length) throw new IllegalArgumentException("Invalid entityclass identifier " + id);
        else return values()[id];
    }

    /**
     * calls the constructor of the represented class
     * @param game the place to dump newly created entities or particles
     * @param input in case of player: this will be the Controller for the plane. Otherwise it will be ignored.
     * @param position the position of spawn
     * @param rotation the rotation of the object upon spawning
     * @param velocity the initial velocity of the spawned object
     * @return an implementation of the class represented by this enum
     */
    public MovingEntity construct(int id, EntityReceiver game, Controller input, PosVector position, Quaternionf rotation, DirVector velocity){
        switch (this) {
            case BASIC_JET:
                BasicJet jet = new BasicJet(id, input, game.getTimer(), game);
                jet.set(position, velocity, rotation);
                return jet;
            case SIMPLE_BULLET:
                return new SimpleBullet(position, velocity, rotation, game.getTimer(), game, id);
            case FALLING_CUBE:
                return new FallingCube(id, position, velocity, rotation, game.getTimer(), game);
            default:
                Toolbox.printError("Construction of entity class " + this + " is not defined!");
                return null;
        }
    }

    /**
     * @return the enum value corresponding to the given entity, or null if there is none
     */
    public static EntityClass get(MovingEntity type){
        if (type instanceof FallingCube) {
            return FALLING_CUBE;
        } else if (type instanceof BasicJet) {
            return BASIC_JET;
        } else if (type instanceof SimpleBullet) {
            return SIMPLE_BULLET;
        } else {
            return null;
        }
    }
}
