/**
 * Created Mar 5, 2008 8:24:15 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.messages;

/**
 * This class represents the text warning message to be delivered by phone call
 * @author Patrice Boissier
 */
public class TextWarningMessage implements WarningMessage {
	private String text;
	
	public TextWarningMessage(String text) {
		this.text = text;
	}
	
	/**
	 * @return a String representing the object
	 */
	public String toString() {
		return text;
	}
	
	/**
	 * @return the text message
	 */
	public String getText() {
		return text;
	}
}
