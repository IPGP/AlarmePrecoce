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
	
	public void testDecodeTrigger(String message, boolean result) throws UnknownHostException {
		System.out.println("Message : " + message);
		packet.setData(message.getBytes());
		packet.setLength(message.length());
		DatagramTriggerConverter datagram2Trigger = new DatagramTriggerConverter(packet);
		Trigger trigger = datagram2Trigger.getTrigger();
		Assert.assertEquals(trigger.getInetAddress(),address);
		Assert.assertEquals(datagram2Trigger.decode(message),result);		
	}
	
	@Test
	public void testDecodeV1Trigger() throws UnknownHostException {
		testDecodeTrigger("Sismo 13/03/2008 13:22:04 Declenchement", true);
		testDecodeTrigger("Sismo 13/03/2008 13:22:04 blalbla", true);
		testDecodeTrigger("Sismo 13/03/2008 13:22:04 blalbla blablabla blabla", true);
		// Invalid time
		testDecodeTrigger("Sismo 13/03/2008 56:22:04 sdf erffs", false);
		testDecodeTrigger("Sismo 13/03/2008 56:22:04 sdf", false);
		// Invalid type
		testDecodeTrigger("Sisdsmo 13/03/2008 13:22:04 Declenchement", false);
		// Invalid date
		testDecodeTrigger("Sismo 13f03/2008 13:22:04 Declenchement", false);
		// Invalid date and incomplete message
		testDecodeTrigger("Sismo 13/03/2008 56:22:04", false);
		// Incomplete message
		testDecodeTrigger("Sismo 03/15/0000 13:22:04", false);
	}
	
	@Test
	public void testDecodeV2Trigger() throws UnknownHostException {
		// vv p yyyy/MM/dd HH:mm:ss application calllist repeat message
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true |Ceci est un message d'alerte|", true);
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856 true |Ceci est un message d'alerte|", true);
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 fichier.csv true |Ceci est un message d'alerte|", true);
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 fichier.csv true |Alerte|", true);
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true message.wav", true);
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 file_test.csv true warningMessage.wav", true);
		// Invalid type
		testDecodeTrigger("020 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true Ceci est un message d alerte", false);
		// Incomplete message
		testDecodeTrigger("02 1 0000/03/18 13:22:04 ", false);
		// Invalid date
		testDecodeTrigger("02 1 2008/13/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true |Ceci est un message d alerte|", false);
		testDecodeTrigger("02 1 2008/03/32 13:22:04 appli_dataTaker01 0692703856,06924555455 true |Ceci est un message d alerte|", false);
		// Invalid time
		testDecodeTrigger("02 1 2008/12/18 25:22:04 appli_dataTaker01 0692703856,06924555455 true |Ceci est un message d alerte|", false);
		// Invalid text call list
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455,toto true |Ceci est un message d alerte|", false);
		// Invalid file call list
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 fichier.csvd true |Ceci est un message d alerte|", false);
		// Invalid text
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true message.fsd", false);
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 file_test.csv true warningMessage.wav coucou", true);		
	}
		
	public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestDatagramTriggerConverter.class);
    }
}
