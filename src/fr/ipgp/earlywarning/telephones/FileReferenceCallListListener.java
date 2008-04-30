/**
 * 
 */
package fr.ipgp.earlywarning.telephones;

import java.util.EventListener;
/**
 * @author patriceboissier
 *
 */
public interface FileReferenceCallListListener extends EventListener {
	public void fileReferenceCallListChanged(FileReferenceCallListChangedEvent event);
}
