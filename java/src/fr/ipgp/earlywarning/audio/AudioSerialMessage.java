/**
 * Created Sep 08, 2008 2:54:12 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.audio;

import java.util.NoSuchElementException;
import java.io.*;
import javax.comm.*;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.configuration.ConversionException;
import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.controler.QueueManagerThread;
/**
 * This class manages the serial port and the audio card of the computer. The aim is to send an audio and ASCII message to an UHF radio system.<br/>
 * Implements the singleton pattern
 * @author patriceboissier
 */
public class AudioSerialMessage {
	private static AudioSerialMessage uniqueInstance;
	private static QueueManagerThread queueManagerThread;
	private MessagePlayback messagePlayback;
	private int comSpeed;
	private String comPort;
	private CommPortIdentifier comPortId;
    private SerialPort serialPort;
	private OutputStream outStream;
	private String textMessage;
	private String beginCommands;
	private String endCommands;
	private int delay;

	private AudioSerialMessage() {
    	EarlyWarning.appLogger.debug("Audio/Serial Message Thread creation");
    	try {
    		configureAudioSerial();
    	} catch (ConversionException ce) {
    		EarlyWarning.appLogger.error("Audio Serial speed or port has a wrong value in configuration file : check audioserial section of earlywarning.xml configuration file. Audio Serial disabled.");
    		queueManagerThread.setUseSound(false);
        	return;
    	} catch (NoSuchElementException nsee) {
    		EarlyWarning.appLogger.error("Audio Serial speed or port is missing in configuration file : check audioserial section of earlywarning.xml configuration file. Audio Serial disabled.");
    		queueManagerThread.setUseSound(false);
        	return;
    	} catch (NoSuchPortException nspe) {
    		EarlyWarning.appLogger.error("No such port "+comPort+". Audio Serial disabled.");
    		queueManagerThread.setUseSound(false);
        	return;
    	}
	}
		
	public static synchronized AudioSerialMessage getInstance (QueueManagerThread queue) {
		if (uniqueInstance == null) {
    		uniqueInstance = new AudioSerialMessage();
		}
		queueManagerThread = queue;
    	return uniqueInstance;
	}
	
	/**
	 * Get information for the serial port from the configuration file.
	 */
	private void configureAudioSerial() throws ConversionException, NoSuchElementException, NoSuchPortException {
		comSpeed = EarlyWarning.configuration.getInt("audioserial.serial.speed");
		comPort = EarlyWarning.configuration.getString("audioserial.serial.port");
		delay = EarlyWarning.configuration.getInt("audioserial.delay");
		textMessage = EarlyWarning.configuration.getString("audioserial.message");
		beginCommands = EarlyWarning.configuration.getString("audioserial.begin_commands");
		endCommands = EarlyWarning.configuration.getString("audioserial.end_commands");
		comPortId=CommPortIdentifier.getPortIdentifier(comPort);
	}
	
