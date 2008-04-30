/**
 * Created Mar 01, 2008 11:01:05 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.apache.commons.configuration.ConversionException;
import fr.ipgp.earlywarning.*;
import fr.ipgp.earlywarning.gateway.*;
import fr.ipgp.earlywarning.messages.*;
import fr.ipgp.earlywarning.telephones.*;
import fr.ipgp.earlywarning.triggers.*;
import fr.ipgp.earlywarning.utilities.CommonUtilities;
/**
 * @author Patrice Boissier
 * Thread that listen for incoming triggers from the network.
 * When a trigger arrives, it is passed to the queue manager.
 */
public class EarlyWarningThread extends Thread {
	private static EarlyWarningThread uniqueInstance;
	private QueueManagerThread queueManagerThread;
    protected DatagramSocket socket = null;
    protected DatagramPacket packet = null;
    protected boolean moreTriggers = true;
    protected byte[] buffer = new byte[512];
    protected int port;
    protected boolean triggerOnError;
	
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
    
    public static synchronized EarlyWarningThread getInstance() throws IOException, ConversionException, NoSuchElementException {
    	if (uniqueInstance == null) {
    		uniqueInstance = new EarlyWarningThread();
    	}
    	return uniqueInstance;
    }
    
    public void run() {
    	EarlyWarning.appLogger.debug("Thread creation");
    	
    	//TEST VOICENT GATEWAY
//    	
//    	VoicentGateway voicentGateway = VoicentGateway.getInstance("195.83.188.145", 8155);
//
//    	//String [] phoneNumbers = {"0262275826", "0692703856", "0692703856", "0262275826", "0692703856", "0692703856"};
//    	String [] phoneNumbers = {"0262275826"};
//    	
//    	voicentGateway.callTillConfirm("C:/Program Files/Voicent/BroadcastByPhone/bin/vcast.exe","C:/temp/log.voc","C:/temp/voicent.wav", "11");
//                "C:/mygroup/log.voc",
//                "C:/mygroup/hello.wav",
//                "11", phoneNumbers);
//    	
//    	String id = voicentGateway.callText("0692703856", "This is a test alert", false);
//    	while (voicentGateway.callStatus(id).equals("Call in progress")) {
//    		try {
//    			Thread.sleep(3000);
//    			System.out.println(voicentGateway.callStatus(id) + " " + id);
//			} catch (InterruptedException ie) {
//				EarlyWarning.appLogger.error("Error while sleeping!");
//    		}
//    	}
//    	System.out.println(voicentGateway.callStatus(id) + " " + id);
//    	System.out.println(voicentGateway.callRemove(id) + " " + id);
    	//TODO VŽrifier la sortie de callRemove
    	//TODO VŽrifier la date de sendTrigger.pl
    	
    	// END OF TEST
    	
    	CallList defaultCallList = null;
    	WarningMessage defaultWarningMessage = null;
    	boolean defaultRepeat = true;
    	String defaultConfirmCode = null;
    	int defaultPriority=1;
    	
    	try {
    		defaultCallList = new FileReferenceCallList(EarlyWarning.configuration.getString("gateway.defaults.txt_call_list"));
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
        } catch (InvalidFileNameException ifne) {
        	EarlyWarning.appLogger.fatal("Default call list has an invalid name (*.txt or *.voc) in configuration file : check triggers.defaults section of earlywarning.xml configuration file. Exiting application.");
        	System.exit(-1);
        }
    	
    	queueManagerThread = QueueManagerThread.getInstance();
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
                EarlyWarning.appLogger.info("A new trigger has been added to the queue : " + trigger.showTrigger());
                EarlyWarning.appLogger.debug("QueueManager : " + queueManagerThread.toString());
            } catch (IOException ioe) {
                EarlyWarning.appLogger.error("Input Output error while receiving datagram");
                addErrorTrigger("Input Output error while receiving datagram");
            } catch (UnknownTriggerFormatException utfe) {
            	EarlyWarning.appLogger.error("Unknown trigger format received : " + utfe.getMessage());
            	addErrorTrigger("Unknown trigger format received : " + utfe.getMessage());
            } catch (InvalidTriggerFieldException itfe) {
            	EarlyWarning.appLogger.error("Invalid field(s) in the received trigger : " + itfe.getMessage());
            	addErrorTrigger("Invalid field(s) in the received trigger : " + itfe.getMessage());
            } catch (MissingTriggerFieldException mtfe) {
            	EarlyWarning.appLogger.error("Missing field(s) in the received trigger : " + mtfe.getMessage());
            	addErrorTrigger("Missing field(s) in the received trigger : " + mtfe.getMessage());
            } catch (InvalidFileNameException ifne) {
            	EarlyWarning.appLogger.error("Invalid call list in the received trigger : " + ifne.getMessage());
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
			CallList callList = new FileReferenceCallList(EarlyWarning.configuration.getString("gateway.defaults.txt_call_list"));
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
			trig.setCallList(callList);
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
			EarlyWarning.appLogger.error("localhost unknown : check hosts file");
			return null;
		} catch (ConversionException ce) {
			EarlyWarning.appLogger.error("Error : an element value has wrong type : check trigger section of earlywarning.xml configuration file. Trigger not sent.");
			return null;
		} catch (NoSuchElementException nsee) {
			EarlyWarning.appLogger.error("Error : An element value is undefined : check trigger section of earlywarning.xml configuration file. Trigger not sent.");
			return null;
        } catch (InvalidFileNameException ifne) {
        	EarlyWarning.appLogger.fatal("Invalid default call list. Check gateway section of earlywarning.xml configuration file. Trigger not sent.");
        	return null;
        }
    }
    
    private void addErrorTrigger (String errorMessage) {
    	if (triggerOnError) {
        	Trigger trig = createErrorTrigger(errorMessage);
        	if (!(trig == null)) {
        		queueManagerThread.addTrigger(trig);
        		EarlyWarning.appLogger.info("A new trigger has been added to the queue : " + trig.showTrigger());
        	}
        }
    }
}
