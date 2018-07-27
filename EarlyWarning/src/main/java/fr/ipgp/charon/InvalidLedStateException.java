package fr.ipgp.charon;

/**
 * An exception thrown when a call provides a LED state String in a wrong format.
 *
 * @author Patrice Boissier
 */
public class InvalidLedStateException extends Exception {
    private static final long serialVersionUID = 633692405123453454L;

    InvalidLedStateException(String msg) {
        super(msg);
    }
}
