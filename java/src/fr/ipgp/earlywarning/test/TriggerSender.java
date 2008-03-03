package fr.ipgp.earlywarning.test;

import java.io.*;
import java.net.*;

public class TriggerSender {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try	{
			// get a datagram socket
			DatagramSocket socket = new DatagramSocket();
			
			// send request
			byte[] buf = new byte[256];
			String application = "TriggerSender";
			buf = application.getBytes();
			InetAddress address = InetAddress.getByName("localhost");
			DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 4445);
			socket.send(packet);
			socket.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}		
	}
}
