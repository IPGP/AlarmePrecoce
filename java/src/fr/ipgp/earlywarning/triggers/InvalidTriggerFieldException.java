/**
 * 
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