	/**
	 * Opens and configures the serial port, sets the DTR to true and then sends the message ASCII text. 
	 * It then waits for "delay" seconds and starts to play the audio message.
	 * Finally the serial port is closed.
	 * @param message the ASCII message
	 */
	public void sendMessage(String message, String wavFile) {
		try {
			serialPort=(SerialPort)comPortId.open("Envoi",5000);
			EarlyWarning.appLogger.debug("Opening serial port");
		} catch (PortInUseException piue) {
			EarlyWarning.appLogger.error("Serial port already "+comPort+" in use");
		}
		try {
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			serialPort.setDTR(true);
			serialPort.setSerialPortParams(comSpeed,SerialPort.DATABITS_8, SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
			EarlyWarning.appLogger.debug("Configuring serial port");
		} catch (UnsupportedCommOperationException ucoe) {
			EarlyWarning.appLogger.error("Error while configuring the serial port "+comPort);
		}
		String finalMessage = textMessage + " " + message;
		try {
			outStream = serialPort.getOutputStream();
			copy(beginCommands, outStream);	
			byte[] data = finalMessage.getBytes();
			outStream.write(data);
			copy(endCommands, outStream);
			EarlyWarning.appLogger.debug("Sending message to serial port : " + finalMessage);
			
			Thread.sleep(1000 * delay);
			EarlyWarning.appLogger.debug("Sleeping for "+delay+" seconds");
			EarlyWarning.appLogger.debug("Playing audio message "+wavFile);
			messagePlayback = new MessagePlayback(wavFile);
			messagePlayback.playClip();
			while (messagePlayback.isPlaying()) {
				Thread.sleep(1000);
			}
			
		} catch (InterruptedException ie) {
			EarlyWarning.appLogger.error("Error while sleeping!");
		} catch (FileNotFoundException fnfe) {
			EarlyWarning.appLogger.error("Error while opening beginCMD or endCMD : " + fnfe.getMessage());
		} catch (IOException ioe) {
			EarlyWarning.appLogger.error("Error while sending data to the serial port");
		} catch (UnsupportedAudioFileException uafe) {
			EarlyWarning.appLogger.error("Unsupported audio file exception : " + uafe.getMessage());
		} catch (LineUnavailableException lue) {
			EarlyWarning.appLogger.error("Line anavailable exception : " + lue.getMessage());
		} finally {
			if (outStream != null) {
				try {
					outStream.close( );
					EarlyWarning.appLogger.debug("Closing the serial port");
				} catch (IOException ex) {
					EarlyWarning.appLogger.error("Error while closing the serial port");
				}
			}
		}
	}
	
	/**
	 * @return true if the wave file is currently playing, false if it's not
	 */
	public boolean isPlaying() {
		return messagePlayback.isPlaying();
	}
	
	/**
	 * Copy the content of the file (passed as parameter) to the outputstream (passed as parameter).
	 * It allows sending the content of a binary file to the serial port.
	 * @param fileName the file name to be copied 
	 * @param out the output stream to copy to
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void copy(String fileName, OutputStream out) throws FileNotFoundException, IOException {
		File file = new File(fileName);
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			byte[] buffer = new byte[1024];
			while (true) {
				int bytesRead = is.read(buffer);
				if (bytesRead == -1) break;
				out.write(buffer, 0, bytesRead);
			}
		} finally {
			if (is != null) {
				try {
					is.close( );
					EarlyWarning.appLogger.debug("Closing the command file");
				} catch (IOException ex) {
					EarlyWarning.appLogger.error("Error while closing the command file");
				}
			}
		}
	}

	
//	/**
//	 * Sends the content of a binary file to the the serial port
//	 */
//	private void sendFile(String filePath) throws FileNotFoundException, IOException {
//		File file = new File(filePath);
//		InputStream is = null;
//		BufferedOutputStream buffer = null;
//		try {
//			is = new BufferedInputStream(new FileInputStream(file));
//			ByteArrayOutputStream bytesArray = new ByteArrayOutputStream();
//			buffer = new BufferedOutputStream(bytesArray);
//			
//			int read = is.read();
//			int[] toWrite = new int[4096];
//			int compteur = 0;
//			long ouonestrendu=0;
//			
//			while(read > -1) {
//				toWrite[compteur] = read;
//				read = is.read();
//				compteur++;
//				if(compteur == 4096) {
//					compteur=0;
//					ouonestrendu++;
//					for(int x=0;x<4096;x++)
//						buffer.write(toWrite[x]);
//					outStream.write(bytesArray.toByteArray());
//					bytesArray.reset();
//				}
//			}
//		
//			for(int x=0;x<4096;x++)
//				buffer.write(toWrite[x]);
//			buffer.flush();
//			outStream.write(bytesArray.toByteArray());
//			outStream.flush();
//		} finally {
//			is.close();
//			buffer.close();
//		}
//	} 
}
