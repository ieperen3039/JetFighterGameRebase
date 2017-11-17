package nl.NG.Jetfightergame.Shaders;

/**
 * Materials that can be used for the robots.
 */
public enum Material {

    /**
     * Gold material properties.
     */
    GOLD(

            new float[]{0.82f, 0.76f, 0, 1},
            new float[]{0.95f, 0.92f, 0.5f, 1},
            75

    ),

    /**
     * Gray material that shines blueish
     */
    SILVER(

            new float[]{0.75f, 0.75f, 0.75f, 1},
            new float[]{0.90f, 0.90f, 1.0f, 1},
            50

    ),

    /**
     * Orange material properties.
     */
    PLASTIC(

            new float[]{1, 0.71f, 0.14f, 1},
            new float[]{1, 0.71f, 0.51f, 1},
            2

    ),

    /**
     * Rough, brown material
     */
    WOOD(

            new float[]{0.5f, 0.27f, 0.14f, 1},
            new float[]{0.02f, 0.04f, 0.06f, 1},
            1

    ),

    /**
     * standard material for non-metals
     */
    ROUGH(
            new float[]{0.9f, 0.9f, 0.9f, 1},
            new float[]{0.1f, 0.1f, 0.1f, 1},
            1f
    ),

    /**
     * material for glowing materials like light-sources
     */
    GLOWING(
            new float[]{1f, 1f, 1f, 1f},
            new float[]{0f, 0f, 0f, 1f},
            0
    );

    /**
     * The diffuse RGBA reflectance of the material.
     */
    public final float[] diffuse;
    /**
     * The specular RGBA reflectance of the material.
     */
    public final float[] specular;
    /**
     * The specular exponent of the material.
     */
    public final float shininess;

    Material(float[] diffuse, float[] specular, float shininess) {
        this.diffuse = diffuse;
        this.specular = specular;
        this.shininess = shininess;
    }

}
