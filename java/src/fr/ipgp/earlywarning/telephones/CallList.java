/**
 * Created Mar 5, 2008 3:00:05 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.telephones;

/**
 * Call list interface.
 *
 * @author Patrice Boissier
 */
public interface CallList {
    String NAME = "name";
    String PHONE = "phone";

    /**
     * @return a String representing the object
     */
    String toString();

    /**
     * @return a String representing the CallList type
     */
    CallListType getType();

    /**
     * @return a String representing the CallList name
     */
    String getName();
}
