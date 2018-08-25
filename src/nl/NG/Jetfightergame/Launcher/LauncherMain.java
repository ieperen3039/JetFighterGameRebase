package nl.NG.Jetfightergame.Launcher;

import nl.NG.Jetfightergame.Engine.JetFighterGame;
import nl.NG.Jetfightergame.EntityGeneral.Factory.EntityClass;
import nl.NG.Jetfightergame.ScreenOverlay.JFGFont;
import nl.NG.Jetfightergame.ServerNetwork.EnvironmentClass;
import nl.NG.Jetfightergame.Settings.ClientSettings;
import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Logger;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

import static java.awt.GridBagConstraints.*;
import static nl.NG.Jetfightergame.Launcher.SwingToolbox.*;

/**
 * @author Geert van Ieperen. Created on 23-8-2018.
 */
public class LauncherMain implements Runnable {
    private static final String BACKDROP_IMAGE = "Backdrop.png"; // TODO backdrop image
    private static final Dimension MINIMUM_LAUNCHER_SIZE = new Dimension(1100, 700);

    private static final Font LARGE_FONT = JFGFont.LUCIDA_CONSOLE.asAWTFont(20f);

    /** settings */
    private EnvironmentClass map = EnvironmentClass.ISLAND_MAP;
    private boolean doStore = false;
    private boolean hideLauncherWhenRunning = false;

    /** panels */
    private JPanel mainMenuPanel;
    private JPanel imagePanel;
    private JPanel ipSearchPanel;
    private JPanel loadoutPanel;
    private JPanel settingsPanel;
    private final JFrame frame;

    private final JPanel[] mutExclPanels;

    private LauncherMain() {
        frame = new JFrame();
        frame.setTitle(ServerSettings.GAME_NAME + " Launcher");
        frame.setSize(MINIMUM_LAUNCHER_SIZE);
        frame.setMinimumSize(MINIMUM_LAUNCHER_SIZE);

        imagePanel = SwingToolbox.getImagePanel(Directory.pictures.getFile(BACKDROP_IMAGE));
        imagePanel.setLayout(new GridBagLayout());

        mainMenuPanel = getMainMenuPanel();
        imagePanel.add(mainMenuPanel, SwingToolbox.getConstraints(0, 0, BOTH));

        imagePanel.add(SwingToolbox.getFiller(), SwingToolbox.getFillConstraints(1, 0));

        ipSearchPanel = getIPSearchPanel();
        loadoutPanel = getLoadoutPanel();
        settingsPanel = getSettingsPanel();

        mutExclPanels = new JPanel[]{ipSearchPanel, loadoutPanel, settingsPanel};
        for (JPanel panel : mutExclPanels) {
            panel.setVisible(false);
            imagePanel.add(panel, SwingToolbox.getFillConstraints(2, 0));
        }

        imagePanel.add(SwingToolbox.getFiller(), SwingToolbox.getFillConstraints(3, 0));

        frame.add(imagePanel);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Point centerPoint = ge.getCenterPoint();

        int dx = centerPoint.x - (frame.getWidth() / 2);
        int dy = centerPoint.y - (frame.getHeight() / 2);

        frame.setLocation(dx, dy);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
    }

