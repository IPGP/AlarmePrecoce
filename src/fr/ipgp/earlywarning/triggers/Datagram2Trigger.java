/**
 * Created Mar 13, 2008 11:07:36 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.triggers;

import java.util.*;
import java.net.*;
import fr.ipgp.earlywarning.*;
import fr.ipgp.earlywarning.utilities.*;
import fr.ipgp.earlywarning.messages.*;
import fr.ipgp.earlywarning.telephones.*;
/**
 * @author Patrice Boissier
 *
 */
public class Datagram2Trigger {
    protected DatagramPacket packet = null;
    protected InetAddress senderAddress;
    protected int senderPort;
    protected Trigger trigger;
    
    public Datagram2Trigger(DatagramPacket packet) {
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
    		if(receivedSplit[0].equals("Sismo") 
    				&& receivedSplit[1].matches("\\d\\d/\\d\\d/\\d\\d\\d\\d")
    				&& receivedSplit[2].matches("\\d\\d:\\d\\d:\\d\\d")
    				&& receivedSplit.length == 4)
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
            	EarlyWarning.appLogger.error("Version inconnue : " + receivedSplit[0]);
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
     * calllist : call list, either a comma separated list of digits or a .csv file name<br/>
     * repeat : true or false<br/>
     * message : warning message, either a message or a .wav file
     * @param elements the elements of the received message
     * @return true if the decoding was successful else false
     */
    public boolean decodeV2(String[] elements) {
    	boolean validFormat = true;
    	if (elements[1].matches("\\d")
    			&&elements[2].matches("\\d\\d\\d\\d/\\d\\d/\\d\\d")
    			&&elements[3].matches("\\d\\d:\\d\\d:\\d\\d")
    			&&elements[4].matches("\\w*")){
    		
    	}
    	return validFormat;
    }
}
