package fr.ipgp.earlywarning.heartbeat;

import fr.ipgp.earlywarning.EarlyWarning;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The class used by backup instances to send "Are you alive?" requests to the main instance. <br />
 * <i>Implements the Singleton design pattern.</i>
 *
 * @author Thomas Kowalski
 */
public class AliveRequester {
    /**
     * There is a unique instance of the <code>Requester</code> for each main instance, that are stored in a Map:
     * <code><br />
     * {<br/>
     * 1.1.1.1 : &lt;AliveRequester instance for 1.1.1.1&gt;<br />
     * 2.2.2.2 : &lt;AliveRequester instance for 2.2.2.2&gt;<br />
     * ...<br />
     * }
     * </code>
     */
    private static final Map<String, AliveRequester> instances = new HashMap<>();

    /**
     * The host to which send requests.
     */
    final String host;

    /**
     * The port to use for this host.
     */
    final int port;

    /**
     * Valued constructor with the instance-to-request host name and the port to use.
     * @param host the instance's host name
     * @param port the port to use for TCP connections
     */
    private AliveRequester(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Returns the {@link AliveRequester} for a given host.
     * @param host the distant instance's host name
     * @param port the port to use for TCP connections to the distant instance
     * @return
     */
    public static AliveRequester getInstance(String host, int port) {
        String key = host + ":" + port;
        if (instances.containsKey(key))
            return instances.get(key);

        AliveRequester newInstance = new AliveRequester(host, port);
        instances.put(key, newInstance);
        return newInstance;
    }

    /**
     * Determines whether or not an instance is alive.
     * @return <code>true</code> if the distant instance is alive and responding, <code>false</code> otherwise.
     */
    public boolean getOnline() {
        AliveState response = getState();
        return response == AliveState.Alive;
    }

    /**
     * Determines a more detailed state than <code>getOnline</code>. See {@link AliveState}.
     * @return an {@link AliveState} corresponding to the distant instance's state.
     */
    public AliveState getState() {
        // The TCP socket to use
        Socket s;

        // Try and connect to the distant instance.
        try {
            s = new Socket(host, port);
        } catch (IOException ex) {
            EarlyWarning.appLogger.error("Could not connect to remote server. Either it is disconnected or EarlyWarning is probably not running there.");
            return AliveState.CantConnect;
        }

        try {
            s.setSoTimeout(2500);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        // Build an AliveRequest object to send
        AliveRequest req = new AliveRequest(getLocalInstanceDescription());

        // Try and bind the IO streams
        ObjectOutputStream out;
        ObjectInputStream in;
        try {
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
        } catch (IOException ex) {
            EarlyWarning.appLogger.error("Could not get IO streams for socket.");
            return AliveState.CantConnect;
        }

        // Send the AliveRequest
        try {
            out.writeObject(req);
            out.flush();
        } catch (IOException ex) {
            EarlyWarning.appLogger.error("Could not write request to socket.");
            return AliveState.CantWrite;
        }

        // Try and receive the distant instance's response (in the form of an AliveResponse object)
        AliveResponse resp;
        try {
            resp = (AliveResponse) in.readObject();
        } catch (ClassNotFoundException ex) {
            EarlyWarning.appLogger.error("Could not decode incoming message (Class not found).");
            return AliveState.Error;
        } catch (IOException e) {
            EarlyWarning.appLogger.error("Remote host did not send response.");
            return AliveState.NoResponse;
        }

        EarlyWarning.appLogger.info("Received alive response: '" + resp.toString() + "'");
        return AliveState.Alive;
    }

    /**
     * Returns the machine's IP on the network.
     * @return the machine's IP on the network in the form of a {@link String}.
     */
    private String getLocalInstanceDescription() {
        // The default String to return in case of an exception
        String DEFAULT = "EarlyWarning [unknown address]";

        // Localhost, used to determine hostname
        InetAddress localhost;
        try {
            localhost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return DEFAULT;
        }

        // Get the hostname
        String name = localhost.getHostName().trim();

        // Get the machine's IP on the network preferred outbound network (ping Google's DNS)
        String ip;
        try (final DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            ip = socket.getLocalAddress().getHostAddress();
        } catch (SocketException | UnknownHostException e) {
            return DEFAULT;
        }

        // If the hostname is empty, don't use it in the description
        if (!name.equalsIgnoreCase(""))
            return "EarlyWarning on " + name + " (" + ip + ")";
        else
            return "EarlyWarning (" + ip + ")";
    }

}
