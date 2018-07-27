package fr.ipgp.earlywarning.heartbeat;

import fr.ipgp.earlywarning.EarlyWarning;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class HeartbeatServerThread extends Thread {
    int port;
    ServerSocket server;
    boolean active = true;

    private static HeartbeatServerThread uniqueInstance;

    private HeartbeatServerThread(int port) throws IOException {
        this.port = port;
        server = new ServerSocket(port);
    }

    public static HeartbeatServerThread getInstance(int port) throws IOException {
        if (uniqueInstance == null)
            uniqueInstance = new HeartbeatServerThread(port);

        return uniqueInstance;
    }

    public void disable()
    {
        EarlyWarning.appLogger.info("Disabling heartbeat server.");
        active = false;
    }

    public void enable()
    {
        EarlyWarning.appLogger.info("Enabling heartbeat server.");
        active = true;
    }

    public void run() {
        EarlyWarning.appLogger.info("Starting heartbeat server on port " + port);

        while (true) {
            if (!active) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                continue;
            }

            Socket s;
            try {
                s = server.accept();
            } catch (IOException e) {
                EarlyWarning.appLogger.error("Could not accept client: " + e.getMessage());
                continue;
            }

            if (!active) {
                EarlyWarning.appLogger.info("Received request but server is inactive.");
                continue;
            }

            ObjectInputStream in;
            ObjectOutputStream out;
            try {
                in = new ObjectInputStream(s.getInputStream());
                out = new ObjectOutputStream(s.getOutputStream());
            } catch (IOException e) {
                EarlyWarning.appLogger.error("Could not get IO streams for socket.");
                continue;
            }

            AliveRequest req;
            try {
                s.setSoTimeout(10000);
                req = (AliveRequest) in.readObject();
            } catch (SocketTimeoutException ex) {
                EarlyWarning.appLogger.error("No request was sent to the server.");
                continue;
            } catch (IOException e) {
                EarlyWarning.appLogger.error("Could not read request.");
                continue;
            } catch (ClassNotFoundException ignored) {
                EarlyWarning.appLogger.error("Could not decode incoming message (Class not found).");
                continue;
            }

            EarlyWarning.appLogger.info("Received alive request: '" + req.toString() + "'");

            AliveResponse resp = new AliveResponse();
            try {
                out.writeObject(resp);
                out.flush();
            } catch (IOException e) {
                EarlyWarning.appLogger.error("Could not write response to socket.");
                continue;
            }
        }
    }
}
