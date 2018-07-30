package fr.ipgp.earlywarning.heartbeat;

import java.util.Date;

/**
 * An object representing a request to the main instance: "are you alive?"
 *
 * @author Thomas Kowalski
 */
public class AliveRequest extends Message {
    /**
     * A {@link String} describing the origin of the request. Typically, <code>hostname (ip)</code>/
     */
    private final String origin;

    /**
     * Valued constructor, with a custom date.
     *
     * @param date   the {@link Date} to use
     * @param origin the origin (see the <code>origin</code> attribute)
     */
    public AliveRequest(Date date, String origin) {
        this.date = date;
        this.origin = origin;
    }

    /**
     * Valued constructor using the current system date
     *
     * @param origin the origin (see the <code>origin</code> attribute)
     */
    public AliveRequest(String origin) {
        this(new Date(), origin);
    }

    /**
     * A string describing the request instance
     *
     * @return <code>AliveRequest from [origin] at [date in ISO format]</code>
     */
    public String toString() {
        return "AliveRequest from " + origin + " at " + formatter.format(this.date);
    }
}
