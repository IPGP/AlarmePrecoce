package fr.ipgp.earlywarning.triggers;
/**
 * Created Mar 13, 2008 11:09:14 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */

import org.junit.*;

import java.net.*;
import java.io.*;
import fr.ipgp.earlywarning.messages.*;
import fr.ipgp.earlywarning.telephones.*;
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
		WarningMessage warningMessage = new TextWarningMessage("Declenchement");
		CallList callList = new FileCallList(new File("default.voc"));
		packet.setData(message.getBytes());
		packet.setLength(message.length());
		DatagramTriggerConverter datagram2Trigger = new DatagramTriggerConverter(packet, callList, warningMessage, true, "11",1);
		Trigger trigger = datagram2Trigger.getTrigger();
		try {
			datagram2Trigger.decode();
		} catch (UnknownTriggerFormatException utfe) {
			System.out.println(utfe.getMessage());
		} catch (InvalidTriggerFieldException itfe) {
			System.out.println(itfe.getMessage());
		} catch (MissingTriggerFieldException mtfe) {
			System.out.println(mtfe.getMessage());
		}
	}
	
	@Test
	public void testCreateV1Trigger() {
		WarningMessage warningMessage = new TextWarningMessage("Declenchement");
		CallList callList = new FileCallList(new File("default.voc"));
		String message = "Sismo 13/03/2008 13:22:04 Declenchement";
		packet.setData(message.getBytes());
		packet.setLength(message.length());
		DatagramTriggerConverter datagram2Trigger = new DatagramTriggerConverter(packet, callList, warningMessage, true, "11", 1);
		Trigger trig = datagram2Trigger.getTrigger();
		try {
			datagram2Trigger.decode();
			Assert.assertEquals("Sismo",trig.getApplication());
			Assert.assertEquals(callList,trig.getCallList());
			Assert.assertEquals(address,trig.getInetAddress());
			Assert.assertEquals(warningMessage,trig.getMessage());
			Assert.assertEquals(1,trig.getPriority());
			Assert.assertEquals("01",trig.getType());
			Assert.assertEquals(true,trig.getRepeat());
			Assert.assertEquals("13/03/2008 13:22:04",trig.getDate());
			Assert.assertEquals("11",trig.getConfirmCode());
			
		} catch (UnknownTriggerFormatException utfe) {
			System.out.println(utfe.getMessage());
		} catch (InvalidTriggerFieldException itfe) {
			System.out.println(itfe.getMessage());
		} catch (MissingTriggerFieldException mtfe) {
			System.out.println(mtfe.getMessage());
		}
	}
	
	@Test
	public void testDecodeValidV1Trigger() {
		testDecodeTrigger("Sismo 13/03/2008 13:22:04 Declenchement");
		testDecodeTrigger("Sismo 13/03/2008 13:22:04 blalbla");
		testDecodeTrigger("Sismo 13/03/2008 13:22:04 blalbla blablabla blabla");
		
	}

	@Test
	public void testDecodeInvalidV1Trigger() {	
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
	public void testDecodeValidV2Trigger() {
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true 12 |Ceci est un message d'alerte!|");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856 true 1 |Ceci est un message 		d'alerte.|");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 fichier.voc true 123542 |Ceci est un message, d'alerte?|");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 fichier.voc true 2263 |Alerte|");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true 958 message.wav");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 file_test.voc true 58547 warningMessage.wav");
		testDecodeTrigger("02 1 2008/03/21 11:00:33 nagios 0692703856 true 11 |Alerte, plus de place sur partage|");
	}

	@Test
	public void testDecodeInvalidV2Trigger() {
		// Invalid priority
		testDecodeTrigger("02 1d 2008/03/18 13:22:04 appli_dataTaker01 file_test.voc true 11 warningMessage.wav");
		// Invalid type
		testDecodeTrigger("020 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true 11 Ceci est un message d alerte");
		// Incomplete message
		testDecodeTrigger("02 1 0000/03/18 13:22:04 ");
		// Invalid date
		testDecodeTrigger("02 1 2008/13/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true 11 |Ceci est un message d alerte|");
		testDecodeTrigger("02 1 2008/03/32 13:22:04 appli_dataTaker01 0692703856,06924555455 true 11 |Ceci est un message d alerte|");
		// Invalid time
		testDecodeTrigger("02 1 2008/12/18 25:22:04 appli_dataTaker01 0692703856,06924555455 true 11 |Ceci est un message d alerte|");
		// Invalid warning message
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true 11 |Ceci est un $message d alerte|");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true 11 |Ceci est un message d alerte");
		// Invalid text call list
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455,toto true 11 |Ceci est un message d alerte|");
		// Invalid file call list
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 fichier.vocd true 11 |Ceci est un message d alerte|");
		// Invalid text
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true 11 message.fsd");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 file_test.voc true 11 warningMessage.wav coucou");
		// Invalid confirm code
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 file_test.voc true 5854705 warningMessage.wav");
		testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 file_test.voc true 585z5 warningMessage.wav");
	}
		
	public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestDatagramTriggerConverter.class);
    }
}
