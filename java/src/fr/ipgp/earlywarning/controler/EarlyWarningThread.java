/**
 * Created Mar 01, 2008 11:01:05 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.commons.configuration.ConversionException;
import fr.ipgp.earlywarning.triggers.*;
import fr.ipgp.earlywarning.*;
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
    protected int port;
	
    public EarlyWarningThread() throws IOException, ConversionException, NoSuchElementException {
    	this("EarlyWarningThread");
    }

    public EarlyWarningThread(String name) throws IOException, ConversionException, NoSuchElementException {
    	super(name);
    	port = EarlyWarning.configuration.getInt("network.port");
    	socket = new DatagramSocket(port);
    	buffer = new byte[256];
    	packet = new DatagramPacket(buffer, buffer.length);
    }
    
    public void run() {
    	EarlyWarning.appLogger.debug("Thread creation");
    	   	
    	QueueManagerThread queueManagerThread = new QueueManagerThread();
    	queueManagerThread.start();
    	
    	// TEST
    	
		Trigger trig1 = new Trigger(CommonUtilities.getUniqueId(),2);
		Trigger trig2 = new Trigger(CommonUtilities.getUniqueId(),2);
		Trigger trig3 = new Trigger(CommonUtilities.getUniqueId(),1);
		Vector<Trigger> vect1 = new Vector<Trigger>();
		Vector<Trigger> vect2 = new Vector<Trigger>();
		Vector<Trigger> vect3 = new Vector<Trigger>();
		vect1.add(trig1);
		vect2.add(trig1);
		vect2.add(trig2);
		vect3.add(trig3);
		vect3.add(trig1);
		vect3.add(trig2);
		queueManagerThread.addTrigger(trig1);
    	System.out.println(queueManagerThread.getQueue().toString());
    	queueManagerThread.addTrigger(trig2);
    	System.out.println(queueManagerThread.getQueue().toString());
    	queueManagerThread.addTrigger(trig3);
    	System.out.println(queueManagerThread.getQueue().toString());
    	//TEST
    	
        while (moreTriggers) {
        	EarlyWarning.appLogger.debug("Waiting for triggers on UDP port " + port);

        	try {
                socket.receive(packet);
            } catch (IOException ioe) {
                EarlyWarning.appLogger.error("Input Output error while receiving datagram");
                ioe.printStackTrace();
            }
            if (Thread.interrupted()) {
            	EarlyWarning.appLogger.warn("Thread stopping");
                return;
            }
            System.out.println(packet.toString());
        }
        socket.close();
    }
}
