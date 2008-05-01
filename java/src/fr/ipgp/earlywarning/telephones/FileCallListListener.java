/**
 * 
 */
package fr.ipgp.earlywarning.telephones;

import java.util.EventListener;
/**
 * @author patriceboissier
 *
 */
public interface FileCallListListener extends EventListener {
	public void fileReferenceCallListChanged(FileCallListChangedEvent event);
}
