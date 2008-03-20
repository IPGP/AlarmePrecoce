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
	 public boolean decode(String received);
}
