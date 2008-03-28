/**
 * Thrown to indicate that the trigger format could not be determined
 */
package fr.ipgp.earlywarning.triggers;

/**
 * @author boissier
 *
 */
public class UnknownTriggerFormatException extends Exception {
	private static final long serialVersionUID = 5720153655400554L;
	UnknownTriggerFormatException() {
    }
	UnknownTriggerFormatException(String msg) {
        super(msg);
    }
}
