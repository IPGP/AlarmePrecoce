/**
 * Created Mar 13, 2008 11:07:36 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.triggers;

import java.util.*;
import java.net.*;
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
    	HashMap hashMap = decode(received);
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
     * @return hashMap the hashMap containing the 
     */
    public HashMap decode(String received) {
    	HashMap hashMap = new HashMap();
    	String[] receivedSplit = received.split(" ");
    	
    	// Format historique OVPF : type v1
    	// 
    	if (receivedSplit[0].equals("Sismo")) {
    		trigger.setApplication(receivedSplit[0]);
        	//trigger.setCallList(new TextCallList("default"));
        	//trigger.setMessage(new TextWarningMessage(receivedSplit[3]));
        	trigger.setType("v1");
    	}
    	
    	// Format v2
    	// version priorite 
    	if (receivedSplit[0].equals("v2")) {
    		
    	}
    	
    	return hashMap;
    }
}
