package fr.ipgp.earlywarning.messages;

/**
 * The exception to throw when an unknown warning message is requested by a trigger
 *
 * @author Thomas Kowalski
 */
public class NoSuchMessageException extends Exception {
    public NoSuchMessageException(String requested) {
        super(requested);
    }
}
