package nl.NG.Jetfightergame.Launcher;

import nl.NG.Jetfightergame.Settings.ServerSettings;
import nl.NG.Jetfightergame.Tools.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.function.Consumer;

import static java.awt.GridBagConstraints.*;

/**
 * @author Geert van Ieperen. Created on 23-8-2018.
 */
public final class SwingToolbox {
    private static final String BUTTON_TRUE_TEXT = "Enabled";
    private static final String BUTTON_FALSE_TEXT = "Disabled";

    public static final Dimension MAIN_BUTTON_DIM = new Dimension(250, 40);
    public static final Dimension SMALL_BUTTON_DIM = new Dimension(150, 20);
    public static final Dimension TEXTBOX_DIMENSIONS = new Dimension(300, MAIN_BUTTON_DIM.height);
    public static final int BUTTON_BORDER = 8;

    public static boolean ALLOW_FLYING_TEXT = true;

    public static Insets borderOf(int pixels) {
        return new Insets(pixels, pixels, pixels, pixels);
    }

    public static JButton getButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(MAIN_BUTTON_DIM);
        button.setMinimumSize(SMALL_BUTTON_DIM);
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
        return new GridBagConstraints(1, index, 1, 1, 1, 0, CENTER, HORIZONTAL, borderOf(BUTTON_BORDER), 0, 5);
    }

    public static GridBagConstraints getButtonConstraints(int x, int y) {
        return new GridBagConstraints(x, y, 1, 1, 1, 0, CENTER, HORIZONTAL, borderOf(BUTTON_BORDER), 0, 5);
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

    static JScrollPane scrollable(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane(panel) {
            @Override
            public void paint(Graphics g) {
                paintChildren(g);
            }
        };
        scrollPane.getVerticalScrollBar().addAdjustmentListener(a ->
                scrollPane.getRootPane().repaint()
        );
        scrollPane.getHorizontalScrollBar().addAdjustmentListener(a ->
                scrollPane.getRootPane().repaint()
        );
        return scrollPane;
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
        JTextComponent jText = new JTextArea();
        if (ALLOW_FLYING_TEXT) {
            jText.addMouseListener(new RepaintUponMouseEvent(jText));
            jText.setOpaque(false);
        }
        jText.setEditable(false);
        return jText;
    }

    static Runnable getSliderSetting(JPanel parent, int col, String text, Consumer<Float> effect, float min, float max, float current) {
        JPanel panel = getSettingPanel(text);

        JPanel combo = new JPanel(new BorderLayout());
        JSlider userInput = new JSlider();
        userInput.setMinimumSize(MAIN_BUTTON_DIM);
        float diff = max - min;
        combo.add(userInput);
        JTextComponent valueDisplay = flyingText();
        userInput.addChangeListener(e -> valueDisplay.setText(
                String.format("%5.1f", Math.toDegrees((userInput.getValue() / 100f) * diff + min))
        ));
        userInput.setValue((int) (100 * (current - min) / diff));
        combo.add(valueDisplay, BorderLayout.EAST);
        panel.add(combo);

        parent.add(panel, getButtonConstraints(col, RELATIVE));

        return () -> effect.accept(((userInput.getValue() / 100f) * diff) + min);
    }

    static Runnable getTextboxSetting(JPanel parent, int col, String text, String current, Consumer<String> effect) {
        JPanel panel = getSettingPanel(text);

        JTextField userInput = new JTextField();
        userInput.setMinimumSize(TEXTBOX_DIMENSIONS);
        userInput.setText(current);
        panel.add(userInput);

        parent.add(panel, getButtonConstraints(col, RELATIVE));

        return () -> effect.accept(userInput.getText());
    }

    public static Runnable getBooleanSetting(JPanel parent, int col, String text, boolean current, Consumer<Boolean> effect) {
        JPanel panel = getSettingPanel(text);

        JToggleButton button = new JToggleButton(current ? BUTTON_TRUE_TEXT : BUTTON_FALSE_TEXT);
        button.addActionListener(e -> button.setText(button.isSelected() ? BUTTON_TRUE_TEXT : BUTTON_FALSE_TEXT));
        button.setMinimumSize(SMALL_BUTTON_DIM);
        button.setSelected(current);
        panel.add(button);

        parent.add(panel, getButtonConstraints(col, RELATIVE));

        return () -> effect.accept(button.isSelected());
    }

    static <Type> Runnable getChoiceSetting(JPanel parent, int col, String text, Type[] values, Type current, Consumer<Type> effect) {
        JPanel panel = getSettingPanel(text);

        JComboBox<Type> userInput = new JComboBox<>(values);

        userInput.setSelectedItem(current);
        panel.add(userInput);

        parent.add(panel, getButtonConstraints(col, RELATIVE));

        return () -> effect.accept(userInput.getItemAt(userInput.getSelectedIndex()));
    }

    private static JPanel getSettingPanel(String text) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        JTextComponent settingName = flyingText();
        settingName.setText(text);
        panel.add(settingName, BorderLayout.NORTH);
        return panel;
    }

    public static void appendToText(JTextPane console, String text, AttributeSet attributes) {
        try {
            int pos = console.getDocument().getLength();
            console.getStyledDocument().insertString(pos, text, attributes);
        } catch (BadLocationException ex) {
            console.setText(ex.toString());
        }
    }

    private static class RepaintUponMouseEvent implements MouseListener {
        private final JComponent component;

        public RepaintUponMouseEvent(JTextComponent component) {
            this.component = component;
        }

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            SwingUtilities.invokeLater(this::repaint);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            repaint();
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
            repaint();
        }

        private void repaint() {
            component.getRootPane().repaint(
                    component.getX(), component.getY(), component.getWidth(), component.getHeight()
            );
        }
    }
}
