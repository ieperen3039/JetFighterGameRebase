package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Geert van Ieperen
 * created on 15-2-2018.
 */
@Ignore
public class MainMenuTest {

    @Test
    public void encapsulatesTest(){
        final boolean hits = Process592.MenuPanel.encapsulates(
                new PosVector(0f, 1f, -1f),
                new PosVector(0f, -1f, -1f),
                new PosVector(0f, -1f, 1f),
                new PosVector(0f, 1f, 1f)
        );
        assert hits;
    }

    @Test // I don't care about the edge cases
    public void encapsulate3DTest(){
        final boolean hits = Process592.MenuPanel.encapsulates(
                new PosVector(7, 2, 1),
                new PosVector(5, 0, 1),
                new PosVector(4, 1, 0),
                new PosVector(2, 1, -2)
        );
        assert hits;
    }

    @Test
    public void encapsulateNot3DTest(){
        final boolean hits = Process592.MenuPanel.encapsulates(
                new PosVector(7, -1, 2),
                new PosVector(5, 1, 2),
                new PosVector(4, 2, 1),
                new PosVector(2, 2, -1)
        );
        assert !hits;
    }

    @Test
    public void encapsulate3DFloatTest(){
        final boolean hits = Process592.MenuPanel.encapsulates(
                new PosVector(38.798f, 20.394f, 8.761f),
                new PosVector(31.343f, -6.698f, -5.075f),
                new PosVector(31.535f, 6.024f, -4.720f),
                new PosVector(38.159f, -22.011f, 7.575f)
        );
        assert hits;
    }
}