package fr.ipgp.earlywarning.triggers;
/*
  Created Mar 13, 2008 11:09:14 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */

import fr.ipgp.earlywarning.messages.TextWarningMessage;
import fr.ipgp.earlywarning.messages.WarningMessage;
import fr.ipgp.earlywarning.telephones.FileCallList;
import fr.ipgp.earlywarning.telephones.InvalidFileNameException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author Patrice Boissier
 */
public class TestDatagramTriggerConverter {

    protected DatagramPacket packet = null;
    protected byte[] buffer = new byte[65535];
    protected InetAddress address = null;

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestDatagramTriggerConverter.class);
    }

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
        try {

            WarningMessage warningMessage = new TextWarningMessage("Declenchement");
            FileCallList callList = new FileCallList("defaultCallList.txt");
            packet.setData(message.getBytes());
            packet.setLength(message.length());
            DatagramTriggerConverter datagram2Trigger = new DatagramTriggerConverter(packet, callList, warningMessage, true, "11", 1);
            Trigger trigger = datagram2Trigger.getTrigger();
            datagram2Trigger.decode();
        } catch (UnknownTriggerFormatException | InvalidFileNameException | IOException | MissingTriggerFieldException | InvalidTriggerFieldException utfe) {
            System.out.println(utfe.getMessage());
        }
    }

    @Test
    public void testCreateV1Trigger() {
        try {
            WarningMessage warningMessage = new TextWarningMessage("Declenchement");
            FileCallList callList = new FileCallList("resources/defaultCallList.voc");
            String message = "Sismo 13/03/2008 13:22:04 Declenchement";
            packet.setData(message.getBytes());
            packet.setLength(message.length());
            DatagramTriggerConverter datagram2Trigger = new DatagramTriggerConverter(packet, callList, warningMessage, true, "11", 1);
            Trigger trig = datagram2Trigger.getTrigger();
            datagram2Trigger.decode();
            Assert.assertEquals("Sismo", trig.getApplication());
            Assert.assertEquals(callList, trig.getCallList());
            Assert.assertEquals(address, trig.getInetAddress());
            Assert.assertEquals(warningMessage, trig.getMessage());
            Assert.assertEquals(1, trig.getPriority());
            Assert.assertEquals("01", trig.getType());
            Assert.assertTrue(trig.getRepeat());
            Assert.assertEquals("13/03/2008 13:22:04", trig.getDate());
            Assert.assertEquals("11", trig.getConfirmCode());

        } catch (UnknownTriggerFormatException | InvalidFileNameException | IOException | MissingTriggerFieldException | InvalidTriggerFieldException utfe) {
            System.out.println(utfe.getMessage());
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
        testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 defaultCallList.voc true 123542 |Ceci est un message, d'alerte?|");
        testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 defaultCallList.txt true 2263 |Alerte|");
        testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true 958 message.wav");
        testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 defaultCallList.txt true 58547 warningMessage.wav");
        testDecodeTrigger("02 1 2008/03/21 11:00:33 nagios 0692703856 true 11 |Alerte, plus de place sur partage|");
    }

    @Test
    public void testDecodeInvalidV2Trigger() {
        // Invalid priority
        testDecodeTrigger("02 1d 2008/03/18 13:22:04 appli_dataTaker01 defaultCallList.csv true 11 warningMessage.wav");
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
        testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 defaultCallList.csvd true 11 |Ceci est un message d alerte|");
        // Invalid text
        testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856,06924555455 true 11 message.fsd");
        testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 defaultCallList.csv true 11 warningMessage.wav coucou");
        // Invalid confirm code
        testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 defaultCallList.csv true 5854705 warningMessage.wav");
        testDecodeTrigger("02 1 2008/03/18 13:22:04 appli_dataTaker01 defaultCallList.csv true 585z5 warningMessage.wav");
    }
}
