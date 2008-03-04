package fr.ipgp.earlywarning.test;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TriggerSender {

	private static byte[] message = null;
	private static int port = 4445;
	private static InetAddress address = null;
	private static DatagramSocket socket = null;
	private static DatagramPacket packet = null;
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			address = InetAddress.getByName("195.83.188.255");
		} catch (UnknownHostException uhe) {
			uhe.printStackTrace();
		}
		
		message = new byte[256];
		
		// preparation de la date
		Date date1 = new Date();
		SimpleDateFormat  simpleFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		
		String messageString = "Sismo " + simpleFormat.format(date1) + " Declenchement";
			
		message = new byte[messageString.length()];
		message = messageString.getBytes();
		try {
			packet = new DatagramPacket(message, message.length, address, port);
			socket = new DatagramSocket();
		} catch (SocketException se) {
			se.printStackTrace();
		}
		try {
			socket.send(packet);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		socket.close();
	}
}