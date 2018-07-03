/*
  Created Mar 5, 2008 3:00:05 PM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.test;

import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Patrice Boissier
 */
public class TriggerV2Sender {

    private static byte[] message = null;
    private static int port = 4445;
    private static InetAddress address = null;
    private static DatagramSocket socket = null;
    private static DatagramPacket packet = null;

    public static void main(String[] args) {

        try {
            address = InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }

        message = new byte[512];

        //Format V2: vv p yyyy/MM/dd HH:mm:ss application calllist repeat confirmcode message<br/>
        Date date1 = new Date();
        SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String messageString = "02 1 " + simpleFormat.format(date1) + " TestApplication defaultCallList.voc true 11 defaultWarningMessage.wav";

        message = new byte[messageString.length()];
        message = messageString.getBytes();
        try {
            packet = new DatagramPacket(message, message.length, address, port);
            socket = new DatagramSocket();
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        try {
            socket.send(packet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        socket.close();
    }
}