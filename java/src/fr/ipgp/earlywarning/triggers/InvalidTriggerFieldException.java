/**
 * Created Mar 13, 2008 11:07:36 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.triggers;

/**
 * Thrown to indicate that one or more fields of a trigger could not be determined.
 *
 * @author Patrice Boissier
 */
public class InvalidTriggerFieldException extends Exception {
    private static final long serialVersionUID = 952405405400554L;

    InvalidTriggerFieldException() {
    }

    InvalidTriggerFieldException(String msg) {
        super(msg);
    }
}