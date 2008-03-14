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
    	trigger = new Trigger(CommonUtilities.getUniqueId(),1);
    	trigger.setInetAddress(senderAddress);
    	HashMap hashMap = decode(received);
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
    	int version;
    	
    	if (receivedSplit[0].matches("\\d\\d")) {
    		version = Integer.parseInt(receivedSplit[0]);
    		System.out.println("Test : " + version);
    	}
    	else {
    		if(receivedSplit[0].equals("Sismo")) {
    			version = 1;
    			System.out.println("Test : " + version);
    		}
    		else
    			version = 0;
    	}
    	
    	switch(version)
        {
            case 1:
                System.out.println("Version 1 : " + receivedSplit[0]);
            break;
            case 2:
            	System.out.println("Version 2 : " + receivedSplit[0]);
            break;
            default:
            	System.out.println("Version inconnue : " + receivedSplit[0]);
            break;
        }


    	
    	// Format historique OVPF : type 01
    	// Sismo dd/MM/yyyy HH:mm:ss Declenchement
    	//TODO utiliser des expressions regulieres pour verifier les champs recus!
    	if (receivedSplit[0].equals("Sismo") && receivedSplit.length == 4) {
    		trigger.setApplication(receivedSplit[0]);
        	trigger.setCallList(new TextCallList("default"));
        	trigger.setMessage(new TextWarningMessage(receivedSplit[3]));
        	trigger.setType("01");
        	trigger.setDate(receivedSplit[1] + " " + receivedSplit[2]);
        	trigger.setRepeat(true);
    	}
    	
    	// Format version 02 : type 02
    	// vv p yyyy/MM/dd HH:mm:ss application calllist repeat message
    	if (receivedSplit[0].equals("02")) {
    		
    	}
    	
    	return hashMap;
    }
}
