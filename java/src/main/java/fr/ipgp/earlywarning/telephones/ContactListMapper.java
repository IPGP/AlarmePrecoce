package fr.ipgp.earlywarning.telephones;

import fr.ipgp.earlywarning.EarlyWarning;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class ContactListMapper {
    private Map<String, ContactList> mappings;

    private static ContactListMapper uniqueInstance;

    private ContactListMapper() throws NoSuchListException, IOException {
        mappings = new HashMap<>();

        try {
            String defaultFileName = EarlyWarning.configuration.getString("contacts.list.default");
            ContactList defaultContactList;
            defaultContactList = ContactListBuilder.build(defaultFileName);
            mappings.put("default", defaultContactList);
        } catch (NoSuchElementException ex) {
            throw new NoSuchListException("Default list is not available.");
        }
    }

    public static void testDefaultList() throws NoSuchListException, IOException
    {
        uniqueInstance = new ContactListMapper();
    }

    public static ContactListMapper getInstance() {
        if (uniqueInstance == null) {
            EarlyWarning.appLogger.fatal("List should have already been initialized. Please verify that you have tester the default list with ContactListMapper.testDefaultList()");
            System.exit(-1);
        }

        return uniqueInstance;
    }

    public ContactList getList(String name) throws NoSuchListException {
        if (mappings.keySet().contains(name))
            return mappings.get(name);

        try {
            String fileName = EarlyWarning.configuration.getString("contacts.lists." + name);
            ContactList list = ContactListBuilder.build(fileName);
            mappings.put(name, list);
            return list;
        } catch (IOException e) {
            throw new NoSuchListException(name);
        }
    }

    public ContactList getListOrDefault(String name) {
        try {
            return getList(name);
        } catch (NoSuchListException e) {
            return getDefaultList();
        }
    }

    public ContactList getDefaultList() {
        assert mappings.keySet().contains("default");
        try {
            return getList("default");
        } catch (NoSuchListException ignored) {
            // This can't happen: the default list is built upon object construction
            return null;
        }
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
