package fr.ipgp.earlywarning.triggers;

import java.util.*;
import java.net.*;

public class Trigger {
	private String type;
	private Map properties;
	
	public Trigger (DatagramPacket packet) {
		this.properties = new Hashtable();
		properties.put("inetAddress", new String(packet.getAddress().toString()));
		properties.put("port", new Integer(packet.getPort()));
		this.type = "v1"; 
	}
	
	public String toString() {
		String returnString = "Trigger from " + (String) this.properties.get("inetAddress") + ":" + (Integer) this.properties.get("port")+ " format "+ this.type;
		return returnString;
	}
	
}
