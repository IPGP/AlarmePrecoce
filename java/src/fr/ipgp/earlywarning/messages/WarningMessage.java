/*
  Created Mar 5, 2008 3:35:37 PM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.messages;

/**
 * This interface represents the warning message to be delivered by phone call
 *
 * @author Patrice Boissier
 */
public interface WarningMessage {

    /**
     * @return a String representing the object
     */
    String toString();

    WarningMessageType getType();
}
