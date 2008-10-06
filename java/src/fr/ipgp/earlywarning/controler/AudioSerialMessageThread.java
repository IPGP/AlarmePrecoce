/**
 * 
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.earlywarning.EarlyWarning;

/**
 * @author patriceboissier
 *
 */
public class AudioSerialMessageThread extends Thread{
	private static AudioSerialMessageThread uniqueInstance;
	private static QueueManagerThread queueManagerThread;
	
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
}
