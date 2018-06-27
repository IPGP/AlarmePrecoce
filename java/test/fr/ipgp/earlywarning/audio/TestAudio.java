/*

 */
package fr.ipgp.earlywarning.audio;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * @author patriceboissier
 */
public class TestAudio {

    /**
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

        String file = "./resources/test.wav";
        try {
            MessagePlayback messagePlayback = new MessagePlayback(file);
            System.out.println("Begin");
            messagePlayback.playClip();
            while (messagePlayback.isPlaying()) {
                Thread.sleep(1000);
                System.out.println("playing sound...");
            }
            System.out.println("End");
        } catch (UnsupportedAudioFileException uafe) {
            System.out.println("Unsupported audio file exception : " + uafe.getMessage());
        } catch (LineUnavailableException lue) {
            System.out.println("Line anavailable exception : " + lue.getMessage());
        } catch (IOException ioe) {
            System.out.println("I/O exception : " + ioe.getMessage());
        } catch (InterruptedException ignored) {

        }
    }
}
