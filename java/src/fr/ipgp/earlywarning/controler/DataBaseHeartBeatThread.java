/**
 * Created Fri 07, 2008 11:40:40 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import java.io.IOException;

import fr.ipgp.earlywarning.EarlyWarning;

/**
 * Sends heartbeat represented by an update in the "fonctionnement" database (for OVPF only!)
 * @author Patrice Boissier
 *
 */
public class DataBaseHeartBeatThread extends Thread {
	protected String host;
	protected int port;
	protected String database;
	protected String user;
	protected String password;
	
	public DataBaseHeartBeatThread() throws IOException {
    	this("DataBaseHeartBeatThread");
    }

    public DataBaseHeartBeatThread(String name) throws IOException {
    	super(name);
    	host = new String(EarlyWarning.configuration.getString("dbms.host"));
    	port = EarlyWarning.configuration.getInt("dbms.port");
    	database = new String(EarlyWarning.configuration.getString("dbms.database"));
    	user = new String(EarlyWarning.configuration.getString("dbms.user"));
    	password = new String(EarlyWarning.configuration.getString("dbms.paassword"));
    }

}
