/**
 * 
 */
package fr.ipgp.earlywarning.controler;

import java.util.NoSuchElementException;
import java.io.*;
import javax.comm.*;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.apache.commons.configuration.ConversionException;
import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.audio.MessagePlayback;
/**
 * @author patriceboissier
 *
 */
public class AudioSerialMessageThread extends Thread{
	private static AudioSerialMessageThread uniqueInstance;
	private static QueueManagerThread queueManagerThread;
	private int comSpeed;
	private String comPort;
	private CommPortIdentifier comPortId;
    private SerialPort serialPort;
	private OutputStream outStream;	
	private AudioSerialMessageThread() {
		this("AudioSerialMessageThread");
	}
	
	private AudioSerialMessageThread(String name) {
		super(name);
	}
	
	public static synchronized AudioSerialMessageThread getInstance (QueueManagerThread queue) {
		if (uniqueInstance == null) {
    		uniqueInstance = new AudioSerialMessageThread();
		}
		queueManagerThread = queue;
    	return uniqueInstance;
	}
	
	public void run() {
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

	private void configureAudioSerial() throws ConversionException, NoSuchElementException, NoSuchPortException {
		comSpeed = EarlyWarning.configuration.getInt("audioserial.serial.speed");
		comPort = EarlyWarning.configuration.getString("audioserial.serial.port");
		comPortId=CommPortIdentifier.getPortIdentifier(comPort);
	}
	
	public void sendMessage(String message) {
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

		String file="./resources/test.wav";
		
		try {
			outStream = serialPort.getOutputStream();
			byte[] data = message.getBytes();
			outStream.write(data);
			EarlyWarning.appLogger.debug("Sending message to serial port : " + message);
			Thread.sleep(10000);
			EarlyWarning.appLogger.debug("Sleeping for 10 seconds");
			MessagePlayback messagePlayback = new MessagePlayback(file);
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
