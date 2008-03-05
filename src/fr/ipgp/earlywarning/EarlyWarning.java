/**
 * 
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
		    configuration = new XMLConfiguration("resources/configuration.xml");
		}
		catch(ConfigurationException cex)
		{
			cex.printStackTrace();
		}
		String port = configuration.getString("network.port");
		System.out.println(port);
		configuration.addProperty("network.address", (String) "195.83.188.8");
	}
}
