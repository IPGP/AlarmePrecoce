/*
  Created Mar 01, 2008 11:01:05 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning;

import fr.ipgp.earlywarning.contacts.ContactListManagerServer;
import fr.ipgp.earlywarning.controler.DataBaseHeartBeatThread;
import fr.ipgp.earlywarning.controler.EarlyWarningThread;
import fr.ipgp.earlywarning.heartbeat.HeartbeatServerThread;
import fr.ipgp.earlywarning.test.TriggerV2Sender2;
import fr.ipgp.earlywarning.test.TriggerV2Sender3;
import fr.ipgp.earlywarning.utilities.CommonUtilities;
import fr.ipgp.earlywarning.utilities.ConfigurationValidator;
import fr.ipgp.earlywarning.utilities.FileSearch;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import static fr.ipgp.earlywarning.utilities.PathUtilities.buildPath;

/**
 * Entry point for the application
 * <ul>
 * <li>Starts the logger (Log4J)</li>
 * <li>Validates the configuration file</li>
 * <li>Checks its own uniqueness.</li>
 * <li>Starts the call list Web interface server</li>
 * <li>Reads the configuration file and then create the {@link EarlyWarningThread}.</li>
 * <li>If the configuration file specifies it, starts the {@link DataBaseHeartBeatThread}.</li>
 * <li>Creates the GUI, if possible.</li>
 * </ul>
 *
 * @author Patrice Boissier
 * @author Thomas Kowalski
 */
public class EarlyWarning {
    public static final Logger appLogger = Logger.getLogger(EarlyWarning.class.getName());
    private static final String CONFIGURATION_PATH = "resources/earlywarning.xml";
    public static XMLConfiguration configuration;

    /**
     * The EarlyWarning application entry point.
     *
     * @param args the command line arguments
     */
    public static void main(final String[] args) {
        // Parse pre-initialization arguments
        PreInitOptions options = parseArgsPreInit(args);

        // Configure the logger
        setLogger();

        // Verify the app is not already running
        checkUniqueness();

        // Read the configuration (and verify its validity, unless the user asked not to)
        readConfiguration(options.validateConfiguration);

        // Now really start the app
        appLogger.debug("Entering application.");

        // Create and start the main threads
        startEarlyWarningThread();
        startDataBaseHeartBeatThread();
        startContactsServer();
        startFailoverManager();

        // Create the GUI for compatible platforms
        createGui();

        // Parse post-initialization arguments
        parseArgsPostInit(args);
    }

    /**
     * Parses the command line arguments.
     * This should be called before initializing anything else, since it determines some initialization options (like configuration validation settings).
     *
     * @param args the command line arguments
     * @return the pre-initialization settings, in the form of a structure-like class
     */
    private static PreInitOptions parseArgsPreInit(final String[] args) {
        PreInitOptions options = new PreInitOptions();
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--novalidation"))
                options.validateConfiguration = false;
            else if (arg.equalsIgnoreCase("--searchresources"))
                options.searchResources = true;
        }

        if (options.searchResources)
            findWorkingDirectory();

