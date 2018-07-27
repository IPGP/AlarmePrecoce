package fr.ipgp.earlywarning.heartbeat;

import java.util.Date;

public class AliveRequest extends Message {
    private String origin;

    public AliveRequest(Date date, String origin)
    {
        this.date = date;
        this.origin = origin;
    }

    public AliveRequest(String origin)
    {
        this(new Date(), origin);
    }

    public String toString()
    {
        return "AliveRequest from " + origin + " at " + formatter.format(this.date);
    }
}
