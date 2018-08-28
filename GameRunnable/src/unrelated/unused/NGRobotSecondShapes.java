package unrelated.unused;

import nl.NG.Jetfightergame.Assets.Shapes.Direction;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Rendering.MatrixStack.MatrixStack;
import nl.NG.Jetfightergame.ShapeCreation.CustomShape;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;

import java.util.List;

/**
 * @deprecated Invoking this will probably crash the game
 * Created by Geert van Ieperen on 2-3-2017.
 */
@SuppressWarnings("SuspiciousNameCombination")
public class NGRobotSecondShapes {

    public static final Shape head = makeHead();
    public static final Shape innerLeg = makeLeg1();
    public static final Shape outerLeg = makeLeg2();
    public static final Shape foot = makeFoot();
    public static final Shape ear = makeEar();
    public static final Shape torso = makeTorso();
    public static final Shape neck = makeNeck();
    public static final Shape pistonBase = makePistonBase();
    public static final Shape pistonHead = makePistonHead();
    private static final float leg1Width = 1.0f;
    private static final float leg1Length = 8.0f;
    private static final float leg1Bulb = 0.5f;
    private static final float leg2Width = 1.25f;
    private static final float leg2Length = 8.0f;
    private static final float leg2LeftRightSpace = (leg2Width - leg1Width) / 2;
    private static final float leg2LeftRightFraction = 10f;
    private static final float torsoCoverThick = 0.25f;
    private static final float torsoShoulderWingThick = 0.25f;
    private static final float torsoBottomStretch = 7.0f;
    private static final float legDistance = 2.0f;
    private static final float neckHeight = 1.5f;

    /**
     * a void method that allows pre-initialisation
     */
    public static void initAll(){}

    public static void headToRightEar(MatrixStack ms) {
        mirrorToY(ms);
        headToLeftEar(ms);
    }

    public static void headToLeftEar(MatrixStack ms) {
        ms.translate(0.0f, 1.0f, 3.2f);
    }

    private static void mirrorToY(MatrixStack ms) {
        ms.scale(1, -1, 1);
    }

    /**
     * translates and turns to previous shoulder, brings y-axis forward
     */
    public static void torsoToLeftShoulder(MatrixStack gl) {
        gl.translate(-1.0f, 5.0f, 3.5f);
        gl.rotate(90, 0, 0, 1);
        gl.rotate(180, 1, 0, 0);
    }

    /**
     * translates and turns to previous shoulder, brings y-axis forward
     */
    public static void torsoToRightShoulder(MatrixStack gl) {
        mirrorToY(gl);
        torsoToLeftShoulder(gl);
    }

    public static void torsoToLeftLeg(MatrixStack gl) {
        gl.translate(0, legDistance, -torsoBottomStretch);
        gl.rotate(180, 1, 0, 0);
    }

    public static void torsoToRightLeg(MatrixStack gl) {
        mirrorToY(gl);
        torsoToLeftLeg(gl);
    }

    public static void torsoToNeck(MatrixStack gl) {
        gl.translate(-0.5f, 0f, 5.0f);
    }

    public static void innerLegTranslation(MatrixStack gl) {
        gl.translate(-0.5f, 0, leg1Length);
    }

    public static void outerLegTranslation(MatrixStack gl) {
        gl.translate(0, 0, leg2Length);
    }

    public static void neckTranslation(MatrixStack gl) {
        gl.translate(0, 0, neckHeight);
    }

