/**
 * 
 */
package fr.ipgp.earlywarning.telephones;

import java.util.EventListener;
/**
 * @author patriceboissier
 *File call list listener interface. Part of the observer pattern.
 */
public interface FileCallListListener extends EventListener {
	public void fileReferenceCallListChanged(FileCallListChangedEvent event);
}
