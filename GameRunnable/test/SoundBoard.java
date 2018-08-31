import nl.NG.Jetfightergame.Sound.AudioFile;
import nl.NG.Jetfightergame.Sound.AudioSource;
import nl.NG.Jetfightergame.Sound.SoundEngine;
import nl.NG.Jetfightergame.Tools.Directory;
import nl.NG.Jetfightergame.Tools.Logger;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.concurrent.CountDownLatch;

import static javax.swing.JFileChooser.CANCEL_SELECTION;
import static nl.NG.Jetfightergame.Tools.Toolbox.checkALError;

/**
 * @author Geert van Ieperen. Created on 30-8-2018.
 */
public final class SoundBoard {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Soundboard");
        JPanel main = new JPanel(new BorderLayout());

        CountDownLatch[] select = {new CountDownLatch(1)};
        SoundEngine soundEngine = new SoundEngine();
        AudioSource src = AudioSource.empty;
        checkALError();

        try {

            JFileChooser dialog = new JFileChooser(Directory.soundEffects.getFile(""));
            dialog.addActionListener(a -> {
                if (a.getActionCommand().equals(CANCEL_SELECTION)) {
                    frame.dispose();
                }
                select[0].countDown();
            });
            main.add(dialog, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel();
            JToggleButton doRepeat = new JToggleButton("Repeat");
            buttonPanel.add(doRepeat);
            JSlider pitch = new JSlider(0, 40, 10);
            pitch.setPreferredSize(new Dimension(400, 100));
            pitch.setMajorTickSpacing(10);
            pitch.setPaintTicks(true);
            buttonPanel.add(pitch);

            main.add(buttonPanel, BorderLayout.SOUTH);

            frame.add(main);
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.pack();
            frame.setVisible(true);

            select[0].await();
            while (frame.isVisible()) {
                File file = dialog.getSelectedFile();
                if (file != null) {
                    src.dispose();
                    src = play(file, pitch.getValue() / 10f, doRepeat.isSelected());
                    checkALError();
                }
                select[0] = new CountDownLatch(1);
                select[0].await();
            }

        } catch (Exception e) {
            e.printStackTrace();
            frame.dispose();

        } finally {
            checkALError();
            src.dispose();

            soundEngine.closeDevices();
        }
    }

    private static AudioSource play(File file, float pitch, boolean doLoop) {
        AudioFile audioData = new AudioFile(file);
        audioData.load();
        Logger.INFO.print(file.getName(), "pitch is " + pitch);
        AudioSource src = new AudioSource(audioData, 1.0f, doLoop);
        src.setPitch(pitch);
        src.play();
        return src;
    }
}
