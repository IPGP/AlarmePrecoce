/**
 * Thrown to indicate that one or more trigger fields are missing
 */
package fr.ipgp.earlywarning.triggers;

/**
 * @author boissier
 *
 */
public class MissingTriggerFieldException  extends Exception {
	MissingTriggerFieldException() {
    }
	MissingTriggerFieldException(String msg) {
        super(msg);
    }
}
