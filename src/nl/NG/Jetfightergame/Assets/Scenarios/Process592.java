package nl.NG.Jetfightergame.Assets.Scenarios;

import nl.NG.Jetfightergame.AbstractEntities.GameEntity;
import nl.NG.Jetfightergame.Assets.Shapes.MenuPanels;
import nl.NG.Jetfightergame.Controllers.Controller;
import nl.NG.Jetfightergame.Engine.GameState.Environment;
import nl.NG.Jetfightergame.Engine.GameState.EnvironmentManager.Worlds;
import nl.NG.Jetfightergame.Engine.GameTimer;
import nl.NG.Jetfightergame.Engine.StaticTimer;
import nl.NG.Jetfightergame.Player;
import nl.NG.Jetfightergame.Primitives.Particles.Particle;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.ShapeCreation.Shape;
import nl.NG.Jetfightergame.Tools.MatrixStack.GL2;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Consumer;

import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.glDisable;

/**
 * This is a spaceless, timeless dimension, and serves as main menu.
 * @author Geert van Ieperen
 * created on 13-2-2018.
 */
public class Process592 implements Environment {

    private final Player player;
    private final ArrayList<Particle> particles;

    /** how much the player looks up */
    private float phi = 0;
    /** how much the player rolls right */
    private float theta = 0;
    private float phiFactor = 1f;
    private float thetaFactor = 1f;

    private MenuPanel[] currentItems;

    public Process592(Player player, Consumer<Worlds> worldSelector) {
        this.player = player;
        player.jet().set();
        particles = new ArrayList<>();
        currentItems = worldSelectionMenu(worldSelector);
    }

    @Override
    public void updateGameLoop() {
        // read controller input
        final Controller input = player.getInput();

        phi += input.pitch() * phiFactor;
        theta += input.roll() * thetaFactor;
    }

    @Override
    public void setLights(GL2 gl) {
    }

    @Override
    public void drawObjects(GL2 gl) {
        glDisable(GL_CULL_FACE);
        player.jet().draw(gl);
        gl.pushMatrix();
        {
//            gl.rotate(DirVector.xVector(), theta);
//            gl.rotate(DirVector.yVector(), phi);
            // draw the menu panels
            drawPanels(gl, currentItems.length);
            // activate the menu panel if the player clicks
        }
        gl.popMatrix();
    }

    private void drawPanels(GL2 gl, int parts) {
        gl.setMaterial(Material.GLOWING, Color4f.GREEN);
        gl.scale(20);

        final Shape panel = MenuPanels.get(parts);
        float t = (float) ((2 * Math.PI) / parts);

        for (int i = 0; i < parts; i++) {
            gl.translate(0, 0, 0.1f);
            gl.draw(panel);
            gl.translate(0, 0, -0.1f);

            gl.rotate(DirVector.xVector(), t);
        }
    }

    @Override
    public void drawParticles(GL2 gl) {
        particles.forEach(p -> p.draw(gl));
    }

    @Override
    public GameTimer getTimer() {
        return new StaticTimer(1); // Time doesn't exist, but this should prevent exceptions
    }

    @Override
    public void cleanUp() {
        particles.clear();
    }

    @Override
    public void addEntity(GameEntity entity) {
        // maybe a bit pessimistic
        throw new UnsupportedOperationException();
    }

    @Override
    public void addParticles(Collection<Particle> newParticles) {
        particles.addAll(newParticles);
    }

    /**
     * creates menuPanels for selecting worlds
     * @param sceneSelector switches the current world to the given world
     * @return a new menu with all values defined in {@link nl.NG.Jetfightergame.Engine.GameState.EnvironmentManager.Worlds}
     */
    private static MenuPanel[] worldSelectionMenu(Consumer<Worlds> sceneSelector) {
        final Worlds[] allScenes = Worlds.values();
        MenuPanel[] main = new MenuPanel[allScenes.length];

        for (int i = 0; i < allScenes.length; i++) {
            Worlds scene = allScenes[i];
            main[i] = new MenuPanel(() -> sceneSelector.accept(scene));
        }

        return main;
    }

    private static class MenuPanel implements Runnable {
        private final Runnable action;

        private MenuPanel(Runnable action) {
            this.action = action;
        }

        @Override
        public void run() {
            action.run();
        }
    }
}
