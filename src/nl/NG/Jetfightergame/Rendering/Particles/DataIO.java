package nl.NG.Jetfightergame.Rendering.Particles;

import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Geert van Ieperen created on 18-5-2018.
 * may be split in two classes extending Datastream
 */
public class DataIO {
    /** writes the given rotation to the given output stream */
    public static void writeQuaternion(DataOutputStream DOS, Quaternionf rot) throws IOException {
        DOS.writeFloat(rot.x);
        DOS.writeFloat(rot.y);
        DOS.writeFloat(rot.z);
        DOS.writeFloat(rot.w);
    }

    /** @see #writeQuaternion(DataOutputStream, Quaternionf) */
    public static Quaternionf readQuaternion(DataInputStream DIS) throws IOException {
        return new Quaternionf(DIS.readFloat(), DIS.readFloat(), DIS.readFloat(), DIS.readFloat());
    }

    /** writes the given vector to the given output stream */
    public static void writeVector(DataOutputStream DOS, Vector3f v) throws IOException {
        DOS.writeFloat(v.x);
        DOS.writeFloat(v.y);
        DOS.writeFloat(v.z);
    }

    /** @see #writeVector(DataOutputStream, Vector3f)  */
    public static PosVector readPosVector(DataInputStream DIS) throws IOException {
        return new PosVector(DIS.readFloat(), DIS.readFloat(), DIS.readFloat());
    }

    /** reads the next 3 floats on the stream as vector */
    public static DirVector readDirVector(DataInputStream DIS) throws IOException {
        return new DirVector(DIS.readFloat(), DIS.readFloat(), DIS.readFloat());
    }

    /** writes a color to the output stream */
    public static void writeColor(DataOutputStream DOS, Color4f c) throws IOException {
        DOS.writeFloat(c.red);
        DOS.writeFloat(c.green);
        DOS.writeFloat(c.blue);
        DOS.writeFloat(c.alpha);
    }

    /** @see #writeColor(DataOutputStream, Color4f)  */
    public static Color4f readColor(DataInputStream DIS) throws IOException {
        return new Color4f(DIS.readFloat(), DIS.readFloat(), DIS.readFloat(), DIS.readFloat());
    }
}
