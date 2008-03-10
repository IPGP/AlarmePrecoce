/**
 * Created Mon 10, 2008 03:03:00 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.utilities;

import java.text.SimpleDateFormat;
import java.util.Date;
/**
 * Formate des date dans differents formats.
 * @author Patrice Boissier
 *
 */
public class DateFormater {

	public static String toISO() {
		Date date = new Date();
		SimpleDateFormat  simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return(simpleFormat.format(date));
	}
	
	public static String toISO(Date date) {
		SimpleDateFormat  simpleFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return(simpleFormat.format(date));
	}
}
