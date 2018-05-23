package nl.NG.Jetfightergame.Engine;

import nl.NG.Jetfightergame.Assets.GeneralEntities.FallingCube;
import nl.NG.Jetfightergame.GameState.CollisionDetection;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Tools.DataStructures.Pair;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.DirVector;
import nl.NG.Jetfightergame.Tools.Vectors.PosVector;
import org.joml.Quaternionf;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Geert van Ieperen
 * created on 18-3-2018.
 */
@SuppressWarnings("unchecked")
public class CollisionDetectionTest extends CollisionDetection {
    public CollisionDetectionTest() {
        super(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    }

    @Test
    public void testInsertionSort() {
        int id = 1;
        Pair[] nums = {new Pair(1f, id++), new Pair(2f, id++), new Pair(3f, id++), new Pair(2f, id++), new Pair(2.01f, id++), new Pair(4f, id++)};
        Toolbox.insertionSort(nums, p -> (Float) p.left);
        Toolbox.print(Arrays.asList(nums));
    }

    @Test
    public void testCheckOverlapOneAxis() {

        TestEntity[] nums = {
                new TestEntity(-2f, 3),
                new TestEntity(-1.5f, 1),
                new TestEntity(3, 2),
                new TestEntity(3.5f, 0)
        };

        int[][] matrix = new int[nums.length][nums.length];

        checkOverlap(matrix, nums, CollisionEntity::zLower, CollisionEntity::zUpper);

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + ", ");
            }
            System.out.println();
        }

        // no values for i > j
        for (int i = 0; i < nums.length; i++) {
            for (int j = i; j < nums.length; j++) {
                assert matrix[i][j] == 0: String.format("loose values on [%d, %d]", i, j);
            }
        }

        assert matrix [1][0] == 0: "false positive";
        assert matrix [2][0] == 1: "false negative";
        assert matrix [2][1] == 0: "false positive";
        assert matrix [3][0] == 0: "false positive";
        assert matrix [3][1] == 1: "false negative";
        assert matrix [3][2] == 0: "false positive";
    }

    @Test
    public void testCheckOverlapThreeAxis() {
        TestEntity[] nums = {
                new TestEntity(4, -4, -2f, 2),
                new TestEntity(4, -4, -1.5f, 3),
                new TestEntity(4, 4, 5, 1),
                new TestEntity(-4, 4, 5.5f, 0)
        };

        int[][] matrix = new int[nums.length][nums.length];

        CollisionEntity[] xLowerSorted = nums.clone();
        CollisionEntity[] yLowerSorted = nums.clone();
        Toolbox.insertionSort(xLowerSorted, CollisionEntity::xLower);
        Toolbox.insertionSort(yLowerSorted, CollisionEntity::yLower);

        checkOverlap(matrix, xLowerSorted, CollisionEntity::xLower, CollisionEntity::xUpper);
        checkOverlap(matrix, yLowerSorted, CollisionEntity::yLower, CollisionEntity::yUpper);
        checkOverlap(matrix, nums, CollisionEntity::zLower, CollisionEntity::zUpper);

        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + ", ");
            }
            System.out.println();
        }

        // no values for i > j
        for (int i = 0; i < nums.length; i++) {
            for (int j = i; j < nums.length; j++) {
                assert matrix[i][j] == 0: String.format("loose values on [%d, %d]", i, j);
            }
        }

        assert matrix [1][0] == 2: "[1][0] => " + matrix[1][0];
        assert matrix [2][0] == 0: "[2][0] => " + matrix[2][0];
        assert matrix [2][1] == 1: "[2][1] => " + matrix[2][1];
        assert matrix [3][0] == 0: "[3][0] => " + matrix[3][0];
        assert matrix [3][1] == 1: "[3][1] => " + matrix[3][1];
        assert matrix [3][2] == 3: "[3][2] => " + matrix[3][2];
    }

    private class TestEntity extends CollisionEntity {
        public TestEntity(float zPos, int id) {
            super(new FallingCube(id, Material.SILVER, 10f, 1f, new PosVector(0, 0, zPos), DirVector.zeroVector(), new Quaternionf(), new GameTimer(), null));
            setId(id);
        }

        public TestEntity(float x, float y, float z, int id) {
            super(new FallingCube(id, Material.SILVER, 10f, 1f, new PosVector(x, y, z), DirVector.zeroVector(), new Quaternionf(), new GameTimer(), null));
            setId(id);
        }
    }
}