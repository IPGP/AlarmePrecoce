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
	 public void decode(String received) throws UnknownTriggerFormatException, InvalidTriggerFieldException, MissingTriggerFieldException;
}
