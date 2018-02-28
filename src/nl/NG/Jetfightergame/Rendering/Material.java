package nl.NG.Jetfightergame.Rendering;

import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

/**
 * Materials that can be used for the robots.
 */
public enum Material {

    /**
     * standard material for non-metals
     */
    ROUGH(
            new float[]{0.9f, 0.9f, 0.9f, 1},
            new float[]{0.1f, 0.1f, 0.1f, 0.1f},
            new float[]{0.5f, 0.5f, 0.5f, 1},
            1f
    ),

    /**
     * material for glowing materials like light-sources
     */
    GLOWING(
            new float[]{1f, 1f, 1f, 1f},
            new float[]{0f, 0f, 0f, 1f},
            new float[]{0f, 0f, 0f, 0f},
            100f
    ),

    /**
     * Gold material properties.
     */
    GOLD(
            new float[]{0.82f, 0.76f, 0, 1},
            new float[]{0.95f, 0.92f, 0.5f, 1},
            new float[]{1f, 1f, 1f, 1f},
            75
    ),

    /**
     * Gray material that shines blueish
     */
    SILVER(
            new float[]{0.75f, 0.75f, 0.75f, 1},
            new float[]{0.90f, 0.90f, 1.0f, 1},
            new float[]{1f, 1f, 1f, 1f},
            50
    ),

    /**
     * regular non-shiny material properties.
     */
    PLASTIC(
            new float[]{1f, 1f, 1f, 1},
            new float[]{0.5f, 0.5f, 0.5f, 1},
            new float[]{0f, 0f, 0f, 0f},
            2
    ),

    /**
     * Rough, brown material
     */
    WOOD(
            new float[]{0.5f, 0.27f, 0.14f, 1},
            new float[]{0.02f, 0.04f, 0.06f, 1},
            new float[]{0.75f, 0.375f, 0.20f, 1},
            0.5f
    );

    /**
     * The material natural color.
     */
    public final Color4f diffuse;
    /**
     * The specular reflectance of the material.
     */
    public final Color4f specular;
    /**
     * color of the lines of the material
     */
    public final Color4f lineColor;
    /**
     * The specular exponent of the material.
     */
    public final float shininess;

    Material(float[] diffuse, float[] specular, float[] lineColor, float shininess) {
        this.diffuse = new Color4f(diffuse);
        this.specular = new Color4f(specular);
        this.lineColor = new Color4f(lineColor);
        this.shininess = shininess;
    }
}
