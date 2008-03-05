/**
 * Created Mar 01, 2008 11:01:05 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning;

import fr.ipgp.earlywarning.controler.EarlyWarningThread;
import java.io.IOException;
import org.apache.commons.configuration.*;

/**
 * @author Patrice Boissier
 *
 */
public class EarlyWarning {

	private static XMLConfiguration configuration = null;

	public static void main(String[] args) throws IOException  {

		readConfiguration();
		
		Thread earlyWarningThread = new EarlyWarningThread();
		earlyWarningThread.start();
		
//		System.out.println("Interruption du Thread");
//		
//		earlyWarningThread.interrupt();
//		try {
//			earlyWarningThread.join();
//		} catch (InterruptedException ie) {
//			ie.printStackTrace();
//		}
//		System.out.println("Thread terminé!");
		
	}
	
	/**
	 * Reads XML configuration file and creates a XMLConfiguration object
	 */
	private static void readConfiguration() {
		try
		{
		    configuration = new XMLConfiguration("resources/configuration.xml");
		}
		catch(ConfigurationException cex)
		{
			cex.printStackTrace();
		}
	}
}
