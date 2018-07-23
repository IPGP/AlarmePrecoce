package fr.ipgp.earlywarning.contacts;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link ContactList} that can contain only one contact. It is mainly use in unit tests.<br />
 * Its behaviour is simple :
 * <ul>
 * <li>Adding a {@link Contact} when the current {@link Contact} is <code>null</code> sets the {@link Contact} to the given reference ;</li>
 * <li>Adding a {@link Contact} when the current {@link Contact} is not <code>null</code> does nothing ;</li>
 * <li>Otherwise, it works just like a classic {@link ContactList}.</li>
 * </ul>
 *
 * @author Thomas Kowalski
 */
public class SingleContactList implements ContactList {
    /**
     * The only contact in the list
     */
    private Contact contact;

    /**
     * Whether or not the only contact should be called (ie whether or not the <code>callList</code> is empty
     */
    private boolean shouldCall = true;

    /**
     * Simple constructor that creates a contact with <code>name</code> = <code>number</code>
     *
     * @param number the contact's phone number
     */
    public SingleContactList(String number) {
        this(number, number);
    }

    /**
     * Valued constructor that creates the contact
     *
     * @param name   the contact's name
     * @param number the contact's phone number
     */
    @SuppressWarnings("WeakerAccess")
    public SingleContactList(String name, String number) {
        this.contact = new Contact(name, number, false);
    }

    /**
     * Default constructor (list is empty)
     */
    public SingleContactList() {
        this.contact = null;
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

    @Override
    public void updateDefaultContact(Contact c)
    {
        if (contact.equals(c))
            contact.priority = true;
    }

    @Override
    public void write() {
        // Does nothing, since this list is only temporary.
    }
}
