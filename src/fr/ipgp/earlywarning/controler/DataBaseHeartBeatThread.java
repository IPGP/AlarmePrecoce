/**
 * Created Fri 07, 2008 11:40:40 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import java.io.IOException;
import org.apache.commons.configuration.*;
import java.sql.*;
import java.util.*;
import fr.ipgp.earlywarning.utilities.*;
import fr.ipgp.earlywarning.*;

/**
 * Sends heartbeat represented by an update in the "fonctionnement" database (for OVPF only!)
 * @author Patrice Boissier
 *
 */
public class DataBaseHeartBeatThread extends Thread {
	protected DataBaseHeartBeat dataBaseHeartBeat;
	protected int aliveMessage;
	protected int startMessage;
	protected int delay;
	protected boolean moreHeartBeats = true;
	
	public DataBaseHeartBeatThread() throws IOException, ConversionException, NoSuchElementException {
    	this("DataBaseHeartBeatThread");
    }

    public DataBaseHeartBeatThread(String name) throws IOException, ConversionException, NoSuchElementException {
    	super(name);
    	aliveMessage = EarlyWarning.configuration.getInt("heartbeat.num_type_alive");
    	startMessage = EarlyWarning.configuration.getInt("heartbeat.num_type_start");
    	delay = EarlyWarning.configuration.getInt("heartbeat.hearbeat_delay");
    }
    
    public void run() {
    	EarlyWarning.appLogger.debug("Thread creation");
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
		} catch (NoSuchElementException nsee) {
			EarlyWarning.appLogger.error("An element value is undefined : check hearbeat or dbms section of earlywarning.xml configuration file. HearBeat notification disabled.");
			EarlyWarning.appLogger.debug("Thread is stopping");
            return;
		} catch (NullPointerException npe) {
			EarlyWarning.appLogger.error("An element value is undefined : check hearbeat or dbms section of earlywarning.xml configuration file. HearBeat notification disabled.");
			EarlyWarning.appLogger.debug("Thread is stopping");
            return;			
		}
		// Notify the start time
		try {
			int result = dataBaseHeartBeat.sendHeartBeat(startMessage, CommonUtilities.dateToISO());
			if (result == 0 )
				EarlyWarning.appLogger.error("Start message not sent to the database : update returned 0");
			else
				EarlyWarning.appLogger.debug("Start message sent. Database updated");
		} catch (SQLException sqle) {
			EarlyWarning.appLogger.error("Database connection problem. This could be a network problem or a configuration problem in dbms section of earlywarning.xml. HeartBeat not sent.");
		}
		
		// HeartBeat notification
		while(moreHeartBeats) {
			try {
				int result = dataBaseHeartBeat.sendHeartBeat(aliveMessage, CommonUtilities.dateToISO());
				if (result == 0 )
					EarlyWarning.appLogger.error("HeartBeat not sent : update returned 0");
				else
					EarlyWarning.appLogger.debug("HeartBeat sent. Database updated");
			} catch (SQLException sqle) {
				EarlyWarning.appLogger.error("Database connection problem. This could be a network problem or a configuration problem in dbms section of earlywarning.xml. HeartBeat not sent.");
			}
			// Sleeping for delay seconds
			try {
				Thread.sleep(1000*delay);
			} catch (InterruptedException ie) {
				EarlyWarning.appLogger.error("Error while sleeping!");
			}
		}
    }
}
