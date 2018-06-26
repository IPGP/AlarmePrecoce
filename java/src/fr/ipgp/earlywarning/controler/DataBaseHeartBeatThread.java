/*
  Created Fri 07, 2008 11:40:40 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.utilities.CommonUtilities;
import fr.ipgp.earlywarning.utilities.DataBaseHeartBeat;
import org.apache.commons.configuration.ConversionException;

import java.io.IOException;
import java.sql.SQLException;
import java.util.NoSuchElementException;

/**
 * Sends heartbeat represented by an update in the "fonctionnement" database (for OVPF only!)<br/>
 * Implements the singleton pattern.
 *
 * @author Patrice Boissier
 */
@SuppressWarnings("LoopConditionNotUpdatedInsideLoop")
public class DataBaseHeartBeatThread extends Thread {
    private static DataBaseHeartBeatThread uniqueInstance;
    protected final int aliveMessage;
    protected final int startMessage;
    protected final int delay;
    protected final boolean moreHeartBeats = true;
    protected DataBaseHeartBeat dataBaseHeartBeat;

    private DataBaseHeartBeatThread() throws ConversionException, NoSuchElementException {
        this("DataBaseHeartBeatThread");
    }

    private DataBaseHeartBeatThread(String name) throws ConversionException, NoSuchElementException {
        super(name);
        aliveMessage = EarlyWarning.configuration.getInt("heartbeat.num_type_alive");
        startMessage = EarlyWarning.configuration.getInt("heartbeat.num_type_start");
        delay = EarlyWarning.configuration.getInt("heartbeat.hearbeat_delay");
    }

    public static synchronized DataBaseHeartBeatThread getInstance() throws IOException {
        if (uniqueInstance == null) {
            uniqueInstance = new DataBaseHeartBeatThread();
        }
        return uniqueInstance;
    }

    public void run() {
        EarlyWarning.appLogger.debug("DataBase HeartBeat Thread creation");
        // Loading the driver
        try {
            dataBaseHeartBeat = new DataBaseHeartBeat(EarlyWarning.configuration);
        } catch (ClassNotFoundException cnfe) {
            EarlyWarning.appLogger.error("Database driver not found. Database support disabled.");
            EarlyWarning.appLogger.debug("Thread is stopping");
            return;
        } catch (ConversionException ce) {
            EarlyWarning.appLogger.error("An element value has wrong type : check hearbeat or dbms section of earlywarning.xml configuration file. HearBeat notification disabled.");
            EarlyWarning.appLogger.debug("Thread is stopping");
            return;
        } catch (NoSuchElementException | NullPointerException nsee) {
            EarlyWarning.appLogger.error("An element value is undefined : check hearbeat or dbms section of earlywarning.xml configuration file. HearBeat notification disabled.");
            EarlyWarning.appLogger.debug("Thread is stopping");
            return;
        }

        // Notify the start time
        try {
            int result = dataBaseHeartBeat.sendHeartBeat(startMessage, CommonUtilities.dateToISO());
            if (result == 0)
                EarlyWarning.appLogger.error("Start message not sent to the database : update returned 0");
            else
                EarlyWarning.appLogger.debug("Start message sent. Database updated");
        } catch (SQLException sqle) {
            EarlyWarning.appLogger.error("Database connection problem. This could be a network problem or a configuration problem in dbms section of earlywarning.xml. HeartBeat not sent.");
        }

        // HeartBeat notification
        //noinspection InfiniteLoopStatement,LoopConditionNotUpdatedInsideLoop
        while (moreHeartBeats) {
            try {
                int result = dataBaseHeartBeat.sendHeartBeat(aliveMessage, CommonUtilities.dateToISO());
                if (result == 0)
                    EarlyWarning.appLogger.error("HeartBeat not sent : update returned 0");
            } catch (SQLException sqle) {
                EarlyWarning.appLogger.error("Database connection problem. This could be a network problem or a configuration problem in dbms section of earlywarning.xml. HeartBeat not sent.");
            }
            // Sleeping for delay seconds
            try {
                Thread.sleep(1000 * delay);
            } catch (InterruptedException ie) {
                EarlyWarning.appLogger.error("Error while sleeping!");
            }
        }
    }
}