    private JPanel getSettingsPanel() {
        JPanel panel = SwingToolbox.invisiblePanel();
        panel.setLayout(new GridBagLayout());

        final int columnsOfSettings = 3;
        final int cols = (columnsOfSettings * 2) - 1;
        int x = 0;

        ArrayList<Runnable> settings = new ArrayList<>();

        // for the title
        GridBagConstraints titleCon = new GridBagConstraints(0, 1, cols, 1, 1, 1, CENTER, HORIZONTAL, borderOf(0), 0, 0);
        final int startOfFields = 1;

        JTextComponent title = SwingToolbox.flyingText();
        title.setFont(LARGE_FONT);
        title.setText("Settings");
        panel.add(title, titleCon);

        settings.add(getTextboxSetting(panel, posInSet(x++), "Target FPS",
                (s) -> ClientSettings.TARGET_FPS = Integer.valueOf(s), String.valueOf(ClientSettings.TARGET_FPS)));

        settings.add(getTextboxSetting(panel, posInSet(x++), "Render Delay",
                (s) -> ClientSettings.RENDER_DELAY = Float.valueOf(s), String.valueOf(ClientSettings.RENDER_DELAY)));

        settings.add(getEnumSetting(panel, posInSet(x++), "Jet Type", EntityClass.getJets(),
                (e) -> ClientSettings.JET_TYPE = e, ClientSettings.JET_TYPE));

        settings.add(getSliderSetting(panel, posInSet(x++), "Field of View",
                (f) -> ClientSettings.FOV = f, 0.35f, 2.10f, ClientSettings.FOV));

        settings.add(getTextboxSetting(panel, posInSet(x++), "Thrust particle density",
                (f) -> ClientSettings.THRUST_PARTICLES_PER_SECOND = Float.valueOf(f), String.valueOf(ClientSettings.THRUST_PARTICLES_PER_SECOND)));

//        ServerSettings.TARGET_TPS = obj_server.getInt("server_ticks_per_second");
//        ServerSettings.SERVER_PORT = obj_server.getInt("server_port");
//        ServerSettings.MAKE_RECORDING = obj_server.getBoolean("store_recording");

        int nOfRows = (((x / 3) + 1) * 2) + 1;
        panel.add(getFiller(), getFillConstraints(0, RELATIVE));
        panel.add(getFiller(), getFillConstraints(1, startOfFields, 1, nOfRows));
        panel.add(getFiller(), getFillConstraints(3, startOfFields, 1, nOfRows));
//        panel.add(getFiller(), getFillConstraints(0, startOfFields + nOfRows, cols, 1));

        JButton applyButton = SwingToolbox.getButton("Apply");
        panel.add(applyButton, SwingToolbox.getButtonConstraints(cols - 1, startOfFields + nOfRows));
        applyButton.addActionListener(e -> settings.forEach(Runnable::run));

        return panel;
    }

    private static int posInSet(int x) {
        return (x % 3) * 2;
    }

    private JPanel getLoadoutPanel() {
        JPanel panel = SwingToolbox.invisiblePanel();
        panel.setLayout(new GridBagLayout());

        panel.add(SwingToolbox.getFiller(), SwingToolbox.getFillConstraints(0, RELATIVE));

        JTextComponent text = SwingToolbox.flyingText();
        text.setText("Choose a jet:");
        panel.add(text, SwingToolbox.getButtonConstraints(RELATIVE));

        for (EntityClass jet : EntityClass.getJets()) {
            String jetName = jet.name().replace("_", " ");
            JButton chooseJet = SwingToolbox.getButton(jetName);
            panel.add(chooseJet, SwingToolbox.getButtonConstraints(RELATIVE));
            chooseJet.addActionListener(e -> {
                ClientSettings.JET_TYPE = jet;
                select(mainMenuPanel);
            });
        }

        panel.add(SwingToolbox.getFiller(), SwingToolbox.getFillConstraints(0, RELATIVE));

        return panel;
    }

    private JPanel getIPSearchPanel() {
        JPanel panel = SwingToolbox.invisiblePanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints cons;

        panel.add(SwingToolbox.getFiller(), SwingToolbox.getFillConstraints(0, 0));

        JTextComponent messageDisplay = SwingToolbox.flyingText();
        messageDisplay.setText("Enter the target IP address");
        messageDisplay.setMinimumSize(BUTTON_DIMENSIONS);
        cons = new GridBagConstraints(0, 1, 2, 1, 0, 0, LINE_END, HORIZONTAL, borderOf(BUTTON_BORDER), 0, 0);
        panel.add(messageDisplay, cons);

        JTextField ipField = new JTextField();
        ipField.setFont(LARGE_FONT);
        ipField.setPreferredSize(SwingToolbox.TEXTBOX_DIMENSIONS);
        cons = new GridBagConstraints(0, 2, 2, 1, 0, 0, LINE_END, HORIZONTAL, borderOf(BUTTON_BORDER), 0, 0);
        panel.add(ipField, cons);

        JButton searchButton = SwingToolbox.getButton("Validate");
        cons = new GridBagConstraints(0, 3, 1, 1, 0, 0, LINE_START, HORIZONTAL, borderOf(BUTTON_BORDER), 0, 0);
        panel.add(searchButton, cons);

        JButton connectButton = SwingToolbox.getButton("Connect");
        cons = new GridBagConstraints(1, 3, 1, 1, 0, 0, LINE_START, HORIZONTAL, borderOf(BUTTON_BORDER), 0, 0);
        panel.add(connectButton, cons);

        panel.add(SwingToolbox.getFiller(), SwingToolbox.getFillConstraints(0, 4));

        // implementation
        ipField.addActionListener(e -> searchButton.doClick());

        searchButton.addActionListener(SwingToolbox.getActionWorker("Searching IP address", () -> {
            messageDisplay.setText("Searching...");
            InetAddress address = getIP(ipField.getText());
            if (address == null) {
                messageDisplay.setText("That is not a valid address");
            } else {
                messageDisplay.setText("Found " + address);
            }
        }));

        connectButton.addActionListener(SwingToolbox.getActionWorker("Searching IP and connect", () -> {
            messageDisplay.setText("Searching...");
            InetAddress address = getIP(ipField.getText());
            if (address == null) {
                messageDisplay.setText("That is not a valid address");
            } else {
                messageDisplay.setText("Starting game with " + address);
                launchGame(address);
            }
        }));

        return panel;
    }

