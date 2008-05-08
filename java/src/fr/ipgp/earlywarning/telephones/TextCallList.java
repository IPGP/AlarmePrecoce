/**
 * Created Mar 5, 2008 8:24:15 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.telephones;

/**
 * Class representing text call lists.
 * @author Patrice Boissier
 */
public class TextCallList implements CallList{
	private String text;
    private CallListType type = CallListType.TEXT;
	
	public TextCallList(String text) {
		this.text = text;
	}
	
	/**
	 * @return a String representing the object
	 */
	public String toString() {
		return text;
	}
	
	/**
	 * @return the type
	 */
	public CallListType getType() {
		return type;
	}

	/**
	 * @return the text to get
	 */
	public String getText() {
		return text;
	}
	
	/**
	 * @return the Text Call List name
	 */
	public String getName() {
		return "Text call list";
	}
}
