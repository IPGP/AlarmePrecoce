package fr.ipgp.earlywarning.contacts;

import fr.ipgp.earlywarning.EarlyWarning;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fr.ipgp.earlywarning.utilities.ConfigurationValidator.getItems;

/**
 * An utility that allows to map names to {@link ContactList} instances.
 *
 * @author Thomas Kowalski
 */
public class ContactListMapper {
    private static ContactListMapper uniqueInstance;
    /**
     * A map between names ({@link String}) and instances of corresponding {@link ContactList}s
     */
    private final Map<String, ContactList> mappings;

    /**
     * Private constructor for the Mapper.
     *
     * @throws NoSuchListException
     * @throws IOException
     */
    @SuppressWarnings("JavaDoc")
    private ContactListMapper() throws NoSuchListException, IOException, ContactListBuilder.UnimplementedContactListTypeException {
        mappings = new HashMap<>();

        String defaultFileName = null;

        for (Map<String, String> listEntry : getItems("contacts.lists.list"))
            if (listEntry.get("id").equalsIgnoreCase("default"))
                defaultFileName = listEntry.get("path");


        if (defaultFileName == null)
            throw new NoSuchListException("Default list is unavailable.");

        try {
            ContactList defaultContactList = ContactListBuilder.build(defaultFileName);
            mappings.put("default", defaultContactList);
        } catch (ContactListBuilder.UnimplementedContactListTypeException e) {
            throw new ContactListBuilder.UnimplementedContactListTypeException("Default list has an invalid extension (" + defaultFileName + ")");
        }
    }

    /**
     * Constructs the ContactListMapper, which checks that the <code>default</code> list exists. If it doesn't, throws an exception.
     *
     * @throws NoSuchListException if the <code>default</code> contact list doesn't exist
     * @throws IOException         if the file corresponding to the <code>default</code> contact list can't be read
     */
    public static void testDefaultList() throws NoSuchListException, IOException, ContactListBuilder.UnimplementedContactListTypeException {
        uniqueInstance = new ContactListMapper();
    }

    /**
     * Singleton getter
     *
     * @return the ContactListMapper unique instance
     */
    public static ContactListMapper getInstance() {
        if (uniqueInstance == null) {
            EarlyWarning.appLogger.fatal("Mapper should have already been initialized. Please verify that you have tester the default list with ContactListMapper.testDefaultList()");
            System.exit(-1);
        }

        return uniqueInstance;
    }

    /**
     * Finds and returns the list associated to a name
     *
     * @param name the name of the list to get
     * @return the requested contact list
     * @throws NoSuchListException if no list with the given name exists
     */
    public ContactList getList(String name) throws NoSuchListException, ContactListBuilder.UnimplementedContactListTypeException {
        if (mappings.keySet().contains(name))
            return mappings.get(name);

        String filename = "";
        for (Map<String, String> listEntry : getItems("contacts.lists.list")) {
            if (listEntry.get("id").equals(name))
                filename = listEntry.get("path");
        }

        if (filename.equals(""))
            throw new NoSuchListException(name);

        try {
            ContactList list = ContactListBuilder.build(filename);
            mappings.put(name, list);
            return list;
        } catch (IOException ex) {
            throw new NoSuchListException(name);
        } catch (ContactListBuilder.UnimplementedContactListTypeException ex) {
            throw new ContactListBuilder.UnimplementedContactListTypeException("Requested call list has an invalid extension (" + filename + ")");
        }
    }

    /**
     * Verifies that a list exist with the given name and returns it, otherwise returns the default list.
     *
     * @param name the name of the list to get
     * @return the requested list or the default list if it doesn't exist
     */
    public ContactList getListOrDefault(String name) {
        try {
            return getList(name);
        } catch (NoSuchListException | ContactListBuilder.UnimplementedContactListTypeException ex) {
            return getDefaultList();
        }
    }

    /**
     * Gets the default {@link ContactList}, as given in the configuration file.
     *
     * @return the default {@link ContactList}
     */
    public ContactList getDefaultList() {
        assert mappings.keySet().contains("default");
        try {
            return getList("default");
        } catch (NoSuchListException | ContactListBuilder.UnimplementedContactListTypeException ignored) {
            // This can't happen: the default list is built upon object construction
            return null;
        }
    }

    public List<String> getAvailableLists() {
        List<String> availableLists = new ArrayList<>();

        for (Map<String, String> listEntry : getItems("contacts.lists.list"))
            availableLists.add(listEntry.get("id"));

        return availableLists;
    }

    /*
    public ContactList getIgnoreCase(String name) throws Exception
    {
        if (mappings.keySet().contains(name))
            return getList(name);

        for(String key: mappings.keySet())
            if (key.equalsIgnoreCase(name))
                return mappings.get(key);

        throw new Exception("No contact list configured for given name: " + name);
    }
    */
}
