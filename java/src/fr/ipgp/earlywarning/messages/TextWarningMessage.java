/**
 * Created Mar 5, 2008 8:24:15 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.messages;

/**
 * @author Patrice Boissier
 * This class represents the text warning message to be delivered by phone call
 */
public class TextWarningMessage implements WarningMessage {
	private String text;
	
	public TextWarningMessage() {
		
	}
	
	/**
	 * @return a String representing the object
	 */
	public String toString() {
		return text;
	}
}
