/*
  Created Mar 01, 2008 11:01:05 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning;

import fr.ipgp.earlywarning.controler.DataBaseHeartBeatThread;
import fr.ipgp.earlywarning.controler.EarlyWarningThread;
import fr.ipgp.earlywarning.contacts.ContactListManagerServer;
import fr.ipgp.earlywarning.test.TriggerV2Sender2;
import fr.ipgp.earlywarning.test.TriggerV2Sender3;
import fr.ipgp.earlywarning.utilities.CommonUtilities;
import fr.ipgp.earlywarning.utilities.ConfigurationValidator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.NoSuchElementException;

/**
 * Entry point for the application<br/>
 * First start the logger (Log4J)<br/>
 * Then, it checks its own uniqueness.<br/>
 * Reads the configuration file and then create the EarlyWarningThread.<br/>
 * If the configuration file specifies it, it starts the DataBaseHeartBeatThread.<br/>
 * Last, it creates the GUI
 *
 * @author Patrice Boissier
 */
public class EarlyWarning {

    public static XMLConfiguration configuration;
    public static Logger appLogger = Logger.getLogger(EarlyWarning.class.getName());

    public static void main(final String[] args) {
        setLogger();

        checkUniqueness();

        readConfiguration();

        appLogger.debug("Entering application.");

        startEarlyWarningThread();

        startDataBaseHeartBeatThread();

        createGui();

        startContactsServer();

        parseArgs(args);
    }

    private static void parseArgs(final String[] args) {
        boolean testCalls = false;
        for (String arg : args)
            if (arg.equalsIgnoreCase("--testcalls"))
                testCalls = true;

        if (testCalls) {
            appLogger.info("--testcalls was passed: two triggers will be emitted.");
            Thread thread = new Thread() {
                public void run() {
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    appLogger.info("Sending first trigger");
                    TriggerV2Sender3.main(args);
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                    appLogger.info("Sending second trigger");
                    TriggerV2Sender2.main(args);
                }
            };
            thread.start();
        }
    }

    /**
     * Reads XML configuration file and creates a XMLConfiguration object
     * The application log a fatal error and exists if the configuration file is missing
     */
    private static void readConfiguration() {
        try {
            configuration = new XMLConfiguration("resources/earlywarning.xml");
            configuration.setThrowExceptionOnMissing(true);
            ConfigurationValidator validator = new ConfigurationValidator(configuration);
            validator.validate();
        } catch (ConfigurationException cex) {
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
     * Check uniqueness of the application
     */
    private static void checkUniqueness() {
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
     * Starts the contact list manager Web server
     */
    private static void startContactsServer() {
        ContactListManagerServer server;
        String home = EarlyWarning.configuration.getString("contacts.home");
        server = new ContactListManagerServer(home);

        try {
            server.startServer();
        } catch (IOException e) {
            appLogger.fatal("Fatal error : cannot start server on port " + server.getPort());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Start EarlyWarningThread
     */
    private static void startEarlyWarningThread() {
        try {
            Thread earlyWarningThread = EarlyWarningThread.getInstance();
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
        }
    }

    /**
     * Create GUI
     */
    @SuppressWarnings("EmptyMethod")
    private static void createGui() {
        // TODO: fix this
        //        try {
//            FileCallListControler fileCallListControler = new FileCallListControler(defaultContactList, fileCallLists);
//            fileCallListControler.displayView();
//        } catch (HeadlessException ignored)
//        {
//            appLogger.info("Running in headless mode.");
//        }
    }
}
