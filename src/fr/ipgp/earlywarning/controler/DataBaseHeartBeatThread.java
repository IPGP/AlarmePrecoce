/**
 * Created Fri 07, 2008 11:40:40 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import java.io.IOException;
import java.sql.*;
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
	
	public DataBaseHeartBeatThread() throws IOException {
    	this("DataBaseHeartBeatThread");
    }

    public DataBaseHeartBeatThread(String name) throws IOException {
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
			EarlyWarning.appLogger.warn("Database driver not found. Database support disabled.");
			EarlyWarning.appLogger.debug("Thread is stopping");
            return;
		}
		// Notify the start time
		try {
			//TODO change the DATE!!!!!
			int result = dataBaseHeartBeat.sendHeartBeat(startMessage, DateFormater.toISO());
			if (result == 0 )
				EarlyWarning.appLogger.warn("Start message not sent to the database : update returned 0");
			else
				EarlyWarning.appLogger.debug("Start message sent. Database updated");
		} catch (SQLException sqle) {
			EarlyWarning.appLogger.warn("Database connection problem. HeartBeat not sent.");
		}
		
		// HeartBeat notification
		while(moreHeartBeats) {
			try {
				int result = dataBaseHeartBeat.sendHeartBeat(aliveMessage, DateFormater.toISO());
				if (result == 0 )
					EarlyWarning.appLogger.warn("HeartBeat not sent : update returned 0");
				else
					EarlyWarning.appLogger.debug("HeartBeat sent. Database updated");
			} catch (SQLException sqle) {
				EarlyWarning.appLogger.warn("Database connection problem. HeartBeat not sent.");
			}
			try {
				Thread.sleep(1000*delay);
			} catch (InterruptedException ie) {
				EarlyWarning.appLogger.debug("Error while sleeping!");
			}
		}
    }
}
