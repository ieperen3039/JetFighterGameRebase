package nl.NG.Jetfightergame.Assets.Shapes;

import nl.NG.Jetfightergame.ShapeCreation.CustomShape;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.PairList;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

/**
 * shapes for jets. The jets are loaded in GeneralShapes
 * @author Geert van Ieperen. Created on 11-8-2018.
 */
public final class CustomJetShapes {
    public static Shape BASIC;
    public static Shape SPITZ;
    public static Shape NIGHT_HAWK;

    public static PairList<PosVector, PosVector> spitzBoosters;
    public static PairList<PosVector, PosVector> nightHawkBoosters;

    public static Shape makeSpitzPlane(boolean doLoadMesh) {
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

    public static Shape makeNightHawk(boolean doLoadMesh) {
        CustomShape frame = new CustomShape();

        PosVector A = new PosVector(7.5f, 0, -1);
        PosVector B = new PosVector(4.5f, 0, 0);
        PosVector C = new PosVector(-3.5f, 5, -1);
        PosVector D = new PosVector(-4.5f, 5, 0);
        PosVector E = new PosVector(2.5f, 0, 2);
        PosVector F = new PosVector(1.5f, 2, 0);
        PosVector G = new PosVector(-4.5f, 0, 2);
        PosVector H = new PosVector(-2.5f, 0, 0);
        PosVector I = new PosVector(-1.5f, 0, -1);
        float ItoLFraction = 2 / C.y;
        PosVector L = I.interpolateTo(C, ItoLFraction);
        PosVector K = H.interpolateTo(D, ItoLFraction);
        PosVector M = new PosVector(-5.5f, 2, -1);
        PosVector J = G.interpolateTo(K, 1.5f);

        frame.addMirrorTriangle(A, B, C);
        frame.addMirrorTriangle(B, C, D);
        frame.addMirrorTriangle(B, E, F);
        frame.addMirrorTriangle(F, D, K);
        frame.addMirrorTriangle(A, C, I);

        frame.addMirrorQuad(E, F, K, G);
        frame.addMirrorQuad(C, D, H, I);
        frame.addMirrorTriangle(H, K, G);
        frame.addMirrorTriangle(K, L, M, new DirVector(0, -1, 0));
        frame.addMirrorTriangle(L, J, M);
        frame.addMirrorTriangle(K, J, M);

        nightHawkBoosters = new PairList<>(2);
        PosVector DCMid = D.middleTo(C);
        PosVector HIMid = H.middleTo(I);
        PosVector DCMid2 = DCMid.mirrorY(new PosVector());
        nightHawkBoosters.add(HIMid, DCMid);
        nightHawkBoosters.add(HIMid, DCMid2);

        return frame.wrapUp(doLoadMesh);
    }
}