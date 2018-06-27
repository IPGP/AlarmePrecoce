/*
  Created Apr 15, 2008 11:01:05 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.telephones;

/**
 * Thrown to indicate that the file call list name is invalid
 *
 * @author Patrice Boissier
 */
public class InvalidFileNameException extends Exception {
    private static final long serialVersionUID = 633692405543453454L;

    InvalidFileNameException() {
    }

    InvalidFileNameException(String msg) {
        super(msg);
    }
}
