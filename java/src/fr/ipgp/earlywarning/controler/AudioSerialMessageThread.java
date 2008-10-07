/**
 * 
 */
package fr.ipgp.earlywarning.controler;

import java.util.NoSuchElementException;
import java.io.*;
import javax.comm.*;
import org.apache.commons.configuration.ConversionException;
import fr.ipgp.earlywarning.EarlyWarning;
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
	}
	// NoSuchPortException, PortInUseException, UnsupportedCommOperationException
	private void configureAudioSerial() throws ConversionException, NoSuchElementException, NoSuchPortException {
		comSpeed = EarlyWarning.configuration.getInt("audioserial.serial.speed");
		comPort = EarlyWarning.configuration.getString("audioserial.serial.port");
		comPortId=CommPortIdentifier.getPortIdentifier(comPort);
	}
	
	public void sendMessage(String message) {
		try {
			serialPort=(SerialPort)comPortId.open("Envoi",5000);
		} catch (PortInUseException piue) {
			EarlyWarning.appLogger.error("Serial port already "+comPort+" in use");
		}
		try {
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN);
			serialPort.setSerialPortParams(comSpeed,SerialPort.DATABITS_8, SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		} catch (UnsupportedCommOperationException ucoe) {
			EarlyWarning.appLogger.error("Error while configuring the serial port "+comPort);
		}
		EarlyWarning.appLogger.debug("Ouverture du port "+comPort);
		try {
			outStream = serialPort.getOutputStream();
			byte[] data = message.getBytes();
			outStream.write(data);
		} catch (IOException ioe) {
			EarlyWarning.appLogger.error("");
		} finally {
			if (outStream != null) {
				try {
					outStream.close( );
				} catch (IOException ex) {
					System.err.println(ex);
				}
			}
		}
	}
}
