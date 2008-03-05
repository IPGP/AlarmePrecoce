package fr.ipgp.earlywarning.controler;

import java.io.*;
import java.net.*;
import fr.ipgp.earlywarning.triggers.*;

public class EarlyWarningThread extends Thread {
	
    protected DatagramSocket socket = null;
    protected DatagramPacket packet = null;
    protected boolean moreTriggers = true;
    protected byte[] buffer = null;
    protected int port = 4445;
	
    public EarlyWarningThread() throws IOException {
    	this("EarlyWarningThread");
    }

    public EarlyWarningThread(String name) throws IOException {
    	super(name);
    	socket = new DatagramSocket(port);
    	buffer = new byte[256];
    	packet = new DatagramPacket(buffer, buffer.length);
    }
    
    public void run() {
    	System.out.println("Création du thread EarlyWarningThread");
        while (moreTriggers) {
        	System.out.println("En attente de trigger");

        	try {
                socket.receive(packet);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
            if (Thread.interrupted()) {
                return;
            }
            Trigger trigger = new Trigger(System.currentTimeMillis());

        }
        socket.close();
    }
}
