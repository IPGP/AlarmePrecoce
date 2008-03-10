/**
 * Created Mar 01, 2008 11:01:05 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning;

import fr.ipgp.earlywarning.controler.EarlyWarningThread;
import fr.ipgp.earlywarning.controler.DataBaseHeartBeatThread;
import java.io.IOException;
import org.apache.commons.configuration.*;
import org.apache.log4j.*;

/**
 * @author Patrice Boissier
 * Entry point for the application
 * Reads the configuration file and then create the EarlyWarningThread.
 */
public class EarlyWarning {

	public static XMLConfiguration configuration;
	public static Logger appLogger = Logger.getLogger(EarlyWarning.class.getName());
	
	public static void main(String[] args) throws IOException  {
		
		setLogger();
		readConfiguration();
		
		appLogger.debug("Entering application.");
		
		Thread earlyWarningThread = new EarlyWarningThread();
		earlyWarningThread.start();
		
		if (configuration.getBoolean("heartbeat.use_heartbeat")) {
			Thread dataBaseHeartBeatThread = new DataBaseHeartBeatThread();
			dataBaseHeartBeatThread.start();
		}
	}
	
	/**
	 * Reads XML configuration file and creates a XMLConfiguration object
	 * The application log a fatal error and exists if the configuration file is missing
	 */
	private static void readConfiguration() {
		try {
		    configuration = new XMLConfiguration("resources/earlywarning.xml");
		} catch(ConfigurationException cex) {
			appLogger.fatal("Fichier de configuration absent ou illisible. Fin de l'application");
			System.exit(1);
		}
	}
	
	/**
	 * Configure Log4J
	 */
	private static void setLogger() {
		PropertyConfigurator.configure("resources/log4j.properties");
	}
}
