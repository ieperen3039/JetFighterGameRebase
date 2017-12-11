package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Controllers.KeyTracker;
import nl.NG.Jetfightergame.Engine.GLMatrix.GL2;
import nl.NG.Jetfightergame.Scenarios.TestLab;
import nl.NG.Jetfightergame.Tools.Toolbox;
import org.junit.Test;

import static org.lwjgl.glfw.GLFW.*;

/**
 * @author Geert van Ieperen
 *         created on 28-11-2017.
 * allows testing the GL object in a setting
 */
public class JetFighterTester {

    private JetFighterGame engine;

    /**
     * openWindow the game by creating a frame based on this engine
     */
    public JetFighterTester() throws Exception {
        engine = new JetFighterGame();
        engine.setGameState(new TestEnvironment());
    }

    @Test
    public void runTest() throws Exception {

    }

    private class TestEnvironment extends GameState {

        private float xOffSet = 0;//debug variable, may be deleted
        private float yOffSet = 0;
        private float zOffSet = 0;

        public TestEnvironment() {
            super();
        }

        /**
         * openWindow the game by creating a frame based on this engine
         */

        @Override
        public void drawObjects(GL2 gl) {

            if (KeyTracker.getInstance().isPressed(GLFW_KEY_1)) {
                xOffSet += 1f / Settings.TARGET_FPS;
            }
            if (KeyTracker.getInstance().isPressed(GLFW_KEY_2)) {
                yOffSet += 1f / Settings.TARGET_FPS;
            }
            if (KeyTracker.getInstance().isPressed(GLFW_KEY_3)) {
                zOffSet += 1f / Settings.TARGET_FPS;
            }

            gl.translate(xOffSet, yOffSet, zOffSet);
            Toolbox.drawAxisFrame(gl);

            super.drawObjects(gl);
        }

        @Override
        protected void buildScene() {
            Toolbox.print("building test scenery");

//            super.buildScene();

            KeyTracker.getInstance().addKey(GLFW_KEY_1);
            KeyTracker.getInstance().addKey(GLFW_KEY_2);
            KeyTracker.getInstance().addKey(GLFW_KEY_3);

//            objects.add(playerJet);
            staticObjects.add(new TestLab(100));
        }
    }
}