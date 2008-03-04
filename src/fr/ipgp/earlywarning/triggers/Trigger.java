package fr.ipgp.earlywarning.triggers;

import java.util.*;
import java.net.*;

public class Trigger {
	private String type = null;
	private Map properties = null;
	private String message = null;
	
	public Trigger (DatagramPacket packet) {
		this.properties = new Hashtable();
		properties.put("inetAddress", new String(packet.getAddress().toString()));
		properties.put("port", new Integer(packet.getPort()));
		message = new String(packet.getData(), 0, packet.getLength());
		this.type = "v1"; 
	}
	
	public String toString() {
		String returnString = "Trigger from " + (String) this.properties.get("inetAddress") + ":" + (Integer) this.properties.get("port")+ " format "+ this.type + " Content : " + message;
		return returnString;
	}
	
}
