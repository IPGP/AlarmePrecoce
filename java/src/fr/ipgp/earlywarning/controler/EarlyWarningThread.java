/**
 * Created Mar 01, 2008 11:01:05 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.commons.configuration.*;
import fr.ipgp.earlywarning.*;
import fr.ipgp.earlywarning.messages.*;
import fr.ipgp.earlywarning.telephones.*;
import fr.ipgp.earlywarning.triggers.*;
import fr.ipgp.earlywarning.utilities.*;
/**
 * Thread that listen for incoming triggers from the network.<br/>
 * When a trigger arrives, it is passed to the queue manager.<br/>
 * Implements the singleton pattern.
 * @author Patrice Boissier
 */
public class EarlyWarningThread extends Thread {
	private static EarlyWarningThread uniqueInstance;
	private QueueManagerThread queueManagerThread;
	private FileCallList defaultCallList;
    protected DatagramSocket socket = null;
    protected DatagramPacket packet = null;
    protected boolean moreTriggers = true;
    protected byte[] buffer = new byte[512];
    protected int port;
    protected boolean triggerOnError;
	private FileWarningMessage defaultWarningMessage = null;
	private boolean defaultRepeat = true;
	private String defaultConfirmCode = null;
	private int defaultPriority=1;
	
    private EarlyWarningThread() throws IOException, ConversionException, NoSuchElementException {
    	this("EarlyWarningThread");
    }

    private EarlyWarningThread(String name) throws IOException, ConversionException, NoSuchElementException {
    	super(name);
    	port = EarlyWarning.configuration.getInt("network.port");
    	triggerOnError = EarlyWarning.configuration.getBoolean("triggers.create_trigger_on_errors");
    	socket = new DatagramSocket(port);
    	packet = new DatagramPacket(buffer, buffer.length);
    }
    
    public static synchronized EarlyWarningThread getInstance(FileCallList defaultCallList) throws IOException, ConversionException, NoSuchElementException {
    	if (uniqueInstance == null) {
    		uniqueInstance = new EarlyWarningThread();
    	}
    	uniqueInstance.defaultCallList = defaultCallList;
    	return uniqueInstance;
    }
    
    public void run() {
    	EarlyWarning.appLogger.debug("Thread creation");
    	
    	configureThread();

    	queueManagerThread = QueueManagerThread.getInstance(defaultWarningMessage);
    	queueManagerThread.start();

    	EarlyWarning.appLogger.debug("Waiting for triggers on UDP port " + port);

        while (moreTriggers) {

        	try {
                socket.receive(packet);
                EarlyWarning.appLogger.info("Received a packet");
                DatagramTriggerConverter datagramTriggerConverter = new DatagramTriggerConverter(packet, defaultCallList, defaultWarningMessage, defaultRepeat, defaultConfirmCode, defaultPriority);
                datagramTriggerConverter.decode();
                Trigger trigger = datagramTriggerConverter.getTrigger();
                queueManagerThread.addTrigger(trigger);
                
                //EarlyWarning.appLogger.info("A new trigger has been added to the queue : " + trigger.showTrigger());
                EarlyWarning.appLogger.info("A new trigger has been added to the queue");
                EarlyWarning.appLogger.debug("QueueManager : " + queueManagerThread.toString());
            } catch (IOException ioe) {
                EarlyWarning.appLogger.error("Input Output error while receiving datagram : " + ioe.getMessage());
                addErrorTrigger("Input Output error while receiving datagram : " + ioe.getMessage());
                if (socket.isClosed()) {
                	try {
                		socket  = new DatagramSocket(port);
                	} catch (SocketException se) {
                		EarlyWarning.appLogger.error("Socket Exception : cannot open socket : " + se.getMessage());
                	}
        		}
            } catch (UnknownTriggerFormatException utfe) {
            	//EarlyWarning.appLogger.error("Unknown trigger format received : " + utfe.getMessage());
            	//EarlyWarning.appLogger.debug("Unknown trigger format received");
            	addErrorTrigger("Unknown trigger format received : " + utfe.getMessage());
            } catch (InvalidTriggerFieldException itfe) {
            	//EarlyWarning.appLogger.error("Invalid field(s) in the received trigger : " + itfe.getMessage());
            	EarlyWarning.appLogger.error("Invalid field(s) in the received trigger");
            	addErrorTrigger("Invalid field(s) in the received trigger : " + itfe.getMessage());
            } catch (MissingTriggerFieldException mtfe) {
            	//EarlyWarning.appLogger.error("Missing field(s) in the received trigger : " + mtfe.getMessage());
            	EarlyWarning.appLogger.error("Missing field(s) in the received trigger");
            	addErrorTrigger("Missing field(s) in the received trigger : " + mtfe.getMessage());
            } catch (InvalidFileNameException ifne) {
            	//EarlyWarning.appLogger.error("Invalid call list in the received trigger : " + ifne.getMessage());
            	EarlyWarning.appLogger.error("Invalid call list in the received trigger");
            	addErrorTrigger("Invalid call list in the received trigger : " + ifne.getMessage());
            }
            
            if (Thread.interrupted()) {
            	EarlyWarning.appLogger.warn("Thread stopping");
                return;
            }
        }
        socket.close();
    } 
    
