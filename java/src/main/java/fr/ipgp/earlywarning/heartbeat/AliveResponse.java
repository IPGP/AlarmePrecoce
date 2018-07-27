package fr.ipgp.earlywarning.heartbeat;

import java.util.Date;

public class AliveResponse extends Message {
    public AliveResponse(Date date) {
        super(date);
    }

    public AliveResponse() {
        this(new Date());
    }

    public String toString() {
        return "AliveState at " + formatter.format(date);
    }
}
