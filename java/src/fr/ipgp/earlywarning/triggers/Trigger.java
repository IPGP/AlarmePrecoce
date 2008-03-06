/**
 * Created Mar 5, 2008 3:00:05 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.triggers;

import java.util.*;
import java.net.*;
import fr.ipgp.earlywarning.telephones.*;
import fr.ipgp.earlywarning.messages.*;

/**
 * @author Patrice Boissier
 *
 */
public class Trigger implements Comparable {
	private Long id;
	private Integer priority;
	private String type;
	private InetAddress inetAddress;
	private String application;
	private CallList callList;
	private WarningMessage message;
	private Map properties;
	
	public Trigger (Long id, Integer priority) {
		if (id == null || priority == null)
            throw new NullPointerException();
		this.id = id;
		this.priority = priority;
	}
	
	/**
	 * @return the priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @param priority the priority to set
	 */
	public void setPriority(int priority) {
		this.priority = priority;
	}

	/**
	 * @return the application
	 */
	public String getApplication() {
		return application;
	}

	/**
	 * @param application the application to set
	 */
	public void setApplication(String application) {
		this.application = application;
	}

	/**
	 * @return the callList
	 */
	public CallList getCallList() {
		return callList;
	}

	/**
	 * @param callList the callList to set
	 */
	public void setCallList(CallList callList) {
		this.callList = callList;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @return the inetAddress
	 */
	public InetAddress getInetAddress() {
		return inetAddress;
	}

	/**
	 * @param inetAddress the inetAddress to set
	 */
	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	/**
	 * @return the message
	 */
	public WarningMessage getMessage() {
		return message;
	}

	/**
	 * @param message the message to set
	 */
	public void setMessage(WarningMessage message) {
		this.message = message;
	}

	/**
	 * @return the properties
	 */
	public Map getProperties() {
		return properties;
	}

	/**
	 * @param key the property key to set
	 * @param value the value to set for the key
	 */
	public void setProperty(String key, Object value) {
		if (properties == null) {
            properties = new HashMap();
        }
		this.properties.put(key, value);
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	
	/**
	 * The compareTo method compares the receiving object with the specified object and 
	 * returns a negative integer, 0, or a positive integer depending on whether the 
	 * receiving object is less than, equal to, or greater than the specified object.
	 * If the specified object cannot be compared to the receiving object, the method 
	 * throws a ClassCastException.
	 * @param trigger
	 * @return
	 */
	public int compareTo(Object o) {
		Trigger trigger = (Trigger)o;
        int lastCmp = priority.compareTo(trigger.getPriority());
        return (lastCmp != 0 ? lastCmp :
            this.id.compareTo(trigger.id));
    }

    public boolean equals(Object o) {
        if (!(o instanceof Trigger))
            return false;
        Trigger trigger = (Trigger)o;
        return trigger.id.equals(this.id) && trigger.priority.equals(this.priority);
    }
    
    public int hashCode() {
        return id.hashCode() + priority.hashCode();
    }
    
    public String toString() {
    	return id + " " + priority;
        }
}
