package nl.NG.Jetfightergame.GameObjects.Structures;

import nl.NG.Jetfightergame.Vectors.PosVector;

/**
 * @author Geert van Ieperen
 *         created on 7-11-2017.
 */
public class GeneralShapes {

    /** a triangle with vertices [a(1, 0, 0), b(0, 1, 0), c(0, 0, 0)] */
    public static final Shape TRIANGLE = makeTriangle();
    /** a 2*2*2 cube with center on (0, 0, 0) */
    public static final Shape CUBE = makeCube();

    /**
     * a void method that allows pre-initialisation
     */
    public static void initAll(){}

    private static Shape makeTriangle() {
        CustomShape frame = new CustomShape();

        frame.addTriangle(
                new PosVector(1, 0, 0),
                new PosVector(0,  1, 0),
                new PosVector(0, 0, 0)
        );

        return frame.wrapUp();
    }

    private static Shape makeCube() {
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

        return frame.wrapUp();
    }
}
