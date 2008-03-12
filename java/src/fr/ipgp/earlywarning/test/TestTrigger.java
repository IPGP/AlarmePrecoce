/**
 * Created Mar 12, 2008 10:52:16 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.test;

import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.*;
/**
 * @author Patrice Boissier
 *
 */
public class TestTrigger {

	private byte[] message = null;
	private int port = 4445;
	private InetAddress address = null;
	private DatagramSocket socket = null;
	private DatagramPacket packet = null;
	
	@Before
	public void setUp() throws UnknownHostException, SocketException {
		address = InetAddress.getByName("localhost");
	}
	
	@After
	public void tearDown() {
		socket.close();
	}
	
	@Test
	public void testNormalTrigger() throws IOException {
		Date date1 = new Date();
		SimpleDateFormat  simpleFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		String messageString = "Sismo " + simpleFormat.format(date1) + " Declenchement";
		message = new byte[messageString.length()];
		message = messageString.getBytes();
		packet = new DatagramPacket(message, message.length, address, port);
		socket = new DatagramSocket();
		socket.send(packet);
	}
}
