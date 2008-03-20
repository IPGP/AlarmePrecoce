/**
 * 
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
