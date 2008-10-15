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
		String finalMessage = textMessage + message;
		try {
			outStream = serialPort.getOutputStream();
			sendFile("./resources/beginCMD");
			sendFile2("./resources/beginCMD");
			byte[] data = finalMessage.getBytes();
			outStream.write(data);
			EarlyWarning.appLogger.debug("Sending message to serial port : " + message);
			Thread.sleep(1000 * delay);
			EarlyWarning.appLogger.debug("Sleeping for "+delay+" seconds");
			EarlyWarning.appLogger.debug("Playing audio message "+wavFile);
			messagePlayback = new MessagePlayback(wavFile);
			messagePlayback.playClip();
			while (messagePlayback.isPlaying()) {
				Thread.sleep(1000);
			}
			sendFile("./resources/endCMD");
			sendFile2("./resources/endCMD");
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
	
	public boolean isPlaying() {
		return messagePlayback.isPlaying();
	}
	
	private void sendFile(String fileName) throws FileNotFoundException, IOException {
		String str;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fileName));
			while ((str = in.readLine()) != null) {
				for (int i = 0;i< str.length();i++){
					outStream.write((int)str.charAt(i));
				}
			}
		} finally {
			in.close();
		}
	}
	
	private void sendFile2(String filePath) throws FileNotFoundException, IOException {
		File file = new File(filePath);
		if(file.exists()) {
			System.out.println("Envoi du fichier "+filePath);
			long fileSize = file.length();
			System.out.println("Taille : "+ fileSize);
			long nbPasses = fileSize / 4096;
			System.out.println("Passages supposŽs : "+nbPasses);
	
			InputStream is = new BufferedInputStream(new FileInputStream(file));
			ByteArrayOutputStream bytesArray = new ByteArrayOutputStream();
			BufferedOutputStream buffer = new BufferedOutputStream(bytesArray);
	
			int read = is.read();
			int[] toWrite = new int[4096];
			int compteur = 0;
			long ouonestrendu=0;
	
			while(read > -1) {
				toWrite[compteur] = read;
				read = is.read();
				compteur++;
				if(compteur == 4096) {
					compteur=0;
					ouonestrendu++;
					for(int x=0;x<4096;x++)
						buffer.write(toWrite[x]);
	
					outStream.write(bytesArray.toByteArray());
	
					bytesArray.reset();
					System.out.println("Avancement : "+(float) ouonestrendu/nbPasses * 100+"%");
				}
			}
	
			for(int x=0;x<4096;x++)
				buffer.write(toWrite[x]);
			buffer.flush();
			outStream.write(bytesArray.toByteArray());
			outStream.flush();
	
			System.out.println("Avancement: "+(float) ouonestrendu/nbPasses * 100+"%");
	
			is.close();
			buffer.close();
			System.out.println("Passages effectuŽs : "+ouonestrendu);
		} else {
			System.out.println("Le fichier "+filePath+" est introuvable");
		}
	} 
}
