package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Identity;
import nl.NG.Jetfightergame.ServerNetwork.EntityClass;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

/**
 * a description of a moving entity. aka Pre-Entity
 */
public class Prentity {
    public final EntityClass type;
    public final PosVector position;
    public final Quaternionf rotation;
    public final DirVector velocity;

    public Prentity(EntityClass type, PosVector position, Quaternionf rotation, DirVector velocity) {
        this.type = type;
        this.position = position;
        this.rotation = rotation;
        this.velocity = velocity;
    }

    public Prentity(EntityClass type, MovingEntity.State state) {
        this(type, state.position(0), state.rotation(0), state.velocity());
    }

    public MovingEntity construct(SpawnReceiver game, Controller input) {
        return type.construct(Identity.next(), game, input, position, rotation, velocity);
    }
}