    private static Shape makeHead() {
        CustomShape frame = new CustomShape(PosVector.zeroVector());

        PosVector A = new PosVector(1.5f, 2.0f, 0.0f);
        PosVector B = new PosVector(5.5f, 1.5f, 0.5f);
        PosVector C = new PosVector(5.5f, 1.5f, 2.5f);
        PosVector D = new PosVector(0.5f, 2.0f, 4.0f);
        PosVector E = new PosVector(2.5f, 2.0f, 3.0f);
        PosVector F = new PosVector(2.5f, 2.0f, 1.0f);
        PosVector G = new PosVector(1.5f, 2.0f, 0.0f);

        frame.addMirrorQuad(A, B, C, D);
        frame.addMirrorQuad(A, D, E, F);
        frame.addMirrorTriangle(A, F, G);

        frame.addQuad(A, B);
        frame.addQuad(C, D);
        frame.addQuad(D, E);
        frame.addQuad(E, F);
        frame.addQuad(F, G);
        frame.addQuad(G, A);

        PosVector headMiddle = B.middleTo(C).middleTo(A.middleTo(D));
        PosVector noseMiddle = new PosVector();
        headMiddle.interpolateTo(B.middleTo(C), 1.2f);

        Pair<List<PosVector>, List<PosVector>> noseStrip = frame.addBezierStrip(B, noseMiddle, C, 10);
        frame.addPlaneToBezierStrip(B, noseStrip, true);
        frame.addPlaneToBezierStrip(B.mirrorY(new PosVector()), noseStrip, false);

//        try {
//            frame.writeOBJFile("head");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return frame.wrapUp(GeneralShapes.RENDER_ENABLED);
    }

    private static Shape makeLeg1() {
        CustomShape frame = new CustomShape(PosVector.zeroVector());

        PosVector A = new PosVector(1.0f, leg1Width, 1.0f);
        PosVector B = new PosVector(1.0f, (float) leg1Width, (float) leg1Length);
        PosVector C = new PosVector(0.5f, (float) leg1Width, (float) leg1Length);
        PosVector D = new PosVector((float) (0.5 + leg1Bulb), (float) leg1Width, 1.5f);
        PosVector E = new PosVector(0.5f, (float) leg1Width, 1.0f);
        PosVector F = new PosVector(0.25f, (float) leg1Width, 0.5f);

        frame.addMirrorQuad(A, B, C, D);
        frame.addMirrorQuad(D, E, F, A);

        frame.addQuad(A, B);
        frame.addQuad(B, C);
        frame.addQuad(C, D);
        frame.addQuad(D, E);
        frame.addQuad(E, F);
        frame.addQuad(F, A);

        return frame.wrapUp(GeneralShapes.RENDER_ENABLED);
    }

    private static Shape makeLeg2() {
        CustomShape frame = new CustomShape(PosVector.zeroVector());

        // outside
        PosVector A = new PosVector(1.0f, (float) leg2Width, 0.5f);
        PosVector B = new PosVector(1.0f, (float) leg2Width, (float) leg2Length);
        PosVector C = new PosVector(1.5f, (float) leg2Width, (float) leg2Length);
        PosVector D = new PosVector(1.5f, (float) leg2Width, (float) -2);

        frame.addMirrorQuad(A, B, C, D);
        frame.addQuad(A, B);
        frame.addQuad(B, C);
        frame.addQuad(C, D);

        // hole to contain upper arm part
        DirVector AtoD = A.to(D, new DirVector()).toDirVector();
        PosVector Ap = A.add(new PosVector(AtoD.x() / leg2LeftRightFraction, -leg2LeftRightSpace, AtoD.z() / leg2LeftRightFraction), new PosVector());
        PosVector Dp = D.add(new PosVector(-AtoD.x() / leg2LeftRightFraction, -leg2LeftRightSpace, -AtoD.z() / leg2LeftRightFraction), new PosVector());
        PosVector inner = new PosVector(Dp.x(), Dp.y(), Ap.z());

        frame.addStrip(
                A, Ap,
                D, Dp,
                D.mirrorY(new PosVector()), Dp.mirrorY(new PosVector()),
                A.mirrorY(new PosVector()), Ap.mirrorY(new PosVector()),
                A, Ap
        );
        frame.addQuad(Ap, inner);
        frame.addQuad(inner, Dp);
        frame.addMirrorTriangle(Ap, inner, Dp);

        return frame.wrapUp(GeneralShapes.RENDER_ENABLED);
    }

