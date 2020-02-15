package nl.NG.Jetfightergame.Tools;

import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author Geert van Ieperen created on 18-5-2018.
 * may be split in two classes extending Datastream
 */
public class DataIO {
    /** writes the given rotation to the given output stream */
    public static void writeQuaternion(DataOutput out, Quaternionf rot) throws IOException {
        out.writeFloat(rot.x);
        out.writeFloat(rot.y);
        out.writeFloat(rot.z);
        out.writeFloat(rot.w);
    }

    /** @see #writeQuaternion(DataOutput, Quaternionf) */
    public static Quaternionf readQuaternion(DataInput in) throws IOException {
        return new Quaternionf(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
    }

    /** writes the given vector to the given output stream */
    public static void writeVector(DataOutput out, Vector3f v) throws IOException {
        out.writeFloat(v.x);
        out.writeFloat(v.y);
        out.writeFloat(v.z);
    }

    /** @see #writeVector(DataOutput, Vector3f) */
    public static PosVector readPosVector(DataInput in) throws IOException {
        return new PosVector(in.readFloat(), in.readFloat(), in.readFloat());
    }

    /** reads the next 3 floats on the stream as vector */
    public static DirVector readDirVector(DataInput in) throws IOException {
        return new DirVector(in.readFloat(), in.readFloat(), in.readFloat());
    }

    /** writes a color to the output stream */
    public static void writeColor(DataOutput out, Color4f c) throws IOException {
        out.writeFloat(c.red);
        out.writeFloat(c.green);
        out.writeFloat(c.blue);
        out.writeFloat(c.alpha);
    }

    /** @see #writeColor(DataOutput, Color4f) */
    public static Color4f readColor(DataInput in) throws IOException {
        return new Color4f(in.readFloat(), in.readFloat(), in.readFloat(), in.readFloat());
    }
}
