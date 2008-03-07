/**
 * Created Tue 06, 2008 10:32:15 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.utilities;

/**
 * This class generates a unique id (in a single JVM) based on system time.
 * @author http://www.rgagnon.com/javadetails/java-0385.html
 *
 */
public class UniqueID {
	static long current= System.currentTimeMillis();
	static public synchronized long get(){
		return current++;
	}
}
