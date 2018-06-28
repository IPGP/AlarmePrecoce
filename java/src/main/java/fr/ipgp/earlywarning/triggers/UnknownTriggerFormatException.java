/*
  Created Mar 13, 2008 11:07:36 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.triggers;

/**
 * Thrown to indicate that the trigger format could not be determined
 *
 * @author Patrice Boissier
 */
public class UnknownTriggerFormatException extends Exception {
    private static final long serialVersionUID = 5720153655400554L;

    UnknownTriggerFormatException() {
    }

    UnknownTriggerFormatException(String msg) {
        super(msg);
    }
}
