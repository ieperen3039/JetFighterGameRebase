package nl.NG.Jetfightergame.EntityGeneral;

import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
import nl.NG.Jetfightergame.Tools.DataIO;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.joml.Quaternionf;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Geert van Ieperen. Created on 25-7-2018.
 */
public class EntityState {
    private final PosVector firstPos;
    private final PosVector secondPos;
    private final Quaternionf firstRot;
    private final Quaternionf secondRot;
    private final DirVector firstVel;
    private final DirVector secondVel;

    public EntityState() {
        PosVector pos = new PosVector();
        this.firstPos = pos;
        this.secondPos = pos;
        Quaternionf rot = new Quaternionf();
        this.firstRot = rot;
        this.secondRot = rot;
        DirVector vel = new DirVector();
        this.firstVel = vel;
        this.secondVel = vel;
    }

    public EntityState(PosVector position, DirVector direction, DirVector firstVel) {
        this(position, Toolbox.xTo(direction), firstVel);
    }

    public EntityState(PosVector firstPos, PosVector secondPos, Quaternionf firstRot, Quaternionf secondRot, DirVector firstVel, DirVector secondVel) {
        this.firstPos = firstPos;
        this.secondPos = secondPos;
        this.firstRot = firstRot;
        this.secondRot = secondRot;
        this.firstVel = firstVel;
        this.secondVel = secondVel;
    }

    public EntityState(PosVector position, Quaternionf rotation, DirVector firstVel) {
        this(position, position, rotation, rotation, firstVel, firstVel);
    }

    public PosVector position(float timeFraction) {
        if (timeFraction == 0) return new PosVector(firstPos);
        else return firstPos.interpolateTo(secondPos, timeFraction);
    }

    public Quaternionf rotation(float timeFraction) {
        if (timeFraction == 0) return new Quaternionf(firstRot);
        else return firstRot.nlerp(secondRot, timeFraction);
    }

    public DirVector velocity(float timeFraction) {
        if (timeFraction == 0) return new DirVector(firstVel);
        else return firstVel.interpolateTo(secondVel, timeFraction);
    }

    public EntityState add(Vector displacement) {
        return new EntityState(
                firstPos.add(displacement, new PosVector()),
                secondPos.add(displacement, new PosVector()),
                firstRot, secondRot,
                firstVel, secondVel
        );
    }

    /**
     * adds a displacement relative to the rotation and returns the result. This object is not changed.
     * @param displacement an offset in world-space
     * @return the new state.
     */
    public EntityState addRelative(DirVector displacement) {
        PosVector newFirst = addRelativeToPosition(firstPos, displacement, firstRot);
        PosVector newSecond = addRelativeToPosition(secondPos, displacement, secondRot);
        return new EntityState(newFirst, newSecond, firstRot, secondRot, firstVel, secondVel);
    }

    private PosVector addRelativeToPosition(PosVector position, DirVector displacement, Quaternionf rotation) {
        ShadowMatrix sm = new ShadowMatrix();
        sm.translate(position);
        sm.rotate(rotation);
        DirVector secondRel = sm.getDirection(displacement);
        return position.add(secondRel, new PosVector());
    }

    public void writeToStream(DataOutput out) throws IOException {
        DataIO.writeVector(out, firstPos);
        DataIO.writeVector(out, secondPos);
        DataIO.writeQuaternion(out, firstRot);
        DataIO.writeQuaternion(out, secondRot);
        DataIO.writeVector(out, firstVel);
        DataIO.writeVector(out, secondVel);
    }

    public static EntityState readFromStream(DataInput in) throws IOException {
        return new EntityState(
                DataIO.readPosVector(in),
                DataIO.readPosVector(in),
                DataIO.readQuaternion(in),
                DataIO.readQuaternion(in),
                DataIO.readDirVector(in),
                DataIO.readDirVector(in)
        );
    }
}
