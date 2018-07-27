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
public class TriggerSender {

    private static final int port = 4445;
    private static byte[] message = null;
    private static InetAddress address = null;
    private static DatagramSocket socket = null;
    private static DatagramPacket packet = null;

    public static void main(String[] args) {

        try {
            address = InetAddress.getByName("localhost");
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }

        message = new byte[256];

        // preparation de la date
        Date date1 = new Date();
        SimpleDateFormat simpleFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        String messageString = "Sismo " + simpleFormat.format(date1) + " Declenchement";

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