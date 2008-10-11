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
	private int comSpeed;
	private String comPort;
	private CommPortIdentifier comPortId;
    private SerialPort serialPort;
	private OutputStream outStream;
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

		try {
			outStream = serialPort.getOutputStream();
			byte[] data = message.getBytes();
			outStream.write(data);
			EarlyWarning.appLogger.debug("Sending message to serial port : " + message);
			Thread.sleep(1000 * delay);
			EarlyWarning.appLogger.debug("Sleeping for "+delay+" seconds");
			EarlyWarning.appLogger.debug("Playing audio message "+wavFile);
			MessagePlayback messagePlayback = new MessagePlayback(wavFile);
			messagePlayback.playClip();
			while (messagePlayback.isPlaying()) {
				Thread.sleep(1000);
			}
			EarlyWarning.appLogger.debug("Playing sound for trigger");
		} catch (InterruptedException ie) {
			EarlyWarning.appLogger.error("Error while sleeping!");
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
}
