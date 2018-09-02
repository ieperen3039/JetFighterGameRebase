package nl.NG.Jetfightergame.Settings;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.Rendering.Material;
import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Logger;
import nl.NG.Jetfightergame.Tools.Toolbox;
import nl.NG.Jetfightergame.Tools.Vectors.Color4f;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Geert van Ieperen created on 26-4-2018.
 */
@SuppressWarnings("Duplicates")
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
    public static boolean SHOW_LIGHT_POSITIONS = false;
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
    public static float MASTER_GAIN = 0.5f;
    public static float BACKGROUND_MUSIC_GAIN = 0.1f;

    /** particle settings */
    public static float PARTICLE_MODIFIER = 1f;
    public static Color4f EXPLOSION_COLOR_1 = Color4f.RED;
    public static Color4f EXPLOSION_COLOR_2 = Color4f.YELLOW;
    public static int PARTICLE_SPLITS = 2;
    public static int PARTICLECLOUD_SPLIT_SIZE = 2000;
    public static float PARTICLECLOUD_MIN_TIME = 0.5f;
    public static float FIRE_PARTICLE_SIZE = 0.8f;

    /** thrust particle settings */
    public static final float BASE_THRUST_PPS = 500f;
    public static float THRUST_PARTICLE_SIZE = 0.8f;
    public static float THRUST_PARTICLE_LINGER_TIME = 0.5f;
    public static float JET_THRUST_SPEED = 60f;
    public static float ROCKET_THRUST_SPEED = 50f;
    public static Color4f THRUST_COLOR_1 = Color4f.ORANGE;
    public static Color4f THRUST_COLOR_2 = Color4f.RED;

    /** miscellaneous */
    public static Material PORTAL_MATERIAL = Material.PLASTIC;
    public static EntityClass JET_TYPE = EntityClass.SPECTATOR_CAMERA;
    public static Color4f JET_COLOR = Color4f.YELLOW;
    public static final boolean USE_SOCKET_FOR_OFFLINE = false;

    public static void writeSettingsToFile(String filePath) throws IOException {
        FileOutputStream out = new FileOutputStream(filePath);
        JsonGenerator gen = new JsonFactory().createGenerator(out);
        gen.useDefaultPrettyPrinter();

        gen.writeStartObject();
        {
            // settings
            /* DO NOT CHANGE STRING NAMES (for backward compatibility) */
            gen.writeNumberField("TARGET_FPS", TARGET_FPS);
            gen.writeNumberField("RENDER_DELAY", RENDER_DELAY);
            gen.writeNumberField("SERVER_PORT", ServerSettings.SERVER_PORT);
            gen.writeBooleanField("MAKE_REPLAY", ServerSettings.SERVER_MAKE_REPLAY);
            gen.writeNumberField("TARGET_TPS", ServerSettings.TARGET_TPS);
            gen.writeNumberField("PARTICLE_MODIFIER", PARTICLE_MODIFIER);
            gen.writeNumberField("CONNECTION_SEND_FREQUENCY", CONNECTION_SEND_FREQUENCY);
            gen.writeStringField("JET_TYPE", JET_TYPE.toString());
            gen.writeBooleanField("LOGGER_PRINT_CALLSITES", Logger.doPrintCallsites);
            gen.writeNumberField("NUMBER_OF_NPCS", ServerSettings.NOF_FUN);
            gen.writeArrayFieldStart("JET_COLOR");
            {
                gen.writeNumber((int) (JET_COLOR.red * 255));
                gen.writeNumber((int) (JET_COLOR.green * 255));
                gen.writeNumber((int) (JET_COLOR.blue * 255));
            }
            gen.writeEndArray();

            // keybindings
            for (KeyBinding binding : KeyBinding.values()) {
                gen.writeArrayFieldStart(binding.name());
                {
                    gen.writeNumber(binding.getKey());
                    gen.writeBoolean(binding.isMouseAxis());
                    gen.writeNumber(binding.getXBox());
                    gen.writeBoolean(binding.isXBoxAxis());
                }
                gen.writeEndArray();
            }
        }
        gen.writeEndObject();

        gen.close();
        out.close();
    }

    public static void readSettingsFromFile(String filename) throws IOException {
        ObjectNode src = new ObjectMapper().readValue(Directory.settings.getFile(filename), ObjectNode.class);
        KeyBinding[] keyBindings = KeyBinding.values();

        Iterator<Map.Entry<String, JsonNode>> fields = src.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode result = entry.getValue();

            switch (fieldName) {
                /* DO NOT CHANGE STRING NAMES (for backward compatibility) */
                case "TARGET_FPS":
                    TARGET_FPS = result.intValue();
                    break;
                case "RENDER_DELAY":
                    RENDER_DELAY = result.floatValue();
                    break;
                case "SERVER_PORT":
                    ServerSettings.SERVER_PORT = result.intValue();
                    break;
                case "MAKE_REPLAY":
                    ServerSettings.SERVER_MAKE_REPLAY = result.booleanValue();
                    break;
                case "TARGET_TPS":
                    ServerSettings.TARGET_TPS = result.intValue();
                    break;
                case "PARTICLE_MODIFIER":
                    PARTICLE_MODIFIER = result.floatValue();
                    break;
                case "CONNECTION_SEND_FREQUENCY":
                    CONNECTION_SEND_FREQUENCY = result.intValue();
                    break;
                case "JET_TYPE":
                    String s = result.textValue();
                    JET_TYPE = Toolbox.findClosest(s, EntityClass.getJets());
                    break;
                case "LOGGER_PRINT_CALLSITES":
                    Logger.doPrintCallsites = result.booleanValue();
                    break;
                case "NUMBER_OF_NPCS":
                    ServerSettings.NOF_FUN = result.intValue();
                    break;
                case "JET_COLOR":
                    assert result.isArray();
                    Iterator<JsonNode> values = result.elements();
                    assert values.hasNext();
                    JET_COLOR = new Color4f(
                            values.next().intValue(),
                            values.next().intValue(),
                            values.next().intValue()
                    );
                    assert !values.hasNext();
                    break;

                default: // maybe not the fastest, but no exception is thrown when the string is not found
                    for (KeyBinding target : keyBindings) {
                        if (fieldName.equals(target.toString())) {
                            assert result.isArray();
                            Iterator<JsonNode> node = result.elements();
                            assert node.hasNext();
                            target.installNew(node.next().intValue(), node.next().booleanValue(), false);
                            target.installNew(node.next().intValue(), node.next().booleanValue(), true);
                            assert !node.hasNext();

                            break;
                        }
                    }
            }
        }
    }
}
