/*

 */
package fr.ipgp.earlywarning.telephones;

import java.util.EventListener;

/**
 * File call list listener interface. Part of the observer pattern.
 *
 * @author patriceboissier
 */
public interface FileCallListListener extends EventListener {
    public void fileReferenceCallListChanged(FileCallListChangedEvent event);
}
