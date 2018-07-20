package fr.ipgp.earlywarning.contacts;

import java.io.IOException;
import java.util.List;

/**
 * Interface for managing a contact list. A class implementing this interface could use a file (JSON for example), a database, etc.
 *
 * @author Thomas Kowalski
 */
public interface ContactList {
    /**
     * Finds a contact by his / her name
     *
     * @param name the contact's name
     * @return the Contact
     * @throws NoSuchContactException if no contact exists with this name
     */
    Contact getContactByName(String name) throws NoSuchContactException;

    /**
     * Adds a contact to the available contacts
     *
     * @param contact the contact to add
     */
    void addContact(Contact contact);

    /**
     * Adds a contact to the available contacts
     *
     * @param contact    the contact to add
     * @param writeAfter if <code>true</code>, the list will be written to the disk after.
     */
    void addContact(Contact contact, boolean writeAfter);

    /**
     * Saves the contacts file.
     */
    void write() throws IOException;

    /**
     * Returns all the contacts known by the list.
     *
     * @return the contacts in the form of a {@link List}
     */
    List<Contact> getAvailableContacts();

    /**
     * Returns all the contacts currently in the call list
     *
     * @return the enabled contacts in the form of a {@link List}
     */
    List<Contact> getEnabledContacts();

    /**
     * Removes all the contacts whose name is not in <code>names</code> from the list.
     *
     * @param names the names of the contact to keep
     */
    void clean(List<String> names);

    /**
     * Updates the call list (people to be called and their order) so it reflects the names list.
     *
     * @param names the names of the contacts who should be called, by call order (possibly multiple times each)
     */
    void updateCallList(List<String> names);

    /**
     * Returns the list of the contacts to call, in the right order.
     *
     * @return the contacts to call, in the right order
     */
    List<Contact> getCallList();

    /**
     * @return the available contacts, in the form of a JSON Array
     */
    String getAvailableContactsAsJson();

    /**
     * @return the enabled contacts, in their call order, in the form of a JSON Array
     */
    String getEnabledContactsAsJson();

    @Override
    boolean equals(Object o);

}
