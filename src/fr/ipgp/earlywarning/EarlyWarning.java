/**
 * Created Mar 01, 2008 11:01:05 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP. COUCOU
 */
package fr.ipgp.earlywarning;

import fr.ipgp.earlywarning.controler.EarlyWarningThread;
import fr.ipgp.earlywarning.controler.DataBaseHeartBeatThread;
import fr.ipgp.earlywarning.utilities.CommonUtilities;
import fr.ipgp.earlywarning.telephones.*;
import fr.ipgp.earlywarning.controler.*;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.*;
import org.apache.commons.configuration.*;
import org.apache.log4j.*;
/**
 * @author Patrice Boissier
 * Entry point for the application
 * Reads the configuration file and then create the EarlyWarningThread.
 */
public class EarlyWarning {

	public static Configuration configuration;
	public static Logger appLogger = Logger.getLogger(EarlyWarning.class.getName());
	public static FileReferenceCallList defaultCallList;
	
	public static void main(String[] args)  {
		
		setLogger();
		
		checkUnicity();
		
		readConfiguration();
		
		appLogger.debug("Entering application.");
		
		startEarlyWarningThread();
		
		startDataBaseHeartBeatThread();

		createGui();
		
	}
	
	/**
	 * Reads XML configuration file and creates a XMLConfiguration object
	 * The application log a fatal error and exists if the configuration file is missing
	 */
	private static void readConfiguration() {
		try {
		    configuration = new XMLConfiguration("resources/earlywarning.xml");
		} catch(ConfigurationException cex) {
			appLogger.fatal("Fatal error : configuration file not present or not readable. Exiting application");
			System.exit(1);
		}
	}
	
	/**
	 * Configure Log4J
	 */
	private static void setLogger() {
		PropertyConfigurator.configure("resources/log4j.properties");
	}
	
	/**
	 * Check unicity of the application
	 */
	private static void checkUnicity() {
		try {
			if (!CommonUtilities.appIsUnique("EarlyWarning")) {
				appLogger.fatal("Application already running : exiting");
				System.exit(1);
			}
		} catch (FileNotFoundException fnfe) {
			appLogger.warn("Unable to create lock file to ensure unicity of the application");
		} catch (IOException ioe) {
			appLogger.warn("Unable to set lock file to ensure unicity of the application");
		}
	}

	/**
	 * Start EarlyWarningThread
	 */
	private static void startEarlyWarningThread() {
		try {
			Thread earlyWarningThread = EarlyWarningThread.getInstance(defaultCallList);
			earlyWarningThread.start();
		} catch (ConversionException ce) {
			appLogger.fatal("Fatal error : an element value has wrong type : check network section of earlywarning.xml configuration file. Exiting application.");
			System.exit(1);
		} catch (NoSuchElementException nsee) {
			appLogger.fatal("Fatal error : An element value is undefined : check network section of earlywarning.xml configuration file. Exiting application.");
			System.exit(1);
		} catch (IOException ioe) {
			appLogger.fatal("Fatal error : I/O exception : " + ioe.getMessage() + ". Exiting application.");
			System.exit(1);
		}
	}
	
	/**
	 * Start DataBaseHeartBeatThread
	 */
	private static void startDataBaseHeartBeatThread() {
		try {
			if (configuration.getBoolean("heartbeat.use_heartbeat")) {
				Thread dataBaseHeartBeatThread = DataBaseHeartBeatThread.getInstance();
				dataBaseHeartBeatThread.start();
			}
		} catch (ConversionException ce) {
			appLogger.error("An element value has wrong type : check hearbeat section of earlywarning.xml configuration file. HearBeat notification disabled.");
		} catch (NoSuchElementException nsee) {
			appLogger.error("An element value is undefined : check hearbeat section of earlywarning.xml configuration file. HearBeat notification disabled.");	
		} catch (IOException ioe) {
			appLogger.error("I/O exception : " + ioe.getMessage() + ". HearBeat notification disabled.");
		}		
	}
	
	/**
	 * Create GUI
	 */
	private static void createGui() {
		try {
			defaultCallList = new FileReferenceCallList(configuration.getString("gateway.defaults.call_list"));
			FileReferenceCallListControler fileReferenceCallListControler = new FileReferenceCallListControler(defaultCallList);
			fileReferenceCallListControler.displayView();
		} catch (InvalidFileNameException ifne) {
			appLogger.error("An invalid call list : check hearbeat section of earlywarning.xml configuration file. HearBeat notification disabled.");
		}
	}
}
