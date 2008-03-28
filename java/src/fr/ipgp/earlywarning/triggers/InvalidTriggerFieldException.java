/**
 * Thrown to indicate that one or more fields of a trigger could not be determined.
 */
package fr.ipgp.earlywarning.triggers;

/**
 * @author boissier
 *
 */
public class InvalidTriggerFieldException extends Exception {
	private static final long serialVersionUID = 952405405400554L;
	InvalidTriggerFieldException() {
    }
	InvalidTriggerFieldException(String msg) {
        super(msg);
    }
}