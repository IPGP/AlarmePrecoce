package audio;

import fr.ipgp.earlywarning.audio.MessagePlayback;

import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

/**
 * @author Patrice Boissier
 */
public class ValidationTestAudio {

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
        } catch (UnsupportedAudioFileException ex) {
            System.out.println("Unsupported audio file exception: " + ex.getMessage());
        } catch (LineUnavailableException ex) {
            System.out.println("Line unavailable exception: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O exception: " + ex.getMessage());
        } catch (InterruptedException ignored) {

        }
    }
}
