/**
 * 
 */
package fr.ipgp.earlywarning.controler;

import java.util.NoSuchElementException;
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
	
	private void configureAudioSerial() throws ConversionException, NoSuchElementException, NoSuchPortException, PortInUseException, UnsupportedCommOperationException {
		comSpeed = EarlyWarning.configuration.getInt("audioserial.serial.speed");
		comPort = EarlyWarning.configuration.getString("audioserial.serial.port");
		comPortId=CommPortIdentifier.getPortIdentifier(comPort);
		serialPort=(SerialPort)comPortId.open("Envoi",5000);
		serialPort.setFlowControlMode(SerialPort.);
		serialPort.setSerialPortParams(comSpeed,SerialPort.DATABITS_8, SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
		EarlyWarning.appLogger.debug("Ouverture du port "+comPort);

			//pour lire et Žcrire avec des streams:
			//in=new BufferedReader(
			//new InputStreamReader(serialPort.getInputStream()));
			//out=new PrintWriter(serialPort.getOutputStream());
	}
}
