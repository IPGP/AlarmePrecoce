/**
 * Thrown to indicate that one or more trigger fields are missing
 */
package fr.ipgp.earlywarning.triggers;

/**
 * @author boissier
 *
 */
public class MissingTriggerFieldException  extends Exception {
	private static final long serialVersionUID = 633692405405400554L;
	MissingTriggerFieldException() {
    }
	MissingTriggerFieldException(String msg) {
        super(msg);
    }
}
