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
    private int total = -1;
	
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
	 * @return the text to get
	 */
	public String getText() {
		return text;
	}
	
   public boolean next() {
	   return true;
   }
	    
   public String getValue(String name) {
	   if (CallList.NAME.equals(name))
		   return "Name";
	   if (CallList.PHONE.equals(name))
		   return "Phone";
	   return null;
   }
	    
   public int getTotal() {
	   return total;
   }
}
