package fr.ipgp.earlywarning.triggersender;

public class Server {
    public final String host;
    public final int port;

    public Server(String host, int port)
    {
        this.host = host;
        this.port = port;
    }

    public Server(String host)
    {
        this(host, 4455);
    }

    public String toString()
    {
        return "EarlyWarning instance (" + host + ":" + port + ")";
    }
}