    private static Shape makeFoot() {
        CustomShape foot = new CustomShape(new PosVector(0, 0, 1));

        PosVector A = new PosVector(2.0f, 2.0f, 3.0f);
        PosVector B = new PosVector(4.0f, 2.0f, 3.0f);
        PosVector C = new PosVector(4.75f, 1.25f, 3.0f);
        PosVector D = new PosVector(5.0f, 0.0f, 3.0f);
        PosVector E = new PosVector(5.0f, 0.0f, 1.5f);

        PosVector F = new PosVector(4.75f, 1.25f, 1.5f);
        PosVector G = new PosVector(4.0f, 2.0f, 1.5f);
        PosVector H = new PosVector(1.0f, 1.5f, 0.0f);
        PosVector I = new PosVector(1.0f, 1.5f, 0.0f);
        PosVector J = new PosVector(2.0f, 2.0f, 1.5f);

        foot.addMirrorQuad(A, B, G, J);
        foot.addMirrorQuad(G, H, I, J);
        foot.addMirrorQuad(B, C, F, G);
        foot.addMirrorQuad(C, D, E, F);
        foot.addMirrorTriangle(F, G, H);
        foot.addMirrorTriangle(E, F, H);

        foot.addQuad(H, I);
        foot.addTriangle(E, H, H.mirrorY(new PosVector()));
        foot.addQuad(I, J);
        foot.addQuad(A, J);
        foot.addQuad(A, B);
        foot.addQuad(B, C);
        foot.addTriangle(C, C.mirrorY(new PosVector()), D);

        return foot.wrapUp(GeneralShapes.RENDER_ENABLED);
    }

    private static Shape makeEar() {
        CustomShape ear = new CustomShape(PosVector.zeroVector());

        PosVector Side = new PosVector(0, 1, 0);
        PosVector Up = new PosVector(0, 0, 2);
        PosVector Back = new PosVector(1.5f, 0, 0);

        ear.addMirrorTriangle(Side, Up, Back);

        return ear.wrapUp(GeneralShapes.RENDER_ENABLED);
    }

