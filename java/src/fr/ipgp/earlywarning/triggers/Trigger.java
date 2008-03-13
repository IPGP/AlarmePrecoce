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
 * Trigger object that "triggers" phone calls
 */
public class Trigger implements Comparable {
	private Long id;
	private Integer priority;
	private String type;
	private InetAddress inetAddress;
	private String application;
	private CallList callList;
	private WarningMessage message;
	private boolean repeat;
	private Map<String,Object> properties;
	
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
	 * @param repeat the repeat to set
	 */
	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}
	
	/**
	 * @return the repeat
	 */
	public boolean getRepeat() {
		return repeat;
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
            properties = new HashMap<String,Object>();
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
	 * The compareTo method compares the receiving object with the specified object.
	 * If the specified object cannot be compared to the receiving object, the method 
	 * throws a ClassCastException.
	 * @param o the Object to compare to the Trigger
	 * @return returns a negative integer, 0, or a positive integer depending on whether the receiving object is less than, equal to, or greater than the specified object.
	 * @throws ClassCastException - if the specified object's type prevents it from being compared to this Object.
	 */
	public int compareTo(Object o) {
		Trigger trigger = (Trigger)o;
        int lastCmp = priority.compareTo(trigger.getPriority());
        return (lastCmp != 0 ? lastCmp :
            this.id.compareTo(trigger.id));
    }

	/**
	 * Indicates whether some other object is "equal to" this Comparator. This method must obey the general contract of Object.equals(Object)
	 * Overrides : equals in class Object
	 * @param o the Object to compare to the Trigger
	 * @return true only if the specified object is also a comparator and it imposes the same ordering as this comparator
	 */
    public boolean equals(Object o) {
        if (!(o instanceof Trigger))
            return false;
        Trigger trigger = (Trigger)o;
        return trigger.id.equals(this.id) && trigger.priority.equals(this.priority);
    }
    
	/**
	 * Returns a hash code value for the object. This method is supported for the benefit of hashtables such as those provided by java.util.Hashtable.
	 * Overrides : hashCode in class Object
	 * @return the hash code value for the object
	 */
    public int hashCode() {
        return id.hashCode() + priority.hashCode();
    }
    
    /**
     * For debugging purpose
     * @return a String with the id and priority of the Trigger
     */
    public String toString() {
    	return "Id : " + id + " - Priority : " + priority;
    }
}
