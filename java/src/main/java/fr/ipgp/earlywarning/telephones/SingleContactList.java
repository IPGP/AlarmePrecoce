package fr.ipgp.earlywarning.telephones;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SingleContactList implements ContactList {
    private Contact contact;
    private boolean shouldCall = true;


    public SingleContactList(String number) {
        this(number, number);
    }

    @SuppressWarnings("WeakerAccess")
    public SingleContactList(String name, String number) {
        this.contact = new Contact(name, number, false);
    }

    @Override
    public Contact getContactByName(String name) throws NoSuchContactException {
        if (name.equals(this.contact.name))
            return contact;

        throw new NoSuchContactException("Contact does not match.");
    }

    @Override
    public void addContact(Contact contact) {
        if (this.contact == null)
            this.contact = contact;
    }

    @Override
    public void addContact(Contact contact, boolean writeAfter) {
        addContact(contact);
    }

    @Override
    public List<Contact> getAvailableContacts() {
        if (contact != null)
            return Collections.singletonList(contact);
        return new ArrayList<>();
    }

    @Override
    public List<Contact> getEnabledContacts() {
        if (contact != null)
            return Collections.singletonList(contact);
        else
            return new ArrayList<>();
    }

    @Override
    public void clean(List<String> names) {
        if (contact != null)
            if (!names.contains(contact.name))
                contact = null;
    }

    @Override
    public void updateCallList(List<String> names) {
        if (contact != null)
            shouldCall = names.contains(contact.name);
    }

    @Override
    public List<Contact> getCallList() {
        if (shouldCall && contact != null)
            return Collections.singletonList(contact);
        return new ArrayList<>();
    }

    @Override
    public String getAvailableContactsAsJson() {
        if (contact == null)
            return "[]";

        JSONArray arr = new JSONArray();
        arr.put(contact);
        return arr.toString();
    }

    @Override
    public String getEnabledContactsAsJson() {
        if (contact == null || !shouldCall)
            return "[]";

        JSONArray arr = new JSONArray();
        arr.put(contact);
        return arr.toString();
    }

    public void write() {

    }
}
