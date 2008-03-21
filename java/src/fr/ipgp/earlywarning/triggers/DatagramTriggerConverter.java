/**
 * Created Mar 13, 2008 11:07:36 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.triggers;

import java.net.*;
import java.io.*;
import java.util.regex.*;
import fr.ipgp.earlywarning.utilities.*;
import fr.ipgp.earlywarning.messages.*;
import fr.ipgp.earlywarning.telephones.*;
/**
 * @author Patrice Boissier
 * Create a trigger object from a datagram packet
 */
public class DatagramTriggerConverter implements TriggerConverter {
    protected DatagramPacket packet = null;
    protected InetAddress senderAddress;
    protected int senderPort;
    protected Trigger trigger;
    protected String packetContent;
    
    public DatagramTriggerConverter(DatagramPacket packet) {
    	this.packet = packet;                
    	this.senderAddress=packet.getAddress();
    	this.senderPort=packet.getPort();
    	this.packetContent = new String(packet.getData());
    	this.trigger = new Trigger(CommonUtilities.getUniqueId(),1);
    	this.trigger.setInetAddress(senderAddress);
    }
    
    /**
     * @return trigger the trigger to get
     */
    public Trigger getTrigger() {
    	return trigger;
    }
    
    /**
     * Decode the properties of the received message from the DatagramPacket.
     * Set the Trigger attributes.
     * @param received the received message of the DatagramPacket
     * @throws UnknownTriggerFormatException, InvalidTriggerFieldException, MissingTriggerFieldException
     */
    public void decode() throws UnknownTriggerFormatException, InvalidTriggerFieldException, MissingTriggerFieldException {
    	String[] packetContentSplit = this.packetContent.split(" ");
    	int version;
    	
    	if (packetContentSplit[0].matches("\\d\\d"))
    		version = Integer.parseInt(packetContentSplit[0]);
    	else {
    		if(packetContentSplit[0].equals("Sismo")) 
    			version = 1;
    		else {
    			throw new UnknownTriggerFormatException("Unknown version : " + packetContentSplit[0]);
    		}
    	}
    	
    	switch(version)
        {
            case 1:
                decodeV1(packetContentSplit);
                break;
            case 2:
                decodeV2(packetContentSplit);
                break;
            default:
            	throw new UnknownTriggerFormatException("Unknown version : " + packetContentSplit[0]);
        }
    }
    
    /**
     * Decode the old OVPF format : type 01
     * Sismo dd/MM/yyyy HH:mm:ss Declenchement
     * @param elements the elements of the received message 
     * @throws InvalidTriggerFieldException
     */
    private void decodeV1(String[] packetContentElements) throws InvalidTriggerFieldException{
    	if (CommonUtilities.isDate(packetContentElements[1] + " " + packetContentElements[2], "dd/MM/yyyy HH:mm:ss") 
    			&& packetContentElements.length > 3) {
    		trigger.setApplication(packetContentElements[0]);
    		trigger.setCallList(new FileCallList(new File("default.csv")));
	    	trigger.setMessage(new TextWarningMessage(packetContentElements[3]));
	    	trigger.setType("01");
	    	trigger.setDate(packetContentElements[1] + " " + packetContentElements[2]);
	    	trigger.setRepeat(true);
	    	trigger.setConfirmCode("11");
	    	System.out.println("valid v1 format");
    	} else 
        	throw new InvalidTriggerFieldException ("Invalid V1 trigger fields : " + this.packetContent);  		
    }
    
    /**
     * Decode version 2 messages.<br/>
     * <b>Format : </b><br/>
     * vv p yyyy/MM/dd HH:mm:ss application calllist repeat confirmcode message<br/>
     * vv : version, two digit<br/>
     * p : priority, one digit<br/>
     * yyyy/MM/dd HH:mm:ss : date, ISO format<br/>
     * application : application name, [a-zA-Z_0-9]*<br/>
     * calllist : call list, either a comma separated list of digits or a .csv file name ([a-zA-Z_0-9]*\.csv)<br/>
     * repeat : true or false<br/>
     * confirmcode : confirmation code, a digit sequence (1 to 6 digits)
     * message : warning message, either text message encapsulated between two "pipes" (|) or a .wav file ([a-zA-Z_0-9]*\.wav)
     * @param elements the elements of the received message
     * @return true if the decoding was successful else false
     */
    private void decodeV2(String[] packetContentElements) throws InvalidTriggerFieldException, MissingTriggerFieldException {
    	String warningMessage = new String();
    	boolean first = true;
    	if (packetContentElements.length>8) {
    		for (int j=8; j<packetContentElements.length; j++) {
    			if (first) {
    				warningMessage = packetContentElements[j];
    				first = false;
    			} else
    				warningMessage = warningMessage + " " + packetContentElements[j];
    		}
    	} else 
    		throw new MissingTriggerFieldException ("Not enough fields for a V2 trigger : " + this.packetContent);
    	
    	if (!packetContentElements[1].matches("\\d"))
    		throw new InvalidTriggerFieldException ("Invalid V2 trigger field(s) : " + packetContentElements[1]);
    	if (!CommonUtilities.isDate(packetContentElements[2] + " " + packetContentElements[3], "yyyy/MM/dd HH:mm:ss"))
    		throw new InvalidTriggerFieldException ("Invalid V2 trigger field(s) : " + packetContentElements[2] + " " + packetContentElements[3]);
    	if (!packetContentElements[4].matches("\\w*"))
    		throw new InvalidTriggerFieldException ("Invalid V2 trigger field(s) : " + packetContentElements[4]);
    	if (!(packetContentElements[6].equals("true") || packetContentElements[6].equals("false")))
    		throw new InvalidTriggerFieldException ("Invalid V2 trigger field(s) : " + packetContentElements[6]);
    	if (!packetContentElements[7].matches("\\d+") || packetContentElements[7].length() > 7)
    		throw new InvalidTriggerFieldException ("Invalid V2 trigger field(s) : " + packetContentElements[7]);
    	if (packetContentElements[5].matches("\\w+\\.csv"))
    		trigger.setCallList(new FileCallList(new File(packetContentElements[5])));
    	else {
    		if (packetContentElements[5].matches("(\\d*)(,\\d*)*"))
    			trigger.setCallList(new TextCallList(packetContentElements[5]));
    		else
    			throw new InvalidTriggerFieldException ("Invalid V2 trigger field(s) : " + packetContentElements[5]);
    	}
    	if (warningMessage.matches("\\w+\\.wav"))
    		trigger.setMessage(new FileWarningMessage(new File(warningMessage)));
    	else {
    		if (warningMessage.matches("\\|[\\w\\s!\\?,\\.'\\u00C0-\\u00FF]*\\|"))
    			trigger.setMessage(new TextWarningMessage(warningMessage));
    		else
    			throw new InvalidTriggerFieldException ("Invalid V2 trigger field(s) : " + warningMessage);
    	}
    	trigger.setApplication(packetContentElements[4]);
    	trigger.setType(packetContentElements[0]);
    	trigger.setPriority(Integer.parseInt(packetContentElements[1]));
    	trigger.setDate(packetContentElements[2] + " " + packetContentElements[3]);
    	trigger.setRepeat(Boolean.parseBoolean(packetContentElements[6]));
    	trigger.setConfirmCode(packetContentElements[7]);
    	System.out.println("valid v2 format");
    }
}
