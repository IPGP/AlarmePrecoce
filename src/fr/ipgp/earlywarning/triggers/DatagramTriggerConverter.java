/**
 * Created Mar 13, 2008 11:07:36 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.triggers;

import java.net.*;
import fr.ipgp.earlywarning.*;
import fr.ipgp.earlywarning.utilities.*;
import fr.ipgp.earlywarning.messages.*;
import fr.ipgp.earlywarning.telephones.*;
/**
 * @author Patrice Boissier
 *
 */
public class DatagramTriggerConverter implements TriggerConverter {
    protected DatagramPacket packet = null;
    protected InetAddress senderAddress;
    protected int senderPort;
    protected Trigger trigger;
    
    public DatagramTriggerConverter(DatagramPacket packet) {
    	this.packet = packet;                
    	senderAddress=packet.getAddress();
    	senderPort=packet.getPort();
    	String received = new String(packet.getData());
    	trigger = new Trigger(CommonUtilities.getUniqueId(),1);
    	trigger.setInetAddress(senderAddress);
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
     * @return true if the message was decoded successfully 
     */
    public boolean decode(String received) {
    	String[] receivedSplit = received.split(" ");
    	int version;
    	
    	if (receivedSplit[0].matches("\\d\\d"))
    		version = Integer.parseInt(receivedSplit[0]);
    	else {
/*    		if(receivedSplit[0].equals("Sismo") 
    				&& receivedSplit[1].matches("\\d\\d/\\d\\d/\\d\\d\\d\\d")
    				&& receivedSplit[2].matches("\\d\\d:\\d\\d:\\d\\d")
    				&& receivedSplit.length == 4)*/
    		if(receivedSplit[0].equals("Sismo") 
    				&& CommonUtilities.isDate(receivedSplit[1] + " " + receivedSplit[2], "dd/MM/yyyy HH:mm:ss")
    				&& receivedSplit.length > 3)
    			version = 1;
    		else
    			version = 0;
    	}
    	
    	switch(version)
        {
            case 1:
                return decodeV1(receivedSplit);
            case 2:
                return decodeV2(receivedSplit);
            default:
            	EarlyWarning.appLogger.error("Unknown version : " + receivedSplit[0]);
            	System.out.println("Unknown version");
            	return false;
        }
    }
    
    /**
     * Decode the old OVPF format : type 01
     * Sismo dd/MM/yyyy HH:mm:ss Declenchement
     * @param elements the elements of the received message 
     * @return true if the decoding was successful else false
     */
    public boolean decodeV1(String[] elements) {
    	boolean validFormat = true;
		trigger.setApplication(elements[0]);
    	trigger.setCallList(new TextCallList("default"));
    	trigger.setMessage(new TextWarningMessage(elements[3]));
    	trigger.setType("01");
    	trigger.setDate(elements[1] + " " + elements[2]);
    	trigger.setRepeat(true);
		System.out.println("valid v1 format");
    	return validFormat;
    }
    
    /**
     * Decode version 2 messages.<br/>
     * <b>Format : </b><br/>
     * vv p yyyy/MM/dd HH:mm:ss application calllist repeat message<br/>
     * vv : version, two digit<br/>
     * p : priority, one digit<br/>
     * yyyy/MM/dd HH:mm:ss : date, ISO format<br/>
     * application : application name, [a-zA-Z_0-9]*<br/>
     * calllist : call list, either a comma separated list of digits or a .csv file name ([a-zA-Z_0-9]*\.csv)<br/>
     * repeat : true or false<br/>
     * message : warning message, either a message or a .wav file ([a-zA-Z_0-9]*\.wav)
     * @param elements the elements of the received message
     * @return true if the decoding was successful else false
     */
    public boolean decodeV2(String[] elements) {
    	if (elements[1].matches("\\d")
    			&& CommonUtilities.isDate(elements[2] + " " + elements[3], "yyyy/MM/dd HH:mm:ss")
    			&& elements[4].matches("\\w*")
    			&& (elements[5].matches("\\w*\\.csv") || elements[5].matches("(\\d*)(,\\d*)+"))
    			&& (elements[6].equals("true") || elements[6].equals("false"))
    			&& (elements[7].matches("\\w*\\.wav") || elements[7].matches(".+"))){
    		System.out.println("valid v2 format");
    		return true;
    	} else
    		return false;     	
    }
}
