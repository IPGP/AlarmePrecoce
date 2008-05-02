/**
 * Created Mar 13, 2008 11:07:36 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.triggers;

/**
 * Thrown to indicate that one or more trigger fields are missing
 * @author Patrice Boissier
 */
public class MissingTriggerFieldException  extends Exception {
	private static final long serialVersionUID = 633692405405400554L;
	MissingTriggerFieldException() {
    }
	MissingTriggerFieldException(String msg) {
        super(msg);
    }
}
