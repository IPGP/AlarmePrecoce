package fr.ipgp.charon;

/**
 * An exception thrown when the Charon gateway responds with a wrongly formatted (probably corrupted) datagram.
 *
 * @author Thomas Kowalski
 */
public class InvalidResponseException extends Exception {
    private final String response;

    public InvalidResponseException(String message, String response) {
        super(message);
        this.response = response;
    }

    public String getResponse() {
        return response;
    }
}
