package fr.ipgp.earlywarning.telephones;

import java.util.List;

public interface ContactList {
    Contact getContactByName(String name);

    void addContact(Contact contact);

    void addContact(Contact contact, boolean writeAfter);

    List<Contact> getEnabledContacts();

    void clean(List<String> names);

    void updateCallList(List<String> names);

    String getAvailableContactsAsJson();

    String getEnabledContactsAsJson();

}
