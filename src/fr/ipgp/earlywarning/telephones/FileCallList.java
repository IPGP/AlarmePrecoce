/**
 * Created Mar 5, 2008 8:24:15 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.telephones;

import java.io.File;

/**
 * @author Patrice Boissier
 *
 */
public class FileCallList implements CallList{
	private File file;
	
	public FileCallList(File file) {
		this.file = file;
	}
	
	/**
	 * @return a String representing the object
	 */
	public String toString() {
		String result = file.toString();
		return result;
	}
	
	public File getFile() {
		return this.file;
	}
}
