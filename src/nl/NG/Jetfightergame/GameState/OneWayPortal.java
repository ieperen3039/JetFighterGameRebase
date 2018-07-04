package nl.NG.Jetfightergame.GameState;

import nl.NG.Jetfightergame.AbstractEntities.Hitbox.Collision;
import nl.NG.Jetfightergame.AbstractEntities.MovingEntity;
import nl.NG.Jetfightergame.AbstractEntities.StaticEntity;
import nl.NG.Jetfightergame.AbstractEntities.Touchable;
import nl.NG.Jetfightergame.Assets.Shapes.GeneralShapes;
import nl.NG.Jetfightergame.Rendering.MatrixStack.ShadowMatrix;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import nl.NG.Jetfightergame.Tools.Vectors.Vector;
import org.joml.Quaternionf;

/**
 * @author Geert van Ieperen. Created on 3-7-2018.
 */
public class OneWayPortal {
    private GameState worldFrom;
    private StaticEntity tunnelFrom;

    private GameState worldTo;
    private StaticEntity tunnelTo;

    public OneWayPortal(
            GameState worldFrom, PosVector positionFrom, DirVector directionFrom,
            GameState worldTo, PosVector positionTo, DirVector directionTo,
            float radius
    ) {
        tunnelTo = new TargetPortal(positionTo, directionTo, radius);
        tunnelFrom = new SourcePortal(positionFrom, directionFrom, radius);
        this.worldFrom = worldFrom;
        this.worldTo = worldTo;
    }

    public Touchable getEntrance() {
        return tunnelFrom;
    }

    public Touchable getExit() {
        return tunnelTo;
    }

    /**
     * teleports entities to the given targetPortal
     */
    private class SourcePortal extends StaticEntity {
        private SourcePortal(Vector position, DirVector direction, float radius) {
            super(GeneralShapes.CHECKPOINTRING, ClientSettings.PORTAL_MATERIAL, Color4f.ORANGE, position, radius, Toolbox.xTo(direction));
        }

        @Override
        public void acceptCollision(Collision cause) {
            MovingEntity source = cause.source();
            worldFrom.removeEntity(source);

            // teleport entity to new location, while switching worlds
            PosVector newPosition = cause.getShapeLocalHitPos();
            ShadowMatrix ms = new ShadowMatrix();
            tunnelTo.toLocalSpace(ms, () -> newPosition.set(ms.getPosition(newPosition)));
            Quaternionf relativeRotation = tunnelTo.getRotation().difference(this.getRotation());
            Quaternionf newRotation = source.getRotation().add(relativeRotation);
            DirVector newVelocity = source.getVelocity();
            relativeRotation.transform(newVelocity);
            source.set(newPosition, newVelocity, newRotation);

            worldTo.addEntity(source);
        }
    }

    private class TargetPortal extends StaticEntity {
        private TargetPortal(Vector position, DirVector direction, float radius) {
            super(GeneralShapes.CHECKPOINTRING, ClientSettings.PORTAL_MATERIAL, Color4f.BLUE, position, radius, Toolbox.xTo(direction.negate(new DirVector())));
        }
    }
}
