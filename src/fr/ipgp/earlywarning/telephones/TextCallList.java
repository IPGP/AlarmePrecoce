/**
 * Created Mar 5, 2008 8:24:15 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.telephones;

/**
 * @author Patrice Boissier
 *
 */
public class TextCallList implements CallList{
	private String text;
	
	public TextCallList(String text) {
		this.text = text;
	}
	
	/**
	 * @return a String representing the object
	 */
	public String toString() {
		return text;
	}
}
