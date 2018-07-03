/*
  Created Mar 5, 2008 3:00:05 PM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.test;

import java.io.IOException;
import java.net.*;

/**
 * @author Patrice Boissier
 */
public class TriggerV2Sender3 {

    private static byte[] message = null;
    private static int port = 4445;
    private static InetAddress address = null;
    private static DatagramSocket socket = null;
    private static DatagramPacket packet = null;

    public static void main(String[] args) {

        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException uhe) {
            uhe.printStackTrace();
        }

        message = new byte[512];

        //Format V2 : vv p yyyy/MM/dd HH:mm:ss application calllist repeat confirmcode message<br/>
        //String messageString = "02 1 2008/03/18 13:22:04 appli_dataTaker01 0692703856 true 1 alerte.wav";
        String messageString = "02 1 2008/03/21 11:00:33 nagios default true 11 test_tone";

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