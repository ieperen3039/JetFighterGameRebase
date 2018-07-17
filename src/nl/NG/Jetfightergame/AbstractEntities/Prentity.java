package nl.NG.Jetfightergame.AbstractEntities;

import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Identity;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

/**
 * a description of a moving entity. aka Pre-Entity
 */
public class Prentity {
    public final String type;
    public final PosVector position;
    public final Quaternionf rotation;
    public final DirVector velocity;

    public Prentity(String type, PosVector position, Quaternionf rotation, DirVector velocity) {
        this.type = type;
        this.position = position;
        this.rotation = rotation;
        this.velocity = velocity;
    }

    public Prentity(String type, MovingEntity.State state) {
        this(type, state.position(0), state.rotation(0), state.velocity());
    }

    public MovingEntity construct(SpawnReceiver game) {
        int id = Identity.next();
        return MovingEntity.get(type, id, position, rotation, velocity, game);
    }
}
