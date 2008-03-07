/**
 * Created Mar 01, 2008 11:01:05 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import java.io.*;
import java.net.*;
import fr.ipgp.earlywarning.*;

/**
 * @author Patrice Boissier
 * Thread that listen for incoming triggers from the network.
 * When a trigger arrives, it is passed to the queue manager.
 */
public class EarlyWarningThread extends Thread {
	
    protected DatagramSocket socket = null;
    protected DatagramPacket packet = null;
    protected boolean moreTriggers = true;
    protected byte[] buffer = null;
    protected int port;
	
    public EarlyWarningThread() throws IOException {
    	this("EarlyWarningThread");
    }

    public EarlyWarningThread(String name) throws IOException {
    	super(name);
    	port = EarlyWarning.configuration.getInt("network.port");
    	socket = new DatagramSocket(port);
    	buffer = new byte[256];
    	packet = new DatagramPacket(buffer, buffer.length);
    }
    
    public void run() {
    	EarlyWarning.appLogger.debug("Creation du thread EarlyWarningThread");
        while (moreTriggers) {
        	EarlyWarning.appLogger.debug("En attente de trigger sur le port " + port);

        	try {
                socket.receive(packet);
            } catch (IOException ioe) {
                EarlyWarning.appLogger.error("Erreur d'entree sortie lors de la reception d'un datagramme");
                ioe.printStackTrace();
            }
            if (Thread.interrupted()) {
            	EarlyWarning.appLogger.warn("Arret du thread");
                return;
            }
            System.out.println(packet.toString());
        }
        socket.close();
    }
}
