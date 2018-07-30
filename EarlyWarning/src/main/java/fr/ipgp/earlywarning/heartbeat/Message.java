package fr.ipgp.earlywarning.heartbeat;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A generic Datagram class, used for communication between app instances.
 *
 * @author Thomas Kowalski
 */
public abstract class Message implements Serializable {
    /**
     * A {@link SimpleDateFormat} used to represent the date in the messages.
     */
    protected static final SimpleDateFormat formatter = new SimpleDateFormat("YYYY/MM/dd hh:mm");
    /**
     * The date associated to the message.
     */
    protected Date date;

    /**
     * (Protected) constructor used to set the date.
     *
     * @param date the date to use.
     */
    Message(Date date) {
        this.date = date;
    }

    /**
     * (Protected) constructor using the current system date.
     */
    Message() {
        this(new Date());
    }
}
