/**
 * Created Tue 06, 2008 10:32:15 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.utilities;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Useful methods for standalone applications...
 *
 * @author http://www.rgagnon.com/javadetails/java-0385.html
 * @author http://www.javafr.com/codes/INSTANCE-UNIQUE-APPLICATION_40088.aspx
 */
public class CommonUtilities {
	
	private static long current= System.currentTimeMillis();
	/**
	 * This methode generates a unique id (in a single JVM) based on system time.
	 * @return long the unique  id
	 */
	public static synchronized long getUniqueId(){
		return current++;
	}
	
	/**
	 * This method ensure that no other occurence of the application is already running.
	 * @param lockFileName the name of the lock file
	 * @return true if the application is unique else it returns false
	 */
	public static boolean appIsUnique(String lockFileName) throws FileNotFoundException, IOException {
		boolean locked;
		File fileLock = new File("Verrou.lock");
		RandomAccessFile raFileLock = new RandomAccessFile(fileLock, "rw");
			
		  
		if (raFileLock.getChannel().tryLock() == null) {
			locked = false;
		}
		else {
			locked = true;
		}
		return locked;
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
	
	public static boolean isDate(String date, String format) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		try {
			sdf.parse(date);
			System.out.println("Date " + date + " is OK!");
			return true;
		} catch (ParseException pe) {
			System.out.println("Date " + date + " is WRONG!");
			return false;
		}
	}
}
