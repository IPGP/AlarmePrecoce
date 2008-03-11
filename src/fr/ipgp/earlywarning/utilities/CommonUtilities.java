/**
 * Created Tue 06, 2008 10:32:15 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.utilities;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Useful methods for standalone applications...
 *
 */
public class CommonUtilities {
	
	private static long current= System.currentTimeMillis();
	/**
	 * This methode generates a unique id (in a single JVM) based on system time.
	 * @author http://www.rgagnon.com/javadetails/java-0385.html
	 * @return long the unique  id
	 */
	public static synchronized long getUniqueId(){
		return current++;
	}
	
	/**
	 * This method ensure that no other occurence of the application is already running.
	 * @author http://rom.developpez.com/java-instance-unique/
	 * @param lock the name of the lock file
	 * @return true if the application is unique else it returns false
	 */
	public static boolean isUnique(String lock) {
	    boolean unique;
	    try {
	        unique = new FileOutputStream(lock).getChannel().tryLock() != null;
	    } catch(IOException ie) {
	        unique = false;
	    }
	    return unique;
	}
	
	/**
	 * Format the current date (based on system time) in ISO format.
	 * @return the date in ISO format
	 */
	public static String dateToISO() {
		Date date = new Date();
		SimpleDateFormat  simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return(simpleFormat.format(date));
	}
	
	/**
	 * Format the date in ISO format.
	 * @param date the date to format
	 * @return the date in ISO format
	 */
	public static String dateToISO(Date date) {
		SimpleDateFormat  simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return(simpleFormat.format(date));
	}
}
