package nl.NG.Jetfightergame.Settings;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

import java.io.IOException;
import java.io.StringWriter;

/**
 * @author Geert van Ieperen created on 26-4-2018.
 */
public final class ClientSettings {
    /** engine settings */
    public static boolean DEBUG_SCREEN = false;

    /** visual settings */
    public static int TARGET_FPS = 60;
    // rendering is delayed by RENDER_DELAY seconds to smooth out rendering and prevent extrapolation
    public static float RENDER_DELAY = 1f / ServerSettings.TARGET_TPS;
    public static float FOV = (float) Math.toRadians(60);
    // absolute size of frustum
    public static float Z_NEAR = 0.05f;
    public static float Z_FAR = 4000.0f;
    public static boolean CULL_FACES = true;
    public static boolean INVERT_CAMERA_ROTATION = false;
    public static boolean V_SYNC = true;
    public static int ANTIALIAS = 1;
    public static boolean SHOW_LIGHT_POSITIONS = DEBUG_SCREEN;
    public static float HIGHLIGHT_LINE_WIDTH = 1f;
    public static Color4f CHECKPOINT_ACTIVE_COLOR = Color4f.YELLOW;
    public static final int MAX_POINT_LIGHTS = 10;

    /** controller settings; these modifiers are also used to inverse direction */
    public static float THROTTLE_MODIFIER = 1f;
    public static float YAW_MODIFIER = 1f;
    public static float PITCH_MODIFIER = 0.05f;
    public static float ROLL_MODIFIER = 0.05f;
    public static int CONNECTION_SEND_FREQUENCY = ServerSettings.TARGET_TPS;

    /** sound */
    public static float SOUND_MASTER_GAIN = 0;
    public static float MAX_VOLUME = 6f;
    public static float MIN_VOLUME = -20f;

    /** particle settings */
    public static float PARTICLE_MODIFIER = 1f;
    public static Color4f EXPLOSION_COLOR_1 = Color4f.RED;
    public static Color4f EXPLOSION_COLOR_2 = Color4f.YELLOW;
    public static int PARTICLE_SPLITS = 2;
    public static int PARTICLECLOUD_SPLIT_SIZE = 2000;
    public static float PARTICLECLOUD_MIN_TIME = 0.5f;
    public static float FIRE_PARTICLE_SIZE = 0.8f;

    /** thrust particle settings */
    public static float THRUST_PARTICLE_SIZE = 0.8f;
    public static float THRUST_PARTICLE_LINGER_TIME = 0.5f;
    public static float THRUST_PARTICLES_PER_SECOND = 500f * PARTICLE_MODIFIER;
    public static float JET_THRUST_SPEED = 60f;
    public static float ROCKET_THRUST_SPEED = 50f;
    public static Color4f THRUST_COLOR_1 = Color4f.ORANGE;
    public static Color4f THRUST_COLOR_2 = Color4f.RED;

    /** miscellaneous */
    public static Material PORTAL_MATERIAL = Material.PLASTIC;
    public static EntityClass JET_TYPE = EntityClass.JET_SPITZ;
    public static final boolean USE_SOCKET_FOR_OFFLINE = false;

    public static String toJSONString() throws IOException {
        StringWriter writer = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(writer);

        gen.writeStartObject();
        /* DO NOT CHANGE STRING NAMES (for backward compatibility) */
        gen.writeNumberField("TARGET_FPS", TARGET_FPS);
        gen.writeNumberField("RENDER_DELAY", RENDER_DELAY);
        gen.writeNumberField("SERVER_PORT", ServerSettings.SERVER_PORT);
        gen.writeBooleanField("MAKE_REPLAY", ServerSettings.MAKE_REPLAY);
        gen.writeNumberField("TARGET_TPS", ServerSettings.TARGET_TPS);
        gen.writeNumberField("PARTICLE_MODIFIER", PARTICLE_MODIFIER);
        gen.writeNumberField("CONNECTION_SEND_FREQUENCY", CONNECTION_SEND_FREQUENCY);
        gen.writeStringField("JET_TYPE", JET_TYPE.toString());
//        gen.writeNumberField("",);
//        gen.writeNumberField("",);
        gen.writeEndObject();

        gen.close();
        return writer.toString();
    }

    public static void readJSONString(String entry) throws IOException {
        JsonParser src = new JsonFactory().createParser(entry);

        String fieldName = src.nextFieldName();
        while (src.nextToken() != JsonToken.END_OBJECT) {
            switch (fieldName) {
                /* DO NOT CHANGE STRING NAMES (for backward compatibility) */
                case "TARGET_FPS":
                    TARGET_FPS = src.getIntValue();
                case "RENDER_DELAY":
                    RENDER_DELAY = src.getIntValue();
                case "ServerSettings.SERVER_PORT":
                    ServerSettings.SERVER_PORT = src.getIntValue();
                case "ServerSettings.MAKE_REPLAY":
                    ServerSettings.MAKE_REPLAY = src.getBooleanValue();
                case "ServerSettings.TARGET_TPS":
                    ServerSettings.TARGET_TPS = src.getIntValue();
                case "PARTICLE_MODIFIER":
                    PARTICLE_MODIFIER = src.getFloatValue();
                case "CONNECTION_SEND_FREQUENCY":
                    CONNECTION_SEND_FREQUENCY = src.getIntValue();
                case "JET_TYPE":
                    JET_TYPE = EntityClass.valueOf(src.getValueAsString());
            }
        }

        src.close();
    }
}
