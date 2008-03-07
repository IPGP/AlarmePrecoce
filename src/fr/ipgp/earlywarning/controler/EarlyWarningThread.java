/**
 * Created Mar 01, 2008 11:01:05 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import java.io.*;
import java.net.*;
import fr.ipgp.earlywarning.triggers.*;
import fr.ipgp.earlywarning.utilities.*;

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
    protected int port = 4445;
	
    public EarlyWarningThread() throws IOException {
    	this("EarlyWarningThread");
    }

    public EarlyWarningThread(String name) throws IOException {
    	super(name);
    	socket = new DatagramSocket(port);
    	buffer = new byte[256];
    	packet = new DatagramPacket(buffer, buffer.length);
    }
    
    public void run() {
    	System.out.println("Creation du thread EarlyWarningThread");
        while (moreTriggers) {
        	System.out.println("En attente de trigger");

        	try {
                socket.receive(packet);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            if (Thread.interrupted()) {
                return;
            }
            
            //test
            Trigger trigger1 = new Trigger(UniqueID.get(),1);
            Trigger trigger2 = new Trigger(UniqueID.get(),1);
            Trigger trigger3 = new Trigger(UniqueID.get(),1);
            Trigger trigger4 = new Trigger(UniqueID.get(),1);
            int[] test = {trigger1.compareTo(trigger2), trigger2.compareTo(trigger3), trigger3.compareTo(trigger4)};
            String[] triggers = {trigger1.toString(), trigger2.toString(), trigger3.toString(), trigger4.toString()}; 
            for (int i=0; i<3; i++) {
            	if (test[i] > 0) {
            		System.out.println(triggers[i]+" < "+triggers[i+1]);
            	} else {
            		System.out.println(triggers[i]+" > "+triggers[i+1]);
            	}
            }
        }
        socket.close();
    }
}
