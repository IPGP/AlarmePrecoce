package fr.ipgp.earlywarning.heartbeat;

import fr.ipgp.earlywarning.EarlyWarning;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * The "heartbeat" server, that answers requests from other instances of the app on the network.
 * It is primordial in the failover system: when a backup instance receives a trigger, it first sends a message to the main instance, that is treated by this object.
 * Upon response from the main instance, it does not originate the call.<br />
 *
 * <i>Implements the Singleton design pattern.</i>
 *
 * @author Thomas Kowalski
 */
public class HeartbeatServerThread extends Thread {
    /**
     * The singleton's unique instance.
     */
    private static HeartbeatServerThread uniqueInstance;

    /**
     * The port to which the server should listen.
     */
    final int port;

    /**
     * The {@link ServerSocket} used to listen.
     */
    ServerSocket server;

    /**
     * Whether or not the server should answer requests. <i>Used only for unit tests.</i>
     */
    boolean active = true;

    /**
     * Only constructor, that takes a port number as its argument.
     *
     * @param port the port for the {@link ServerSocket} to use
     * @throws IOException if the port cannot be bound.
     */
    private HeartbeatServerThread(int port) throws IOException {
        super("HeartbeatServer");

        this.port = port;
        server = new ServerSocket(port);
    }

    /**
     * Returns the unique instance.
     *
     * @param port the port for the {@link ServerSocket} to use
     * @return the unique instance of the {@link HeartbeatServerThread}
     * @throws IOException if the port cannot be bound
     */
    public static HeartbeatServerThread getInstance(int port) throws IOException {
        if (uniqueInstance == null)
            uniqueInstance = new HeartbeatServerThread(port);

        return uniqueInstance;
    }

    /**
     * Stops the processing of any alive requests.
     */
    public void disable() {
        EarlyWarning.appLogger.info("Disabling heartbeat server.");
        active = false;
    }

    /**
     * Enables the processing of requests.
     */
    public void enable() {
        EarlyWarning.appLogger.info("Enabling heartbeat server.");
        active = true;
    }

    /**
     * The main server loop.
     */
    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        EarlyWarning.appLogger.info("Starting heartbeat server on port " + port);

        // Infinite loop
        while (true) {
            // If the server is disabled, just wait a second and loop again
            if (!active) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                continue;
            }

            // Accept a client
            Socket s;
            try {
                s = server.accept();
            } catch (IOException e) {
                EarlyWarning.appLogger.error("Could not accept client: " + e.getMessage());
                continue;
            }

            // We have to verify here, once again, that the server should be accepting requests.
            // Indeed: when the server is enabled, it will wait on server.accept while until it gets a first request.
            // Though, the server might have been disabled between the call to server.accept and the first request.
            // We add this test in case it has happened.
            if (!active) {
                EarlyWarning.appLogger.info("Received request but server is inactive.");
                continue;
            }

            // Create the necessary objects and bind them to the corresponding IO streams.
            ObjectInputStream in;
            ObjectOutputStream out;
            try {
                in = new ObjectInputStream(s.getInputStream());
                out = new ObjectOutputStream(s.getOutputStream());
            } catch (IOException e) {
                EarlyWarning.appLogger.error("Could not get IO streams for socket.");
                continue;
            }

            // Try to read an AliveRequest from the InputStream (because it is what the remote host should be sending)
            // Timeout: 10s
            AliveRequest req;
            try {
                s.setSoTimeout(10000);
                req = (AliveRequest) in.readObject();
            } catch (SocketTimeoutException ex) {
                EarlyWarning.appLogger.error("No request was sent to the server.");
                continue;
            } catch (IOException e) {
                EarlyWarning.appLogger.error("Could not read request: " + e.getMessage());
                continue;
            } catch (ClassNotFoundException ignored) {
                EarlyWarning.appLogger.error("Could not decode incoming message (Class not found).");
                continue;
            }

            EarlyWarning.appLogger.info("Received alive request: '" + req.toString() + "'");

            // Build a response and send it through the OutputStream
            AliveResponse resp = new AliveResponse();
            try {
                out.writeObject(resp);
                out.flush();
            } catch (IOException e) {
                EarlyWarning.appLogger.error("Could not write response to socket.");
            }
        }
    }
}
