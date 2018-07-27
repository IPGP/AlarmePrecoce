package fr.ipgp.earlywarning.heartbeat;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public abstract class Message implements Serializable {
    protected static SimpleDateFormat formatter = new SimpleDateFormat("YYYY/MM/dd hh:mm");
    protected Date date;

    public Message(Date date) {
        this.date = date;
    }

    public Message() {
        this(new Date());
    }
}
