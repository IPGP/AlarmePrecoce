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
    protected byte[] buffer = new byte[512];
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
                System.out.println("Content : " + new String(packet.getData()));
                datagramTriggerConverter.decode();
                Trigger trigger = datagramTriggerConverter.getTrigger();
                queueManagerThread.addTrigger(trigger);
                EarlyWarning.appLogger.info("A new trigger has been added to the queue : " + trigger.toString());
                EarlyWarning.appLogger.debug("QueueManager : " + queueManagerThread.toString());
            } catch (IOException ioe) {
                EarlyWarning.appLogger.error("Input Output error while receiving datagram");
            } catch (UnknownTriggerFormatException utfe) {
            	EarlyWarning.appLogger.error("Unknown trigger format received : " + utfe.getMessage());
            } catch (InvalidTriggerFieldException itfe) {
            	EarlyWarning.appLogger.error("Invalid field(s) in the received trigger : " + itfe.getMessage());
            } catch (MissingTriggerFieldException mtfe) {
            	EarlyWarning.appLogger.error("Missing field(s) in the received trigger : " + mtfe.getMessage());
            } 
            if (Thread.interrupted()) {
            	EarlyWarning.appLogger.warn("Thread stopping");
                return;
            }
        }
        socket.close();
    }
}