    private JPanel getMainMenuPanel() {
        JPanel panel = SwingToolbox.invisiblePanel();
        panel.setLayout(new GridBagLayout());

        JButton startLocal = SwingToolbox.getButton("Start Local");
        startLocal.addActionListener(e -> launchGame(null));
        panel.add(startLocal, SwingToolbox.getButtonConstraints(RELATIVE));

        JButton searchLocal = SwingToolbox.getButton("Search Local");
        searchLocal.addActionListener(e -> launchGame(InetAddress.getLoopbackAddress()));
        panel.add(searchLocal, SwingToolbox.getButtonConstraints(RELATIVE));

        JButton searchInternet = SwingToolbox.getButton("Search on IP");
        searchInternet.addActionListener(e -> select(ipSearchPanel));
        panel.add(searchInternet, SwingToolbox.getButtonConstraints(RELATIVE));

        JButton changeLoadout = SwingToolbox.getButton("Change Loadout");
        changeLoadout.addActionListener(e -> select(loadoutPanel));
        panel.add(changeLoadout, SwingToolbox.getButtonConstraints(RELATIVE));

        panel.add(SwingToolbox.getFiller(), SwingToolbox.getFillConstraints(0, RELATIVE));

        JButton settingsButton = SwingToolbox.getButton("Settings");
        settingsButton.addActionListener(e -> select(settingsPanel));
        panel.add(settingsButton, SwingToolbox.getButtonConstraints(RELATIVE));

        JButton exitGame = SwingToolbox.getButton("Exit Launcher");
        exitGame.addActionListener(e -> frame.dispose());
        panel.add(exitGame, SwingToolbox.getButtonConstraints(RELATIVE));

        return panel;
    }

    /** @return the InetAddress represented by text, or null if it is invalid */
    private static InetAddress getIP(String text) {
        try {
            return InetAddress.getByName(text);

        } catch (UnknownHostException e) {
            if (ServerSettings.DEBUG) Logger.ERROR.print(e);
            return null;
        }
    }

    private void select(JPanel panelToShow) {
        for (JPanel panel : mutExclPanels) {
            panel.setVisible(panel == panelToShow);
        }
    }

    private void launchGame(InetAddress address) {
        try {
            JetFighterGame game = new JetFighterGame(true, doStore, null, map, address);
            frame.setVisible(hideLauncherWhenRunning);
            game.root();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            frame.setVisible(true);
        }
    }

    @Override
    public void run() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        setLookAndFeel(JFGFont.LUCIDA_CONSOLE);
        new LauncherMain().run();
    }

    private static void setLookAndFeel(JFGFont font) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

            FontUIResource fontResource = new FontUIResource(font.asAWTFont(14f));
            int count = 0;

            UIDefaults uiDefaults = UIManager.getDefaults();
            for (Map.Entry<Object, Object> e : uiDefaults.entrySet()) {
                Object item = e.getKey();
                if (item instanceof String) {
                    String property = (String) item;
                    if (property.contains(".font")) {
                        uiDefaults.put(item, fontResource);
                        count++;
                    }
                }
            }
            Logger.DEBUG.print("Changed " + count + " font fields to " + font.name);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            Logger.ERROR.print("Error while loading Look&Feel : " + e);
            if (ServerSettings.DEBUG) e.printStackTrace();
            Logger.ERROR.print("Falling back on default L&F, this may look weird");
        } catch (UnsupportedLookAndFeelException e) {
            Logger.ERROR.print("Invalid Look&Feel : " + e.getMessage());
            if (ServerSettings.DEBUG) e.printStackTrace();
            Logger.ERROR.print("Falling back on default L&F, this may look weird");
        }
    }
}
