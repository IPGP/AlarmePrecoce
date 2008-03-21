/**
 * 
 */
package fr.ipgp.earlywarning.triggers;

/**
 * @author boissier
 *
 */
public interface TriggerConverter {
	 public Trigger getTrigger();
	 public void decode() throws UnknownTriggerFormatException, InvalidTriggerFieldException, MissingTriggerFieldException;
}
