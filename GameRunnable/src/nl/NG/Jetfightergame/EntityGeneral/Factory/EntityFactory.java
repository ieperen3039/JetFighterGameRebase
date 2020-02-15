package nl.NG.Jetfightergame.EntityGeneral.Factory;

import nl.NG.Jetfightergame.EntityGeneral.EntityMapping;
import nl.NG.Jetfightergame.EntityGeneral.EntityState;
import nl.NG.Jetfightergame.EntityGeneral.MovingEntity;
import nl.NG.Jetfightergame.GameState.SpawnReceiver;
import nl.NG.Jetfightergame.Identity;
import nl.NG.Jetfightergame.Tools.DataIO;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;

import java.io.*;

/**
 * a description of a moving entity. aka Pre-Entity
 */
public abstract class EntityFactory implements Externalizable {
    protected EntityClass type;
    protected int id;
    public PosVector position;
    protected Quaternionf rotation;
    protected DirVector velocity;

    public EntityFactory(EntityClass type, MovingEntity e) {
        this.type = type;

        id = e.idNumber();
        position = e.getPosition();
        rotation = e.getRotation();
        velocity = e.getVelocity();
    }

    /**
     * creates a factory of the given type
     * @param position the position where the object will be located
     * @param rotation the rotation of the object upon spawning
     * @param velocity the initial velocity of the spawned object
     */
    public EntityFactory(EntityClass type, PosVector position, Quaternionf rotation, DirVector velocity) {
        this.type = type;
        this.position = position;
        this.rotation = rotation;
        this.velocity = velocity;
        this.id = Identity.next();
    }

    public EntityFactory(EntityClass type, EntityState state, float fraction) {
        this(type, state.position(fraction), state.rotation(fraction), state.velocity(0));
    }

    /**
     * create a new factory of the given type and give it a new unique id
     * @param type     the type of entity to create a factory of
     * @param state    the position of the new entity
     * @param fraction the faction of the state where this entity is spawned
     * @return a new factory with a unique id and with only the basic parameters set
     */
    public static EntityFactory newFactoryOf(EntityClass type, EntityState state, float fraction) {
        EntityFactory factory = type.getFactory();
        factory.id = Identity.next();
        factory.set(state, fraction);
        return factory;
    }

    /**
     * creates a factory instance as read from the stream
     * @param in the stream writing using {@link #writeFactory(DataOutputStream)}
     * @return the entity read from the stream
     */
    public static EntityFactory readFactory(DataInputStream in) throws IOException {
        EntityClass type = EntityClass.get(in.read());
        EntityFactory p = type.getFactory();
        p.readInternal(in);
        return p;
    }

    /**
     * writes this factory to the given output
     * @param out the stream reading using {@link #readFactory(DataInputStream)}
     */
    public void writeFactory(DataOutputStream out) throws IOException {
        out.write(type.ordinal());
        writeInternal(out);
    }

    protected EntityFactory() {
    }

    public void set(EntityState state, float fraction) {
        position = state.position(fraction);
        rotation = state.rotation(fraction);
        velocity = state.velocity(fraction);
    }

    /**
     * create an entity from this factory
     * @param game     the entity deposit for this instance
     * @param entities
     * @return a new entity
     */
    public abstract MovingEntity construct(SpawnReceiver game, EntityMapping entities);

    protected void writeInternal(DataOutput out) throws IOException {
        // identity
        out.writeInt(id);
        // state
        DataIO.writeVector(out, position);
        DataIO.writeQuaternion(out, rotation);
        DataIO.writeVector(out, velocity);
    }

    protected void readInternal(DataInput in) throws IOException {
        // identity
        id = in.readInt();
        // state
        position = DataIO.readPosVector(in);
        rotation = DataIO.readQuaternion(in);
        velocity = DataIO.readDirVector(in);
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        writeInternal(out);
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException {
        readInternal(in);
    }

    public int getIdentity() {
        return id;
    }

    @Override
    public String toString() {
        return "Factory[" + type + "(" + id + ")]";
    }
}