    private static Shape makeTorso() {
        CustomShape torso = new CustomShape(PosVector.zeroVector());

        PosVector D = new PosVector(1.0f, 4.0f, 5.0f);
        PosVector F = new PosVector(1.0f, 4.0f, 1.0f);
        PosVector G = new PosVector(1.0f, 3.5f, 2.0f);
        PosVector R = new PosVector(2.0f, 6.0f, 5.0f);
        PosVector S = new PosVector(3.0f, 5.0f, 3.0f);
        PosVector T = new PosVector(3.0f, 4.0f, 1.0f);
        PosVector U = new PosVector(2.0f, 3.5f, 2.0f);

        // bottom
        PosVector H = new PosVector(1.0f, 3.5f, (float) -torsoBottomStretch);
        PosVector I = new PosVector(3.0f, 2.0f, (float) -torsoBottomStretch);
        PosVector V = new PosVector(2.0f, 3.5f, (float) -torsoBottomStretch);
        PosVector W = new PosVector(3.0f, 1.0f, (float) (-1 - torsoBottomStretch));

        PosVector A = new PosVector(3.0f, 2.0f, 3.0f);
        PosVector B = new PosVector(3.0f, 2.5f, 0.5f);
        PosVector J = new PosVector(1.0f, 2.0f, 5.0f);
        PosVector P = new PosVector(3.0f, 3.0f, 3.0f);
        PosVector O = new PosVector(2.0f, 4.0f, 5.0f);
        PosVector K = new PosVector((float) (1 + (6.0 / 5.5)), 1.25f, 2.0f);
        PosVector Q = new PosVector(3.0f, 1.0f, 1.0f);
        PosVector Y = new PosVector(3.0f, 1.0f, 3.0f);
        PosVector Z = new PosVector(2.0f, 2.0f, 5.0f);

        // cover
        PosVector CA = A.add(Direction.FORWARD.vector(torsoCoverThick), new PosVector());
        PosVector CB = B.add(Direction.UPFORWARD.vector(torsoCoverThick), new PosVector());
        PosVector CD = D.add(Direction.UPFORWARD.vector(torsoCoverThick), new PosVector());
        PosVector CJ = J.add(Direction.UPFORWARD.vector(torsoCoverThick), new PosVector());
        PosVector CP = P.add(Direction.UPBACK.vector(torsoCoverThick), new PosVector());
        PosVector CO = O.add(Direction.UPBACK.vector(torsoCoverThick), new PosVector());
        PosVector CK = K.add(Direction.UPFORWARD.vector(torsoCoverThick), new PosVector());
        PosVector CQ = Q.add(Direction.BACK.vector(torsoCoverThick), new PosVector());
        PosVector CY = Y.add(Direction.UPBACK.vector(torsoCoverThick), new PosVector());
        PosVector CZ = Z.add(Direction.UPBACK.vector(torsoCoverThick), new PosVector());

        // flanks
        torso.addQuad(A, I);
        torso.addMirrorQuad(A, G, H, I);
        torso.addMirrorTriangle(A, G, B);
        torso.addMirrorTriangle(G, B, F);
        torso.addMirrorTriangle(B, F, D);
        torso.addMirrorTriangle(D, O, R);
        torso.addMirrorQuad(R, O, P, S);
        torso.addMirrorQuad(P, S, T, Q);
        torso.addMirrorTriangle(T, Q, U);
        torso.addMirrorQuad(Q, U, V, W);
        torso.addQuad(Q, W);

        // outer border of cover
        // this could have been a strip (it is basically a strip)
        torso.addQuad(A, CA);
        torso.addMirrorQuad(A, B, CB, CA);
        torso.addMirrorQuad(B, D, CD, CB);
        torso.addMirrorQuad(D, O, CO, CD);
        torso.addMirrorQuad(O, P, CP, CO);
        torso.addMirrorQuad(P, Q, CQ, CP);
        torso.addQuad(Q, CQ);

        // inner border of cover
        torso.addQuad(K, CK);
        torso.addMirrorQuad(K, J, CJ, CK);
        torso.addMirrorQuad(J, Z, CZ, CJ);
        torso.addMirrorQuad(Z, Y, CY, CZ);
        torso.addQuad(Y, CY);

        // main plates of cover
        torso.addQuad(CA, CB);
        torso.addQuad(CB, CK);
        torso.addMirrorQuad(CB, CK, CJ, CD);
        torso.addMirrorQuad(CJ, CD, CO, CZ);
        torso.addMirrorQuad(CO, CZ, CY, CP);
        torso.addQuad(CP, CQ);

        // shoulder
        PosVector SSD = new PosVector(1.0f, 4.0f, (float) (5.0 - torsoShoulderWingThick));
        PosVector SSR = R.add(Direction.DOWNFORWARD.vector(torsoShoulderWingThick), new PosVector());
        PosVector SSS = S.add(Direction.FORWARD.vector(torsoShoulderWingThick), new PosVector());
        PosVector SST = T.add(Direction.FORWARD.vector(torsoShoulderWingThick), new PosVector());
        PosVector SSO = O.add(Direction.DOWNFORWARD.vector(torsoShoulderWingThick), new PosVector());
        PosVector SSSubS = S.middleTo(P).add(Direction.FORWARD.vector(torsoShoulderWingThick), new PosVector());

        // sides
        torso.addMirrorQuad(G, H, V, U);
        torso.addMirrorQuad(F, G, U, T);
        torso.addMirrorQuad(SST, SSO, SSD, F);
        torso.addMirrorTriangle(SST, SSO, SSSubS);

        // shoulder covers
        torso.addMirrorQuad(T, S, SSS, SST);
        torso.addMirrorQuad(S, R, SSR, SSS);
        torso.addMirrorQuad(R, D, SSD, SSR);
        torso.addMirrorTriangle(SST, SSS, SSSubS);
        torso.addMirrorQuad(SSSubS, SSO, SSR, SSS);
        torso.addMirrorTriangle(SSR, SSO, SSD);

        // board
        torso.addQuad(B, D);
        torso.addQuad(D, O);
        torso.addQuad(O, Q);

        // bottom
        torso.addQuad(I, H);
        torso.addQuad(H, V);
        torso.addQuad(V, W);

        return torso.wrapUp(GeneralShapes.RENDER_ENABLED);
    }

    private static Shape makeNeck() {
        return GeneralShapes.CUBE;
    }

    private static Shape makePistonBase() {
        return GeneralShapes.CUBE;
    }

    private static Shape makePistonHead() {
        return GeneralShapes.CUBE;
    }
}
