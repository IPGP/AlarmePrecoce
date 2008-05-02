/**
 * Created Mar 21, 2008 11:20:01 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.gateway;

import java.util.Random;

/**
 * Mock phone gateway for testing purpose.
 * @author Patrice Boissier
 */
public class MockGateway implements Gateway {
	
	private static MockGateway uniqueInstance;
	
	private MockGateway() {
		
	}
	
	public static synchronized MockGateway getInstance() {
		if (uniqueInstance == null) {
			uniqueInstance = new MockGateway();
		}
		return uniqueInstance;
	}
	
	/**
	 * 
	 */
	public String callText(String phoneno, String text, boolean selfdelete) {
		return "";
	}
	
	public String callAudio(String phoneno, String audiofile, boolean selfdelete) {
		return "";
	}
	
	public String callStatus(String reqID) {
		Random generator = new Random();
		int randomInt = generator.nextInt(4);
		switch (randomInt) {
		case 0 :
			return "Call Made";
		case 1 :
			return "Call Failed";
		case 2 :
			return "Call Will Retry";
		default :
			return "";	
		}
	}
	
	public String callRemove(String reqID) {
		return "";
	}
	public String callTillConfirm(String vcastexe, String vocfile, String wavfile, String ccode, String [] phoneNumbers) {
		return "";
	}
}
