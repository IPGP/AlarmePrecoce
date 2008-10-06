/**
 * 
 */
package fr.ipgp.earlywarning.audio;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import fr.ipgp.earlywarning.audio.MessagePlayback;
/**
 * @author patriceboissier
 *
 */
public class TestAudio {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String file="./resources/test.wav";
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
		} catch (InterruptedException ie) {
			
		}
	}
}
