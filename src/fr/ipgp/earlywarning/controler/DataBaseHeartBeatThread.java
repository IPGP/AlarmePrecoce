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
	protected boolean moreHeartBeats = true;
	
	public DataBaseHeartBeatThread() throws IOException {
    	this("DataBaseHeartBeatThread");
    }

    public DataBaseHeartBeatThread(String name) throws IOException {
    	super(name);    	
    }
    
    public void run() {
    	EarlyWarning.appLogger.debug("Thread creation");
    	try {
			dataBaseHeartBeat = new DataBaseHeartBeat();
		} catch (ClassNotFoundException cnfe) {
			EarlyWarning.appLogger.warn("Database driver not found. Database support disabled.");
			EarlyWarning.appLogger.debug("Thread is stopping");
            return;
		}
		while(moreHeartBeats) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException ie) {
				EarlyWarning.appLogger.debug("Error while sleeping!");
			}
			try {
				dataBaseHeartBeat.sendHeartBeat(0, "2008-03-10 00:00:00");
			} catch (SQLException sqle) {
				EarlyWarning.appLogger.warn("Database connection problem. HeartBeat not sent.");
			}
		}
    }
}
