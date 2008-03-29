/**
 * Created Mar 5, 2008 3:00:05 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.telephones;

/**
 * @author Patrice Boissier
 *
 */
public interface CallList {
    public static final String NAME = "name";
    public static final String PHONE = "phone";
    
	/**
	 * @return a String representing the object
	 */
	public String toString();
	
    /**
     * move the record forward
     */
    public boolean next();
    
    /**
     * get the current record values
     * @param name parameter name
     */
    public String getValue(String name);
    
    /**
     * total number of records
     */
    public int getTotal();
}
