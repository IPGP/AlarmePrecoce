/**
 * Created Mar 01, 2008 11:01:05 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import java.io.*;
import java.net.*;
import java.util.*;
import org.apache.commons.configuration.ConversionException;
import fr.ipgp.earlywarning.*;
import fr.ipgp.earlywarning.triggers.*;

/**
 * @author Patrice Boissier
 * Thread that listen for incoming triggers from the network.
 * When a trigger arrives, it is passed to the queue manager.
 */
public class EarlyWarningThread extends Thread {
	
    protected DatagramSocket socket = null;
    protected DatagramPacket packet = null;
    protected boolean moreTriggers = true;
    protected byte[] buffer = new byte[65535];
    protected int port;
	
    public EarlyWarningThread() throws IOException, ConversionException, NoSuchElementException {
    	this("EarlyWarningThread");
    }

    public EarlyWarningThread(String name) throws IOException, ConversionException, NoSuchElementException {
    	super(name);
    	port = EarlyWarning.configuration.getInt("network.port");
    	socket = new DatagramSocket(port);
    	packet = new DatagramPacket(buffer, buffer.length);
    }
    
    public void run() {
    	EarlyWarning.appLogger.debug("Thread creation");
    	   	
    	QueueManagerThread queueManagerThread = new QueueManagerThread();
    	queueManagerThread.start();

    	EarlyWarning.appLogger.debug("Waiting for triggers on UDP port " + port);

        while (moreTriggers) {

        	try {
                socket.receive(packet);
                EarlyWarning.appLogger.info("Received a packet");
                DatagramTriggerConverter datagramTriggerConverter = new DatagramTriggerConverter(packet);
                Trigger trigger = datagramTriggerConverter.getTrigger();
                queueManagerThread.addTrigger(trigger);
                EarlyWarning.appLogger.info("A new trigger has been added to the queue : "+trigger.toString());
            } catch (IOException ioe) {
                EarlyWarning.appLogger.error("Input Output error while receiving datagram");
            }
            if (Thread.interrupted()) {
            	EarlyWarning.appLogger.warn("Thread stopping");
                return;
            }
        }
        socket.close();
    }
}