        return options;
    }

    private static void findWorkingDirectory() {
        File root = null;
        String path = "../../";
        try {
            root = new File(path).getCanonicalFile();
            path = root.getCanonicalPath();
        } catch (IOException e) {
            appLogger.fatal("Trying to search in incorrect path.");
            System.exit(-1);
        }

        File configFile = null;
        try {
            configFile = FileSearch.searchForFile(root, "earlywarning.xml");
        } catch (FileNotFoundException e) {
            appLogger.fatal("Could not locate configuration file in '" + path + "'");
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        String workingDir = null;
        try {
            workingDir = configFile.getParentFile().getParentFile().getCanonicalPath() + "/";
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        appLogger.info("Setting working directory to '" + workingDir + "'");
        System.setProperty("user.dir", workingDir);
    }

    /**
     * Parses the command line arguments.
     * This should be called after all initializations, since it may emit triggers or calls.
     *
     * @param args the command line arguments
     * @return the post-initialization settings, in the form of a structure-like class
     */
    private static PostInitOptions parseArgsPostInit(final String[] args) {
        PostInitOptions options = new PostInitOptions();

        for (String arg : args) {
            if (arg.equalsIgnoreCase("--testcalls"))
                options.testCalls = true;
            else if (arg.equalsIgnoreCase("--testcall"))
                options.testCall = true;
        }

        if (options.testCalls) {
            appLogger.info("--testcalls was passed: two triggers will be emitted after 2500ms and 5000ms.");
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException ignored) {
                        appLogger.info("Skipping delay before first trigger...");
                    }
                    appLogger.info("Sending first trigger");
                    TriggerV2Sender3.main(args);
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException ignored) {
                        appLogger.info("Skipping delay before second trigger");
                    }
                    appLogger.info("Sending second trigger...");
                    TriggerV2Sender2.main(args);
                }
            }.start();
        } else if (options.testCall) {
            // Only execute the unique test call if we haven't already executed two other test calls
            appLogger.info("--testcall was passed: a trigger will be emitted after 2500ms.");
            new Thread() {
                public void run() {
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException ignored) {
                        appLogger.info("Skipping delay before trigger");
                    }
                    appLogger.info("Sending trigger...");
                    TriggerV2Sender3.main(args);
                }
            }.start();
        }

        return options;
    }

    /**
     * Reads the XML configuration file (<code>resources/earlywarning.xml</code>) and creates a {@link XMLConfiguration} object.
     * The application logs a fatal error and exits if the configuration file does not exist.
     */
    private static void readConfiguration(boolean validate) {
        try {
            configuration = new XMLConfiguration(buildPath(CONFIGURATION_PATH));
        } catch (ConfigurationException e) {
            appLogger.fatal("Could not read configuration at '" + CONFIGURATION_PATH + "'");
            System.exit(-1);
        }

        configuration.setThrowExceptionOnMissing(true);
        if (validate) {
            ConfigurationValidator validator = new ConfigurationValidator(configuration);
            validator.validate();
        } else
            appLogger.warn("No validation will be made on the configuration file. If it contains a mistake, it will only be noticeable at call time.");
    }

    /**
     * Configures Log4J
     */
    private static void setLogger() {
        PropertyConfigurator.configure(buildPath("resources/log4j.properties"));
    }

    /**
     * Verifies uniqueness of the application
     */
    private static void checkUniqueness() {
        try {
            if (!CommonUtilities.appIsUnique("EarlyWarning")) {
                appLogger.fatal("Application already running: exiting");
                System.exit(1);
            }
        } catch (FileNotFoundException ex) {
            appLogger.warn("Unable to create lock file to ensure uniqueness of the application");
        } catch (IOException ex) {
            appLogger.warn("Unable to set lock file to ensure uniqueness of the application");
        }
    }

    /**
     * Starts the contact list manager Web server.
     */
    private static void startContactsServer() {
        ContactListManagerServer server;
        String home = EarlyWarning.configuration.getString("contacts.home");
        server = new ContactListManagerServer(home);

        try {
            server.startServer();
        } catch (IOException ex) {
            appLogger.fatal("Cannot start ContactServer on port " + server.getPort());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Starts the {@link EarlyWarningThread}.
     */
    private static void startEarlyWarningThread() {
        try {
            Thread earlyWarningThread = EarlyWarningThread.getInstance();
            earlyWarningThread.start();
        } catch (IOException ex) {
            appLogger.fatal("Fatal error: I/O exception: " + ex.getMessage() + ". Exiting application.");
            System.exit(1);
        }
    }

    /**
     * Starts the {@link DataBaseHeartBeatThread}.
     */
    private static void startDataBaseHeartBeatThread() {
        if (configuration.getBoolean("heartbeat.use_heartbeat")) {
            Thread dataBaseHeartBeatThread = DataBaseHeartBeatThread.getInstance();
            dataBaseHeartBeatThread.start();
        }
    }

    private static void startFailoverManager() {
        if (!configuration.getBoolean("failover.is_failover")) {
            int port = configuration.getInt("failover.heartbeat_port");
            HeartbeatServerThread heartbeatServerThread = null;
            try {
                heartbeatServerThread = HeartbeatServerThread.getInstance(port);
            } catch (IOException e) {
                appLogger.fatal("Could not start heartbeat server.");
                System.exit(-1);
            }

            heartbeatServerThread.start();
        } else
            appLogger.info("Current instance is failover, not starting heartbeat server.");
    }

    /**
     * Create GUI
     */
    @SuppressWarnings("EmptyMethod")
    private static void createGui() {
        // TODO: fix this in a future version
        //        try {
        //            FileCallListControler fileCallListControler = new FileCallListControler(defaultContactList, fileCallLists);
        //            fileCallListControler.displayView();
        //        } catch (HeadlessException ignored)
        //        {
        //            appLogger.info("Running in headless mode.");
        //        }
    }

    /**
     * A structure for pre-initialization settings
     */
    private static class PreInitOptions {
        public boolean searchResources = false;
        /**
         * Whether or not the configuration validity should be verified upon reading
         */
        boolean validateConfiguration = true;
    }

    /**
     * A structure for post-initialization settings
     */
    private static class PostInitOptions {
        /**
         * Whether or not two triggers should be emitted
         */
        boolean testCalls = false;
        /**
         * Whether or not a trigger should be emitted (not compatible with <code>testCalls</code>)
         */
        boolean testCall = false;
    }
}
