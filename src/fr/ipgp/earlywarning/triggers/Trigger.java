package fr.ipgp.earlywarning.triggers;

import java.util.*;
import java.net.*;

public class Trigger {
	private String type = null;
	private Map<String,Object> properties = null;
	
	public Trigger (DatagramPacket packet) {
		this.properties = new Hashtable<String,Object>();
		properties.put("inetAddress", new String(packet.getAddress().toString()));
		properties.put("port", new Integer(packet.getPort()));
		this.type = "v1"; 
		
		String message = new String(packet.getData(), 0, packet.getLength());
		System.out.println(message);
	}
	
	public String toString() {
		String returnString = "Trigger from " + (String) this.properties.get("inetAddress") + ":" + (Integer) this.properties.get("port")+ " format "+ this.type;
		return returnString;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public Object getProperty(String key) {
		return this.properties.get(key);
	}
	
	public void setProperty(String key, Object value) {
		this.properties.put(key, value);
	}
}
