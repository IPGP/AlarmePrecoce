/**
 * Created Mar 5, 2008 3:00:05 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.triggers;

import fr.ipgp.earlywarning.messages.WarningMessage;
import fr.ipgp.earlywarning.messages.WarningMessageType;
import fr.ipgp.earlywarning.telephones.CallList;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Trigger object that "triggers" phone calls.
 *
 * @author Patrice Boissier
 */
public class Trigger implements Comparable {
    private Long id;
    private Integer priority;
    private String type;
    private InetAddress inetAddress;
    private String application;
    private CallList callList;
    private WarningMessage message;
    private String textMessage;
    private boolean repeat;
    private String date;
    private String confirmCode;
    private Map<String, Object> properties;

    public Trigger(Long id, Integer priority) {
        if (id == null || priority == null)
            throw new NullPointerException();
        this.id = id;
        this.priority = priority;
    }

    /**
     * @return the textMessage
     */
    public String getTextMessage() {
        return textMessage;
    }

    /**
     * @param textMessage the textMessage to set
     */
    public void setTextMessage(String textMessage) {
        this.textMessage = textMessage;
    }

    /**
     * @return the confirmCode
     */
    public String getConfirmCode() {
        return confirmCode;
    }

    /**
     * @param confirmCode the confirmCode to set
     */
    public void setConfirmCode(String confirmCode) {
        this.confirmCode = confirmCode;
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
     * @return the repeat
     */
    public boolean getRepeat() {
        return repeat;
    }

    /**
     * @param repeat the repeat to set
     */
    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    /**
     * @return the date
     */
    public String getDate() {
        return date;
    }

    /**
     * @param date the date to set
     */
    public void setDate(String date) {
        this.date = date;
    }

    /**
     * @return the properties
     */
    public Map getProperties() {
        return properties;
    }

    /**
     * @param key   the property key to set
     * @param value the value to set for the key
     */
    public void setProperty(String key, Object value) {
        if (properties == null) {
            properties = new HashMap<>();
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
     *
     * @param o the Object to compare to the Trigger
     * @return returns a negative integer, 0, or a positive integer depending on whether the receiving object is less than, equal to, or greater than the specified object.
     * @throws ClassCastException - if the specified object's type prevents it from being compared to this Object.
     */
    public int compareTo(Object o) {
        Trigger trigger = (Trigger) o;
        int lastCmp = priority.compareTo(trigger.getPriority());
        return (lastCmp != 0 ? lastCmp :
                this.id.compareTo(trigger.id));
    }

    /**
     * Indicates whether some other object is "equal to" this Comparator. This method must obey the general contract of Object.equals(Object)
     * Overrides : equals in class Object
     *
     * @param o the Object to compare to the Trigger
     * @return true only if the specified object is also a comparator and it imposes the same ordering as this comparator
     */
    public boolean equals(Object o) {
        if (!(o instanceof Trigger))
            return false;
        Trigger trigger = (Trigger) o;
        return trigger.id.equals(this.id) && trigger.priority.equals(this.priority);
    }

    /**
     * Returns a hash code value for the object. This method is supported for the benefit of hashtables such as those provided by java.util.Hashtable.
     * Overrides : hashCode in class Object
     *
     * @return the hash code value for the object
     */
    public int hashCode() {
        return id.hashCode() + priority.hashCode();
    }

    /**
     * For debugging purpose
     *
     * @return a String with the id and priority of the Trigger
     */
    public String toString() {
        return "Id : " + id + " - Priority : " + priority;
    }

    /**
     * Represent the properties of a trigger
     *
     * @return a String with the properties of the Trigger
     */
    public String showTrigger() {
        return "Id : " + id + " - Priority : " + priority + " - Type : " + type + " From : " + inetAddress.toString() +
                " - Application : " + application + " - Repeat : " + repeat + " - Date : " + date + " - Confirm Code : " + confirmCode +
                " - Call List : " + callList.toString() + " - Warning Message : " + message.toString();
    }

    /**
     * Represent a trigger for mail notification
     */
    public String mailTrigger() {
        String body = "Trigger " + id + " received on " + date + "\n";
        body += "Priority : " + priority + "\n";
        body += "Type : " + type + "\n";
        body += "From host : " + inetAddress.toString() + "\n";
        body += "From application : " + application + "\n";
        body += "Confirmation code : " + confirmCode + "\n";
        body += "Call list : " + callList.toString() + "\n";
        body += "Warning message : " + message.toString() + "\n";
        if (message.getType() == WarningMessageType.WAV)
            body += "Text Warning message : " + textMessage + "\n";
        return body;
    }
}
