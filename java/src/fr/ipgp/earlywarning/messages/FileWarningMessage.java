/**
 * Created Mar 5, 2008 8:24:15 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.messages;

import java.io.*;
/**
 * @author Patrice Boissier
 * This class represents the file warning message to be delivered by phone call
 */
public class FileWarningMessage implements WarningMessage {
	private File file;
	
	public FileWarningMessage(File file) {
		this.file = file;
	}
	
	/**
	 * @return a String representing the object
	 */
	public String toString() {
		String result = file.toString();
		return result;
	}
}
