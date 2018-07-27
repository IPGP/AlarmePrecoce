package fr.ipgp.earlywarning.triggersender;

import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Sender {
    private final static String DEFAULT_CONFIRM_CODE = "11";
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("YYYY/MM/dd hh:mm:ss");

    public void send(int priority, Date date, String application, String contactList, boolean repeat, String confirmCode, String message)
    {
        final int port = 4445;
        byte[] data;
        InetAddress address = null;

        String dateAsString = dateFormatter.format(date);
        String dataString = String.format("02 %d %s %s %s %s %s %s", priority, dateAsString, application, contactList, repeat, confirmCode, message);

//        String dataString = "02 1 2008/03/21 11:00:33 nagios default true 11 sismicite";

        data = dataString.getBytes();

        System.out.println("Sending datagram '" + dataString + "'");
        for (Server s : Configuration.servers)
            sendBytes(data, s.host, s.port);

        System.out.println();
    }

    private void sendBytes(byte[] data, String host, int port)
    {
        System.out.println("\tSending trigger to " + host);

        DatagramSocket socket = null;
        DatagramPacket packet = null;

        try {
            packet = new DatagramPacket(data, data.length, InetAddress.getByName(host), port);
            socket = new DatagramSocket();
        } catch (SocketException | UnknownHostException ex) {
            ex.printStackTrace();
        }

        if (socket == null)
            System.err.println("Could not build socket for " + host + ":" + port);
        else if (packet == null)
            System.err.println("Could not build packet for " + host + ":" + port);

        try {
            socket.send(packet);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        socket.close();
    }

    public void send(Date date, String application, String contactList, String confirmCode, String message) {
        send(5, date, application, contactList, true, confirmCode, message);
    }

    public void send(Date date, String application, String contactList, String message)
    {
        send(date, application, contactList, DEFAULT_CONFIRM_CODE, message);
    }

    public void send(String application, String message, String contactList)
    {
        send(new Date(), application, contactList, message);
    }

    public void send(String application, String message)
    {
        send(application, message, "default");
    }

    public void send(String contactList)
    {
        send("nagios", "default", contactList);
    }

    public Sender()
    {

    }
}
