package nl.NG.Jetfightergame.Assets.Shapes;

import nl.NG.Jetfightergame.ShapeCreation.BasicShape;
import nl.NG.Jetfightergame.ShapeCreation.CustomShape;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.PairList;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * @author Geert van Ieperen. Created on 11-8-2018.
 */
public final class CustomJetShapes {
    private static boolean isLoaded;
    public static Shape BASIC;
    public static Shape SPITZ;

    public static PairList<PosVector, PosVector> spitzBoosters;

    public static void init(boolean doLoadMesh) {
        if (isLoaded) {
            Logger.ERROR.print("Tried loading shapes while they where already loaded");
            return;
        }
        isLoaded = true;

        BASIC = new BasicShape("ConceptBlueprint.obj", doLoadMesh);
        SPITZ = makeSpitzPlane(doLoadMesh);
    }

    private static Shape makeSpitzPlane(boolean doLoadMesh) {
        CustomShape frame = new CustomShape();

        PosVector A = new PosVector(6, 0, -0.5f);
        PosVector B = new PosVector(5, 0, 0);
        PosVector C = new PosVector(4, 1, -0.5f);
        PosVector D = new PosVector(3, 1, 0);
        PosVector E = new PosVector(7, 4, -1);
        PosVector F = new PosVector(0, 2, -0.5f);
        PosVector G = new PosVector(-4, 2, 3);
        PosVector H = new PosVector(-2, 0, 1);
        PosVector I = new PosVector(-3, 4, -2);
        PosVector J = new PosVector(-2, 0.5f, 0);
        PosVector J2 = J.mirrorY(new PosVector());
        PosVector K = new PosVector(3, 0, 1);
        PosVector L = new PosVector(3, 0, -1);
        PosVector M = new PosVector(-1, 0, -1);

        frame.addMirrorQuad(A, B, D, C);
        frame.addTriangle(B, D, D.mirrorY(new PosVector()));
        frame.addTriangle(K, D, D.mirrorY(new PosVector()));

        frame.addMirrorTriangle(A, C, L);
        frame.addMirrorTriangle(C, L, F);
        frame.addMirrorTriangle(L, F, M);
        frame.addMirrorTriangle(M, F, J);
        frame.addMirrorTriangle(M, J, J2);

        frame.addMirrorTriangle(C, D, E);
        frame.addMirrorTriangle(E, D, F);
        frame.addMirrorTriangle(C, E, F);

        frame.addMirrorTriangle(D, F, H);
        frame.addMirrorTriangle(K, D, H);

        frame.addMirrorTriangle(G, H, F);
        frame.addMirrorTriangle(G, F, J);
        frame.addMirrorTriangle(G, H, J);

        frame.addMirrorTriangle(H, I, J);
        frame.addMirrorTriangle(I, J, F);
        frame.addMirrorTriangle(H, I, F);
        frame.addQuad(F, J);
        frame.addTriangle(H, J, J2);

        spitzBoosters = new PairList<>(3);
        spitzBoosters.add(H, J.middleTo(J2));
        spitzBoosters.add(J, H.middleTo(J2));
        spitzBoosters.add(J2, H.middleTo(J));

        return frame.wrapUp(doLoadMesh);
    }

}