/**
 * Thrown to indicate that the trigger format could not be determined
 */
package fr.ipgp.earlywarning.triggers;

/**
 * @author boissier
 *
 */
public class UnknownTriggerFormatException extends Exception {
	UnknownTriggerFormatException() {
    }
	UnknownTriggerFormatException(String msg) {
        super(msg);
    }
}
