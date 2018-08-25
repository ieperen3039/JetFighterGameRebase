package nl.NG.Jetfightergame.Launcher;

import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

import static java.awt.GridBagConstraints.*;

/**
 * @author Geert van Ieperen. Created on 23-8-2018.
 */
public final class SwingToolbox {

    public static final Dimension BUTTON_DIMENSIONS = new Dimension(200, 45);
    public static final Dimension TEXTBOX_DIMENSIONS = new Dimension(300, BUTTON_DIMENSIONS.height);
    public static final int BUTTON_BORDER = 5;
    private static final boolean ALLOW_FLYiNG_TEXT = false;

    public static Insets borderOf(int pixels) {
        return new Insets(pixels, pixels, pixels, pixels);
    }

    public static JButton getButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(BUTTON_DIMENSIONS);
        return button;
    }

    public static JPanel getFiller() {
        JPanel filler = new JPanel();
        filler.setOpaque(false);
        if (ServerSettings.DEBUG) {
            filler.setBorder(new BevelBorder(BevelBorder.LOWERED));
        }
        return filler;
    }

    public static GridBagConstraints getConstraints(int x, int y, int fill) {
        return new GridBagConstraints(x, y, 1, 1, 0, 0, CENTER, fill, borderOf(0), 0, 0);
    }

    public static GridBagConstraints getButtonConstraints(int index) {
        return new GridBagConstraints(0, index, 1, 1, 1, 0, CENTER, HORIZONTAL, borderOf(BUTTON_BORDER), 0, 0);
    }

    public static GridBagConstraints getButtonConstraints(int x, int y) {
        return new GridBagConstraints(x, y, 1, 1, 1, 0, CENTER, HORIZONTAL, borderOf(BUTTON_BORDER), 0, 0);
    }

    public static GridBagConstraints getFillConstraints(int x, int y) {
        return new GridBagConstraints(x, y, 1, 1, 1, 1, CENTER, BOTH, borderOf(0), 0, 0);
    }

    public static GridBagConstraints getFillConstraints(int x, int y, int width, int height) {
        return new GridBagConstraints(x, y, width, height, 1, 1, CENTER, BOTH, borderOf(0), 0, 0);
    }

    public static JPanel invisiblePanel() {
        return new JPanel() {
            @Override
            public void paint(Graphics g) {
                paintChildren(g);
            }
        };
    }

    public static JPanel getImagePanel(File imageFile) {
        try {
            final BufferedImage sourceImage = ImageIO.read(imageFile);

            return new JPanel() {
                int width = sourceImage.getWidth();
                int height = sourceImage.getHeight();

                @Override
                public void paint(Graphics g) {
                    float scalar1 = (float) getHeight() / height;
                    float scalar2 = (float) getWidth() / width;
                    float scalar = Math.max(scalar1, scalar2);
                    Image image = sourceImage.getScaledInstance((int) (this.width * scalar), (int) (height * scalar), Image.SCALE_FAST);
                    g.drawImage(image, 0, 0, null);

                    paintChildren(g);
                }
            };

        } catch (Exception e) {
            Logger.ERROR.print("Could not load image " + imageFile);
            return new JPanel();
        }
    }

    public static ActionListener getActionWorker(Runnable runnable) {
        return e -> {
            Thread thread = new Thread(runnable, "Action worker");
            thread.setDaemon(true);
            thread.start();
        };
    }

    public static ActionListener getActionWorker(String name, Runnable runnable) {
        return e -> {
            Thread thread = new Thread(runnable, name);
            thread.setDaemon(true);
            thread.start();
        };
    }

    public static JTextComponent flyingText() {
        JTextComponent jText = new JTextArea() {
            @Override
            public void setText(String t) {
                super.setText(t);
                if (ALLOW_FLYiNG_TEXT) {
                    setOpaque(true);
                    JRootPane rootPane = getRootPane();
                    if (rootPane != null) rootPane.repaint();
                    setOpaque(false);
                }
            }
        };
        jText.setEditable(false);
        return jText;
    }

    static Runnable getSliderSetting(JPanel panel, int col, String text, Consumer<Float> effect, float min, float max, float current) {
        JTextArea settingName = new JTextArea(text);
        panel.add(settingName, SwingToolbox.getButtonConstraints(col, RELATIVE));

        JPanel combo = new JPanel(new BorderLayout());
        JSlider userInput = new JSlider();
        userInput.setMinimumSize(BUTTON_DIMENSIONS);
        float diff = max - min;
        combo.add(userInput);
        JTextComponent valueDisplay = flyingText();
        userInput.addChangeListener(e -> valueDisplay.setText(
                String.format("%5.1f", Math.toDegrees((userInput.getValue() / 100f) * diff + min))
        ));
        userInput.setValue((int) (100 * (current - min) / diff));
        combo.add(valueDisplay, BorderLayout.EAST);

        panel.add(combo, SwingToolbox.getButtonConstraints(col, RELATIVE));

        return () -> effect.accept(((userInput.getValue() / 100f) * diff) + min);
    }

    static Runnable getTextboxSetting(JPanel panel, int col, String text, Consumer<String> effect, String current) {
        JTextArea settingName = new JTextArea(text);
        panel.add(settingName, SwingToolbox.getButtonConstraints(col, RELATIVE));
        JTextField userInput = new JTextField();
        userInput.setMinimumSize(BUTTON_DIMENSIONS);
        userInput.setText(current);
        panel.add(userInput, SwingToolbox.getButtonConstraints(col, RELATIVE));
        return () -> effect.accept(userInput.getText());
    }

    private static Runnable getBooleanSetting(JPanel panel, int col, String text, Consumer<Boolean> effect, boolean current) {
        JTextArea settingName = new JTextArea(text);
        panel.add(settingName, SwingToolbox.getButtonConstraints(col, RELATIVE));
        JToggleButton button = new JToggleButton();
        button.setEnabled(current);
        panel.add(button, SwingToolbox.getButtonConstraints(col, RELATIVE));
        return () -> effect.accept(button.isSelected());
    }

    static <Type> Runnable getEnumSetting(JPanel panel, int col, String text, Type[] values, Consumer<Type> effect, Type current) {
        JTextArea settingName = new JTextArea(text);
        panel.add(settingName, SwingToolbox.getButtonConstraints(col, RELATIVE));
        JComboBox<Type> userInput = new JComboBox<>(values);
        userInput.setSelectedItem(current);
        panel.add(userInput, SwingToolbox.getButtonConstraints(col, RELATIVE));
        return () -> effect.accept(userInput.getItemAt(userInput.getSelectedIndex()));
    }
}
