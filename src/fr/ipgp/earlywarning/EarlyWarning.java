/**
 * 
 */
package fr.ipgp.earlywarning;

import fr.ipgp.earlywarning.controler.EarlyWarningThread;
import java.io.IOException;
import org.apache.commons.configuration.*;
import org.apache.commons.lang.*;
/**
 * @author Patrice Boissier
 *
 */
public class EarlyWarning {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException  {
		System.out.println("Démarrage du système d'alarme précoce");
		
		readConfiguration();
		
		System.out.println("Lecture du fichier de configuration");

		Thread earlyWarningThread = new EarlyWarningThread();
		earlyWarningThread.start();
		
		System.out.println("Tread lancé!");
		
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
	
	private static void readConfiguration() {
		try
		{
		    XMLConfiguration config = new XMLConfiguration("./resources/configuration.xml");
		    // do something with config
		}
		catch(ConfigurationException cex)
		{
		    // something went wrong, e.g. the file was not found
		}

	}
}
