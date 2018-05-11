package nl.NG.Jetfightergame.Assets.Shapes;

import nl.NG.Jetfightergame.ShapeCreation.CustomShape;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 7-11-2017.
 */
public final class GeneralShapes {

    /** a triangle with vertices [a(1, 0, 0), b(0, 1, 0), c(0, 0, 0)] */
    public static Shape TRIANGLE;

    public static void init(boolean loadMesh){
        TRIANGLE = makeTriangle(loadMesh);
        INVERSE_CUBE = makeInverseCube(0, loadMesh);
        CUBE = makeCube(loadMesh);
    }

    /** a 2*2*2 cube with center on (0, 0, 0) */
    public static Shape CUBE;

    public static Shape INVERSE_CUBE;

    private static Shape makeTriangle(boolean loadMesh) {
        CustomShape frame = new CustomShape(new PosVector(0, 0, -1));

        frame.addTriangle(
                new PosVector(1, 0, 0),
                new PosVector(0, 1, 0),
                new PosVector(0, 0, 0),
                new DirVector(0, 0, 1)
        );

        return frame.wrapUp(loadMesh);
    }

    private static Shape makeCube(boolean loadMesh) {
        CustomShape frame = new CustomShape();

        PosVector PPP = new PosVector(1, 1, 1);
        PosVector PPN = new PosVector(1, 1, -1);
        PosVector NPP = new PosVector(-1, 1, 1);
        PosVector NPN = new PosVector(-1, 1, -1);

        frame.addQuad(PPP, PPN);
        frame.addQuad(PPN, NPN);
        frame.addQuad(NPN, NPP); // -x plane
        frame.addQuad(NPP, PPP);
        frame.addMirrorQuad(PPP, PPN, NPN, NPP);

        return frame.wrapUp(loadMesh);
    }

    /**
     * create a new inverse cube
     * @param splits number of splits on each side.
     * @param loadMesh
     * @return a cube with normals pointing inside, made out of {@code 6 * 2 ^ splits} quads
     */
    public static Shape makeInverseCube(int splits, boolean loadMesh){
        CustomShape frame = new CustomShape();

        PosVector PPP = new PosVector(1, 1, 1);
        PosVector PPN = new PosVector(1, 1, -1);
        PosVector PNP = new PosVector(1, -1, 1);
        PosVector PNN = new PosVector(1, -1, -1);
        PosVector NPP = new PosVector(-1, 1, 1);
        PosVector NPN = new PosVector(-1, 1, -1);
        PosVector NNP = new PosVector(-1, -1, 1);
        PosVector NNN = new PosVector(-1, -1, -1);

        recursiveQuad(frame, NPN, NPP, NNP, NNN, new DirVector(1, 0, 0), splits);
        recursiveQuad(frame, PNP, PNN, NNN, NNP, new DirVector(0, 1, 0), splits);
        recursiveQuad(frame, PPP, PPN, PNN, PNP, new DirVector(-1, 0, 0), splits);
        recursiveQuad(frame, PPN, NPN, NNN, PNN, new DirVector(0, 0, 1), splits);
        recursiveQuad(frame, PPP, PPN, NPN, NPP, new DirVector(0, -1, 0), splits);
        recursiveQuad(frame, NPP, PPP, PNP, NNP, new DirVector(0, 0, -1), splits);

        return frame.wrapUp(loadMesh);
    }

    /**
     * recursively split the given quad, and add all tiny components to frame. This results in {@code 2^splits} quads
     * @param normal the shared normal of the resulting quads
     * @param splits the number of splits to be made, with 0 is no splits
     */
    private static void recursiveQuad(CustomShape frame, PosVector A, PosVector B, PosVector C, PosVector D, DirVector normal, int splits) {
        if (splits == 0) {
            frame.addQuad(A, B, C, D, normal);

        } else {
            PosVector AB = A.middleTo(B);
            PosVector BC = B.middleTo(C);
            PosVector CD = C.middleTo(D);
            PosVector AD = A.middleTo(D);
            PosVector MID = AB.middleTo(CD);

            recursiveQuad(frame, A, AB, MID, AD, normal, splits - 1);
            recursiveQuad(frame, AB, B, BC, MID, normal, splits - 1);
            recursiveQuad(frame, MID, BC, C, CD, normal, splits - 1);
            recursiveQuad(frame, AD, MID, CD, D, normal, splits - 1);
        }
    }
}