    /**
     * Create a custom error trigger based on the error message.
     * @param errorMessage
     */
    private Trigger createErrorTrigger (String errorMessage) {
    	try {  	
    		long id = CommonUtilities.getUniqueId();
			int priority = EarlyWarning.configuration.getInt("triggers.defaults.priority");
			boolean supportText2Speech = EarlyWarning.configuration.getBoolean("gateway.text_to_speech");
			WarningMessage message;
			if (supportText2Speech)
				message = new TextWarningMessage(errorMessage);
			else
				message = new FileWarningMessage(EarlyWarning.configuration.getString("gateway.defaults.error_message"));
			String application = new String("EarlyWarning");
			String type = new String("v2");
			boolean repeat = EarlyWarning.configuration.getBoolean("triggers.defaults.repeat");
			Date date1 = new Date();
			SimpleDateFormat  simpleFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
			String date = simpleFormat.format(date1);
			String confirmCode = new String(EarlyWarning.configuration.getString("triggers.defaults.confirm_code"));
	    	Trigger trig = new Trigger(id, priority);
			trig.setApplication(application);
			trig.setCallList(defaultCallList);
			InetAddress inetAddress = InetAddress.getByName("localhost");
			trig.setInetAddress(inetAddress);
			trig.setMessage(message);
			trig.setPriority(priority);
			trig.setType(type);
			trig.setRepeat(repeat);
			trig.setDate(date);
			trig.setConfirmCode(confirmCode);
			return trig;
		} catch (UnknownHostException uh) {
			EarlyWarning.appLogger.error("localhost unknown : check hosts file : " + uh.getMessage());
			return null;
		} catch (ConversionException ce) {
			EarlyWarning.appLogger.error("Error : an element value has wrong type : check trigger section of earlywarning.xml configuration file. Trigger not sent.");
			return null;
		} catch (NoSuchElementException nsee) {
			EarlyWarning.appLogger.error("Error : An element value is undefined : check trigger section of earlywarning.xml configuration file. Trigger not sent.");
			return null;
        }
    }
    
    /**
     * Adds an error trigger to the queue manager
     * @param errorMessage the error message to be delivered
     */
    private void addErrorTrigger (String errorMessage) {
    	if (triggerOnError) {
        	Trigger trig = createErrorTrigger(errorMessage);
        	if (!(trig == null)) {
        		queueManagerThread.addTrigger(trig);
        		EarlyWarning.appLogger.info("A new trigger has been added to the queue : " + trig.showTrigger());
        	}
        }
    }
    
    /**
     * Configure the thread, based on the configuration file.
     */
    private void configureThread() {
     	try {
    		defaultWarningMessage = new FileWarningMessage(EarlyWarning.configuration.getString("gateway.defaults.warning_message"));
    		defaultRepeat = EarlyWarning.configuration.getBoolean("triggers.defaults.repeat");
    		defaultConfirmCode = EarlyWarning.configuration.getString("triggers.defaults.confirm_code");
    		defaultPriority = EarlyWarning.configuration.getInt("triggers.defaults.priority");
    	} catch (ConversionException ce) {
        	EarlyWarning.appLogger.fatal("Default call list, warning message, repeat or confirm code has a wrong value in configuration file : check triggers.defaults section of earlywarning.xml configuration file. Exiting application.");
        	System.exit(-1);
        } catch (NoSuchElementException nsee) {
        	EarlyWarning.appLogger.fatal("Default call list, warning message, repeat or confirm code is missing in configuration file : check triggers.defaults section of earlywarning.xml configuration file. Exiting application.");
        	System.exit(-1);
        }
    }
}
