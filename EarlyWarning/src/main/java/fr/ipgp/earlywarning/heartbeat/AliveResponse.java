package fr.ipgp.earlywarning.heartbeat;

import java.util.Date;

/**
 * The object used to represent a response to an "Are you alive?" request.
 *
 * @author Thomas Kowalski
 */
public class AliveResponse extends Message {
    /**
     * Valued constructor with a custom date
     *
     * @param date the Date to use
     */
    public AliveResponse(Date date) {
        super(date);
    }

    /**
     * Default constructor using the current system date.
     */
    public AliveResponse() {
        this(new Date());
    }

    /**
     * A {@link String} representing the request instance.
     *
     * @return <code>AliveResponse at [date in ISO format]</code>
     */
    public String toString() {
        return "AliveResponse at " + formatter.format(date);
    }
}
