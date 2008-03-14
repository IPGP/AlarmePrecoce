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
public class TestDatagram2Trigger {
	
    protected DatagramPacket packet = null;
    protected byte[] buffer = new byte[65535];
    
	@Before
	public void setUp() {
		packet = new DatagramPacket(buffer, buffer.length);
	}
	
	@After
	public void tearDown() {

	}
	
	@Test
	public void testCreateDatagram2Trigger() throws UnknownHostException {
		HashMap hashMap = new HashMap();
		String message = new String("Sismo 13/03/2008 13:22:04 Declenchement");
		InetAddress address = InetAddress.getByName("localhost");
		packet.setData(message.getBytes());
		packet.setPort(4445);
		packet.setAddress(address);
		packet.setLength(message.length());
		Datagram2Trigger datagram2Trigger = new Datagram2Trigger(packet);
		Trigger trigger = datagram2Trigger.getTrigger();
		Assert.assertEquals(trigger.getInetAddress(),address);
		Assert.assertEquals(datagram2Trigger.decode(message),hashMap);
	}
	
	public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestDatagram2Trigger.class);
    }
}
