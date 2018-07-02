package fr.ipgp.earlywarning.telephones;

import fr.ipgp.earlywarning.EarlyWarning;

import java.io.IOException;
import java.util.*;

/**
 * An utility that allows to map names to <code>ContactList</code> instances.
 *
 * @author Thomas Kowalski
 */
public class ContactListMapper {
    /**
     * A map between names (<code>String</code>) and instances of corresponding <code>ContactList</code>s
     */
    private final Map<String, ContactList> mappings;

    private static ContactListMapper uniqueInstance;

    /**
     * Private constructor for the Mapper.
     *
     * @throws NoSuchListException
     * @throws IOException
     */
    @SuppressWarnings("JavaDoc")
    private ContactListMapper() throws NoSuchListException, IOException {
        mappings = new HashMap<>();

        try {
            String defaultFileName = EarlyWarning.configuration.getString("contacts.lists.default");
            ContactList defaultContactList;
            defaultContactList = ContactListBuilder.build(defaultFileName);
            mappings.put("default", defaultContactList);
        } catch (NoSuchElementException ex) {
            throw new NoSuchListException("Default list is not available ('contacts.lists.default' configuration entry).");
        }
    }

    /**
     * Constructs the ContactListMapper, which checks that the <code>default</code> list exists. If it doesn't, throws an exception.
     *
     * @throws NoSuchListException if the <code>default</code> contact list doesn't exist
     * @throws IOException         if the file corresponding to the <code>default</code> contact list can't be read
     */
    public static void testDefaultList() throws NoSuchListException, IOException {
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
    public ContactList getList(String name) throws NoSuchListException {
        if (mappings.keySet().contains(name))
            return mappings.get(name);

        try {
            String fileName = EarlyWarning.configuration.getString("contacts.lists." + name);
            ContactList list = ContactListBuilder.build(fileName);
            mappings.put(name, list);
            return list;
        } catch (NoSuchElementException e) {
            throw new NoSuchListException(name);
        } catch (IOException e) {
            throw new NoSuchListException(name);
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
        } catch (NoSuchListException e) {
            return getDefaultList();
        }
    }

    /**
     * Gets the default ContactList, as given in the configuration file.
     *
     * @return the default ContactList
     */
    public ContactList getDefaultList() {
        assert mappings.keySet().contains("default");
        try {
            return getList("default");
        } catch (NoSuchListException ignored) {
            // This can't happen: the default list is built upon object construction
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public List<String> getAvailableLists() {
        List<String> availableLists = new ArrayList<>();

        Iterator<String> it = EarlyWarning.configuration.getKeys("contacts.lists");
        for (; it.hasNext(); ) {
            String name = it.next();
            String[] split = name.split("\\.");
            availableLists.add(split[split.length - 1]);
        }

        return availableLists;
    }

    /*
    public ContactList getIgnoreCase(String name) throws Exception
    {
        if (mappings.keySet().contains(name))
            return getList(name);

        for(String key : mappings.keySet())
            if (key.equalsIgnoreCase(name))
                return mappings.get(key);

        throw new Exception("No contact list configured for given name: " + name);
    }
    */
}
