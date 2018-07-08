package nl.NG.Jetfightergame.ServerNetwork;

import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.Assets.FighterJets.BasicJet;
import nl.NG.Jetfightergame.Assets.GeneralEntities.FallingCube;
import nl.NG.Jetfightergame.Assets.Weapons.SimpleBullet;
import nl.NG.Jetfightergame.Assets.Weapons.SimpleRocket;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import static nl.NG.Jetfightergame.Settings.ServerSettings.*;

/**
 * @author Geert van Ieperen created on 10-5-2018.
 */
public enum  EntityClass {
    BASIC_JET, SIMPLE_BULLET, FALLING_CUBE_SMALL, FALLING_CUBE_LARGE, SIMPLE_ROCKET;

    private static final EntityClass[] VALUES = values();

    /**
     * @param id a number n corresponing to an enum ordinal
     * @return the enum e such that {@code e.ordinal() == n}
     * @throws IllegalArgumentException if the id does not correspond to a valid message
     */
    public static EntityClass get(int id) {
        if (id >= VALUES.length) throw new IllegalArgumentException("Invalid entityclass identifier " + id);
        else return VALUES[id];
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
    public MovingEntity construct(int id, SpawnReceiver game, Controller input, PosVector position, Quaternionf rotation, DirVector velocity){
        GameTimer timer = game.getTimer();
        switch (this) {
            case BASIC_JET:
                BasicJet jet = new BasicJet(id, input, timer, game);
                jet.set(position, velocity, rotation);
                return jet;
            case SIMPLE_BULLET:
                return new SimpleBullet(id, position, velocity, rotation, timer, game);
            case SIMPLE_ROCKET:
                return new SimpleRocket(id, position, velocity, rotation, timer, game);
            case FALLING_CUBE_SMALL:
                return new FallingCube(id, Material.SILVER, CUBE_MASS_SMALL, CUBE_SIZE_SMALL, position, velocity, rotation, timer, game);
            case FALLING_CUBE_LARGE:
                return new FallingCube(id, Material.SILVER, CUBE_MASS_LARGE, CUBE_SIZE_LARGE, position, velocity, rotation, timer, game);
            default:
                Logger.printError("Construction of entity class " + this + " is not defined!");
                return null;
        }
    }

    /**
     * @return the enum value corresponding to the given entity, or null if there is none
     * @deprecated an entity should know its own defined type. (not yet implemented) //TODO add this
     */
    public static EntityClass get(MovingEntity type){
        if (type instanceof FallingCube) {
            if (type.getMass() == CUBE_MASS_LARGE){
                return FALLING_CUBE_LARGE;

            } else if (type.getMass() == CUBE_MASS_SMALL) {
                return FALLING_CUBE_SMALL;

            } else {
                return FALLING_CUBE_SMALL;
            }

        } else if (type instanceof BasicJet) {
            return BASIC_JET;

        } else if (type instanceof SimpleBullet) {
            return SIMPLE_BULLET;

        } else {
            return null;
        }
    }

    public static String asString(int id) {
        return id < VALUES.length ? get(id).toString() : id + " (Invalid entity id)";
    }
}
