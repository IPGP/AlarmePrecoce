package fr.ipgp.earlywarning.triggers;
/**
 * Created Mar 13, 2008 11:09:14 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */

import org.junit.*;
import java.net.*;
import java.util.*;
import fr.ipgp.earlywarning.triggers.*;

/**
 * @author Patrice Boissier
 *
 */
public class TestDatagramTriggerConverter {
	
    protected DatagramPacket packet = null;
    protected byte[] buffer = new byte[65535];
	protected InetAddress address = null;
    
	@Before
	public void setUp() throws UnknownHostException {
		packet = new DatagramPacket(buffer, buffer.length);
		address = InetAddress.getByName("localhost");
		packet.setPort(4445);
		packet.setAddress(address);

	}
	
	@After
	public void tearDown() {

	}
	
	public void testDecodeTrigger(String message) {
		packet.setData(message.getBytes());
		packet.setLength(message.length());
		DatagramTriggerConverter datagram2Trigger = new DatagramTriggerConverter(packet);
		Trigger trigger = datagram2Trigger.getTrigger();
		Assert.assertEquals(trigger.getInetAddress(),address);
		try {
			datagram2Trigger.decode(message);
		} catch (UnknownTriggerFormatException utfe) {
			System.out.println("Unknown trigger format : " + message);
		} catch (InvalidTriggerFieldException itfe) {
			System.out.println("Invalid trigger field : " + message);
		} catch (MissingTriggerFieldException mtfe) {
			System.out.println("Missing trigger field : " + message);
		}
	}
	
	@Test
	public void testDecodeV1Trigger() {
		testDecodeTrigger("Sismo 13/03/2008 13:22:04 Declenchement");
		testDecodeTrigger("Sismo 13/03/2008 13:22:04 blalbla");
		testDecodeTrigger("Sismo 13/03/2008 13:22:04 blalbla blablabla blabla");
		// Invalid time
		testDecodeTrigger("Sismo 13/03/2008 56:22:04 sdf erffs");
		testDecodeTrigger("Sismo 13/03/2008 56:22:04 sdf");
		// Invalid type
		testDecodeTrigger("Sisdsmo 13/03/2008 13:22:04 Declenchement");
		// Invalid date
		testDecodeTrigger("Sismo 13f03/2008 13:22:04 Declenchement");
		// Invalid date and incomplete message
		testDecodeTrigger("Sismo 13/03/2008 56:22:04");
		// Incomplete message
		testDecodeTrigger("Sismo 03/15/0000 13:22:04");
	}
	
	@Test
	public void testDecodeV2Trigger() {
		// vv p yyyy/MM/dd HH:mm:ss application calllist repeat message
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true |Ceci est un message d'alerte!|");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856 true |Ceci est un message 		d'alerte.|");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 fichier.csv true |Ceci est un message, d'alerte?|");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 fichier.csv true |Alerté|");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true message.wav");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 file_test.csv true warningMessage.wav");
		// Invalid priority
		testDecodeTrigger("02 1d 2008/03/18 13:22:04 appli_dataTaker01 file_test.csv true warningMessage.wav");
		// Invalid type
		testDecodeTrigger("020 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true Ceci est un message d alerte");
		// Incomplete message
		testDecodeTrigger("02 1 0000/03/18 13:22:04 ");
		// Invalid date
		testDecodeTrigger("02 1 2008/13/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true |Ceci est un message d alerte|");
		testDecodeTrigger("02 1 2008/03/32 13:22:04 appli_dataTaker01 0692703856,06924555455 true |Ceci est un message d alerte|");
		// Invalid time
		testDecodeTrigger("02 1 2008/12/18 25:22:04 appli_dataTaker01 0692703856,06924555455 true |Ceci est un message d alerte|");
		// Invalid warning message
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true |Ceci est un $message d alerte|");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true |Ceci est un message d alerte");
		// Invalid text call list
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455,toto true |Ceci est un message d alerte|");
		// Invalid file call list
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 fichier.csvd true |Ceci est un message d alerte|");
		// Invalid text
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true message.fsd");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 file_test.csv true warningMessage.wav coucou");		
	}
		
	public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestDatagramTriggerConverter.class);
    }
}
