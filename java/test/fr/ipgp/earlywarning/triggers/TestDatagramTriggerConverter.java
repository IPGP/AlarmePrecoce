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
	
	@Test
	public void testDecodeV1Trigger() throws UnknownHostException {
		String message = new String("Sismo 13/03/2008 13:22:04 Declenchement");
		packet.setData(message.getBytes());
		packet.setLength(message.length());
		DatagramTriggerConverter datagram2Trigger = new DatagramTriggerConverter(packet);
		Trigger trigger = datagram2Trigger.getTrigger();
		Assert.assertEquals(trigger.getInetAddress(),address);
		Assert.assertEquals(datagram2Trigger.decode(message),true);
	}
	
	@Test
	public void testDecodeV2Trigger() throws UnknownHostException {
		String message = new String("Sismo 13/03/2008 13:22:04 Declenchement");
		packet.setData(message.getBytes());
		packet.setLength(message.length());
		DatagramTriggerConverter datagram2Trigger = new DatagramTriggerConverter(packet);
		Trigger trigger = datagram2Trigger.getTrigger();
		Assert.assertEquals(trigger.getInetAddress(),address);
		Assert.assertEquals(datagram2Trigger.decode(message),true);
	}
	
	@Test
	public void testDecodeUnknownTrigger() throws UnknownHostException {
		String message = new String("Sifsdsmo 13/03/2008 13:22:04 Declenchement");
		packet.setData(message.getBytes());
		packet.setLength(message.length());
		DatagramTriggerConverter datagram2Trigger = new DatagramTriggerConverter(packet);
		Trigger trigger = datagram2Trigger.getTrigger();
		Assert.assertEquals(trigger.getInetAddress(),address);
		Assert.assertEquals(datagram2Trigger.decode(message),false);
	}

	@Test
	public void testDecodeV1BadTrigger() throws UnknownHostException {
		String message = new String("Sismo 13/03/200s8 1f3:22f:,04 Ddqsfeclenchement df sdfzer asr ");
		packet.setData(message.getBytes());
		packet.setLength(message.length());
		DatagramTriggerConverter datagram2Trigger = new DatagramTriggerConverter(packet);
		Trigger trigger = datagram2Trigger.getTrigger();
		Assert.assertEquals(trigger.getInetAddress(),address);
		Assert.assertEquals(datagram2Trigger.decode(message),false);
	}
	
	public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestDatagramTriggerConverter.class);
    }
}
