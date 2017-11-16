package nl.NG.Jetfightergame.GameObjects.Structures;

import nl.NG.Jetfightergame.Vectors.DirVector;
import nl.NG.Jetfightergame.Vectors.PosVector;
import nl.NG.Jetfightergame.Vectors.Vector;

/**
 * @author Geert van Ieperen
 *         created on 11-11-2017.
 *         wrapper for Vector class
 */
public class Vector3f {
    final float x;
    final float y;
    final float z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f(double x, double y, double z){
        this.x = (float) x;
        this.y = (float) y;
        this.z = (float) z;
    }

    public Vector3f(Vector v){
        this(v.x(), v.y(), v.z());
    }

    public PosVector toPosVector(){
        return new PosVector(x, y, z);
    }

    public DirVector toDirVector(){
        return new DirVector(x, y, z);
    }
}
