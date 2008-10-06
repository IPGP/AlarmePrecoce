/**
 * 
 */
package fr.ipgp.earlywarning.audio;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.File;
/**
 * @author patriceboissier
 *
 */
public class MessagePlayback {
	private Clip messageClip;
	private DataLine.Info info;
	
	public MessagePlayback(String filename) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
		messageClip = null;
		AudioInputStream stream = AudioSystem.getAudioInputStream(new File(filename));
		AudioFormat format = stream.getFormat(); // read the file details
		if (format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED) {
			format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), format.getSampleSizeInBits()*2, format.getChannels(), format.getFrameSize()*2, format.getFrameRate(), true);
			stream = AudioSystem.getAudioInputStream(format, stream);
		}
		// Create the clip
		info = new DataLine.Info(Clip.class, stream.getFormat(), ((int)stream.getFrameLength()*format.getFrameSize()));
		messageClip = (Clip) AudioSystem.getLine(info);
		// finally, load the data into the Clip object
		messageClip.open(stream);
	}
	
	// play a clip through the speakers
	public void playClip() {
		if( messageClip != null ) {
			// first, reset the clip to play from the beginning
			messageClip.stop();
			messageClip.setFramePosition(0); // this routine can be used to start a wav file from an arbitrary position
			// play the wav file
			messageClip.start();
		}
	}
	
	public boolean isPlaying() {
		return messageClip.isActive();
	}
}
