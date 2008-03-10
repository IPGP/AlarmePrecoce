/**
 * Created Fri 07, 2008 11:40:40 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import java.io.IOException;
import fr.ipgp.earlywarning.utilities.*;

/**
 * Sends heartbeat represented by an update in the "fonctionnement" database (for OVPF only!)
 * @author Patrice Boissier
 *
 */
public class DataBaseHeartBeatThread extends Thread {
	protected DataBaseHeartBeat dataBaseHeartBeat;
	
	public DataBaseHeartBeatThread() throws IOException {
    	this("DataBaseHeartBeatThread");
    }

    public DataBaseHeartBeatThread(String name) throws IOException {
    	super(name);    	
    }

}
