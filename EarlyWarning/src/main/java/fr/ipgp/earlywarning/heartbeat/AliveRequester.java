package fr.ipgp.earlywarning.heartbeat;

import fr.ipgp.earlywarning.EarlyWarning;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;


public class AliveRequester {
    private static Map<String, AliveRequester> instances = new HashMap<>();
    String host;
    int port;

    private AliveRequester(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static AliveRequester getInstance(String host, int port) {
        String key = host + ":" + port;
        if (instances.containsKey(key))
            return instances.get(key);

        AliveRequester newInstance = new AliveRequester(host, port);
        instances.put(key, newInstance);
        return newInstance;
    }

    public boolean getOnline() {
        AliveState response = getState();
        return response == AliveState.Alive;
    }

    public AliveState getState() {
        Socket s;
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
        AliveRequest req = new AliveRequest(getLocalIP());

        ObjectOutputStream out;
        ObjectInputStream in;
        try {
            out = new ObjectOutputStream(s.getOutputStream());
            in = new ObjectInputStream(s.getInputStream());
        } catch (IOException ex) {
            EarlyWarning.appLogger.error("Could not get IO streams for socket.");
            return AliveState.CantConnect;
        }

        try {
            out.writeObject(req);
            out.flush();
        } catch (IOException ex) {
            EarlyWarning.appLogger.error("Could not write request to socket.");
            return AliveState.CantWrite;
        }

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

    private String getLocalIP() {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            return "EarlyWarning [unknown IP]";
        }

        String name = inetAddress.getHostName().trim();
        String ip = inetAddress.getHostAddress();

        if (!name.equalsIgnoreCase(""))
            return "EarlyWarning on " + name + " (" + ip + ")";
        else
            return "EarlyWarning (" + ip + ")";
    }

}
