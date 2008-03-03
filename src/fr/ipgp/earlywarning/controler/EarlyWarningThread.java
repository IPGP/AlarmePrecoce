package fr.ipgp.earlywarning.controler;

import java.io.*;
import java.net.*;
import fr.ipgp.earlywarning.triggers.*;

public class EarlyWarningThread extends Thread {
	
    protected DatagramSocket socket = null;
    protected boolean moreTriggers = true;
	
    public EarlyWarningThread() throws IOException {
    	this("EarlyWarningThread");
    }

    public EarlyWarningThread(String name) throws IOException {
    	super(name);
    	socket = new DatagramSocket(4445);
    }
    
    public void run() {
    	System.out.println("Création du thread EarlyWarningThread");
        while (moreTriggers) {
            try {
                byte[] buf = new byte[256];
                
                // receive request
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                System.out.println("En attente de trigger");
                socket.receive(packet);
                if (Thread.interrupted()) {
                    //We've been interrupted: no more processing.
                    return;
                }
                Trigger trigger = new Trigger(packet);
                System.out.println(trigger.toString());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        socket.close();
    }
}
