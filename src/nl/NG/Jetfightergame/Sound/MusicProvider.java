package nl.NG.Jetfightergame.Sound;

import nl.NG.Jetfightergame.Engine.Settings;
import nl.NG.Jetfightergame.Tools.Timer;
import nl.NG.Jetfightergame.Engine.Updatable;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Tom Peters
 * requires a lot of work yet
 */

public class MusicProvider extends Thread implements Updatable {

    public static final String path = "res/Sounds/SICCMIXX.wav";
    private boolean isRunning = false;
    private Clip clip;
    private FloatControl gainControl;
    private long duration;
    private Timer loopTimer;
    private long timeToPlay;
    private float baseVolume = 2f;
    //thread parameters
    private float targetVolume;
    private float currentVolume;
    private boolean fading = false;
    private float fadeStep = 0.1f;
    private Thread fadeThread = new Thread();
    private volatile boolean cancelled = false;

    public MusicProvider(Timer gameLoopTimer){
        this.loopTimer = gameLoopTimer;
        try {
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void init() throws IOException {
        if (!isRunning) return;

        try {
            InputStream fileStream = new BufferedInputStream(new FileInputStream(path));
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(fileStream);
            AudioFormat format = audioInputStream.getFormat();

            long frames = audioInputStream.getFrameLength();
            this.duration = (long)((frames+0.0) / format.getFrameRate()); //in seconds
            this.clip = AudioSystem.getClip();
            this.clip.open(audioInputStream);
            this.gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            gainControl.setValue(baseVolume);
            this.clip.start();
        } catch (LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            throw new IOException(e);
        }
    }

    /**
     * Check every update cycle if there should start a new clip playing. A new clip starts after 2 times the time of the original clip
     */
    @Override
    public void update(float deltaTime) {
        if (timeToPlay < loopTimer.getTime()) { //start again after 2 times the duration
            if (clip != null) clip.start();
            timeToPlay = loopTimer.getTime() + duration * 1000 * 2; //1000 to convert to miliseconds
        }
    }

    /**
     * Toggle the music on or off
     */
    public void toggle() {
        if (isRunning) {
            isRunning = false;
            cleanup();
        } else {
            isRunning = true;
            try {
                init();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Set the basic volume of the music
     * @param percentage The volume in percentages
     */
    public void setBaseVolume(float percentage) {
        this.baseVolume = percentage * (Settings.MAX_VOLUME - Settings.MIN_VOLUME) + Settings.MIN_VOLUME;
        fadeVolumeTo(baseVolume, true);
    }

    /**
     * Fade the volume to a specific value
     * @param value the value to fade to
     * @param overridePrevious Whether or not the previous fade should be overridden.
     *                         This should be the case in for example the menu
     */
    public void fadeVolumeTo(float value, boolean overridePrevious) {
        currentVolume = gainControl.getValue();
        if (!fading && currentVolume != value && !overridePrevious) {  //prevent running it twice
            targetVolume = value;
            fadeThread = new Thread(this);
            fadeThread.start();
        } else if (overridePrevious) {
            //cancel fadeThread
            cancelled = true;
            //wait for it to finish
            waitForMusicThread(fadeThread);
            cancelled = false;
            //start fadeThread again
            targetVolume = value;
            fadeThread = new Thread(this);
            fadeThread.start();
        }
    }

    /**
     * The actual fading of the volume, using a thread
     */
    @Override
    public void run() {
        fading = true;
        if (currentVolume > targetVolume) {
            while (currentVolume > targetVolume) {
                if (cancelled) {
                    cancelled = false;
                    fading = false;
                    return;
                }
                if (currentVolume - fadeStep > -60) {
                    currentVolume -= fadeStep;
                } else {
                    currentVolume = -60;//minimum no clue why
                }
                gainControl.setValue(currentVolume);
                try {
                    fadeThread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else if (currentVolume < targetVolume) {
            while (currentVolume < targetVolume) {
                if (cancelled) {
                    cancelled = false;
                    fading = false;
                    return;
                }
                if (currentVolume + fadeStep > 6) {
                    currentVolume += fadeStep;
                } else {
                    currentVolume = 6;//maximum no clue why
                }
                gainControl.setValue(currentVolume);
                try {
                    fadeThread.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        cancelled = false;
        fading = false;
    }

    /**
     * @return Whether or not the music is currently playing
     */
    public boolean isOn() {
        return isRunning;
    }

    /**
     * Wait for a thread to finish.
     * @param t
     */
    private void waitForMusicThread(Thread t) {
        try {
            t.join();
        } catch(InterruptedException e) {
            System.err.println("Interrupted while waiting for music thread to join");
            e.printStackTrace();
        }
    }

    /**
     * Cleanup all threads to prevent music from playing a little bit after the main program closed.
     */
    public void cleanup() {
        if (clip != null)
            clip.stop();
        cancelled = true;
        waitForMusicThread(fadeThread);
        cancelled = false;
    }
}
