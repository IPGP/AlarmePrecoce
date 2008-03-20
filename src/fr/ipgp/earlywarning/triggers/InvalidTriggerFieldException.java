/**
 * Thrown to indicate that one or more fields of a trigger could not be determined.
 */
package fr.ipgp.earlywarning.triggers;

/**
 * @author boissier
 *
 */
public class InvalidTriggerFieldException extends Exception {
	InvalidTriggerFieldException() {
    }
	InvalidTriggerFieldException(String msg) {
        super(msg);
    }
}