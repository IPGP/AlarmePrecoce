package fr.ipgp.earlywarning.telephones;

import jdk.nashorn.internal.runtime.regexp.joni.exception.ValueException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;

public class JSONContactList implements ContactList {
    /**
     * The (JSON) file containing the data
     */
    private final File file;
    /**
     * The contacts information
     */
    private List<Contact> contacts;
    /**
     * A list of phone numbers, in the order that they should be called in
     */
    private List<String> callList;

    /**
     * Normal constructor for the JSONContactList.
     *
     * @param path the JSON file path.
     * @throws IOException if the file can't be read and written
     */
    public JSONContactList(String path) throws IOException {
        contacts = new ArrayList<>();
        callList = new ArrayList<>();

        file = new File(path);
        if (file.isFile())
            read();
        else if (file.isDirectory())
            throw new FileAlreadyExistsException("ContactsList file '" + path + "' exists and is a directory.");
        else {
            if (!file.getParentFile().isDirectory())
                if (!file.getParentFile().mkdirs())
                    throw new IOException("Can't create directories '" + file.getParentFile().getPath() + "'");

            // Reserve the file for later use.
            initializeFile();
            read();
        }
    }

    /**
     * Initialize the JSON file with two empty lists.
     *
     * @throws IOException if the file can't be written
     */
    private void initializeFile() throws IOException {
        if (!file.isFile()) {
            if (!file.createNewFile())
                throw new IOException("Can't create file '" + file.getPath() + "'");
        }

        FileWriter writer = new FileWriter(file);
        JSONObject obj = new JSONObject();
        obj.put("available", new JSONArray());
        obj.put("enabled", new JSONArray());
        obj.write(writer);
        writer.close();
    }

    /**
     * Finds the contact corresponding to a name
     *
     * @param name the name of the contact
     * @return the corresponding contact
     * @throws ValueException if no corresponding Contact exists in the list
     */
    public Contact getContactByName(String name) {
        for (Contact c : contacts) {
            if (c.name.equals(name))
                return c;
        }

        throw new ValueException("No contact found for name " + name);
    }

    /**
     * Adds a contact and tries to save the JSON file.
     *
     * @param contact the contact to be added
     */
    @Override
    public void addContact(Contact contact) {
        addContact(contact, true);
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    /**
     * Adds a contact to the contact list.
     *
     * @param contact    the contact to be added
     * @param writeAfter if true, tries to write the JSON file after adding the contact.
     */
    public void addContact(Contact contact, boolean writeAfter) {
        contacts.add(contact);

        if (contact.priority)
            for (Contact oldContact : contacts)
                if (oldContact != contact)
                    oldContact.priority = false;

        if (writeAfter) {
            try {
                write();
            } catch (IOException ex) {
                System.err.println("Couldn't write contact list.");
            }
        }
    }

    /**
     * Writes the contacts list and the call list to the JSON file.
     *
     * @throws IOException if the file can't be written
     */
    public void write() throws IOException {
        JSONObject obj = new JSONObject();

        JSONArray available = new JSONArray();
        for (Contact c : contacts)
            available.put(c.jsonSerialize());

        JSONArray enabled = new JSONArray();
        for (String phone : callList)
            enabled.put(phone);

        obj.put("enabled", enabled);
        obj.put("available", available);

        FileWriter writer = new FileWriter(file);
        writer.write(obj.toString());
        writer.close();
    }

    /**
     * Reads the JSON file and fills local lists (call list and contacts list)
     *
     * @throws IOException if the file can't be read
     */
    private void read() throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        StringBuilder sb = new StringBuilder();
        String line;

        while ((line = bufferedReader.readLine()) != null)
            sb.append(line).append("\n");

        JSONObject main = new JSONObject(sb.toString());

        JSONArray available = main.getJSONArray("available");
        JSONArray enabled = main.getJSONArray("enabled");

        JSONObject jsonContact;

        for (int i = 0; i < available.length(); i++) {
            jsonContact = available.getJSONObject(i);
            Contact c = new Contact(jsonContact);
            contacts.add(c);
        }

        for (int i = 0; i < enabled.length(); i++) {
            callList.add(enabled.getString(i));
        }
    }

    /**
     * Finds the contacts in use in the call list.
     *
     * @return a List of enabled contacts
     */
    public List<Contact> getEnabledContacts() {
        List<Contact> result = new ArrayList<>();

        for (Contact c : contacts)
            if (callList.indexOf(c.phone) > -1)
                result.add(c);

        return result;
    }

    /**
     * Gets the names of all the contacts in the list
     *
     * @return the names of the contacts
     */
    public List<String> getNames() {
        List<String> names = new ArrayList<>();
        for (Contact c : contacts)
            names.add(c.name);

        return names;
    }

    public void clean(List<String> names) {
        List<Contact> newContacts = new ArrayList<>();

        for (Contact c : contacts)
            if (names.indexOf(c.name) > -1)
                newContacts.add(c);

        contacts = newContacts;

        List<String> existingNumbers = new ArrayList<>();
        for (Contact c : contacts)
            existingNumbers.add(c.phone);

        List<String> newCallList = new ArrayList<>();
        for (String number : callList)
            if (existingNumbers.indexOf(number) > -1)
                newCallList.add(number);
    }

    /**
     * Returns all the contacts as a JSON array
     *
     * @return a stringified JSON array of Contacts
     */
    public String getAvailableContactsAsJson() {
        JSONArray arr = new JSONArray();
        for (Contact c : contacts)
            arr.put(c.jsonSerialize());

        return arr.toString();
    }

    /**
     * Matches the names to update the local phone number list
     *
     * @param names the new list of contact names to be used
     */
    public void updateCallList(List<String> names) {
        callList.clear();
        for (String name : names)
            callList.add(getContactByName(name).phone);
    }

    /**
     * Returns the phone numbers of the call list as a JSON array
     *
     * @return a stringified JSON array of phone numbers (String)
     */
    public String getEnabledContactsAsJson() {
        JSONArray arr = new JSONArray();
        for (String phone : callList)
            arr.put(phone);

        return arr.toString();
    }
}
