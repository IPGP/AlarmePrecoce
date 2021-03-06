package contacts;

import fr.ipgp.earlywarning.contacts.Contact;
import fr.ipgp.earlywarning.contacts.JSONContactList;
import fr.ipgp.earlywarning.contacts.NoSuchContactException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang.NullArgumentException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static commons.TestCommons.setUpEnvironment;

/**
 * Tests for the {@link JSONContactList}
 *
 * @author Thomas Kowalski
 */
public class TestJSONContactList {
    private static File testsRoot;

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestJSONContactList.class);
    }

    @BeforeClass
    public static void setUp() throws IOException, ConfigurationException {
        setUpEnvironment();

        testsRoot = new File("tests").getCanonicalFile();

        File contactsRoot = new File(testsRoot.getCanonicalPath() + "/contacts");
        if (!contactsRoot.isDirectory())
            if (!contactsRoot.mkdirs())
                throw new IOException("Cannot create tests folder '" + contactsRoot.getCanonicalPath() + "'");
    }

    @AfterClass
    public static void tearDown() {
        testsRoot.deleteOnExit();
    }

    /**
     * Verify that, if the target file does not exist, the {@link JSONContactList} creates the necessary folders and the JSON file.
     */
    @Test
    public void testCreateNewFile() throws IOException {
        File f = new File("tests/contacts.json").getCanonicalFile();
        if (f.exists())
            Assert.assertTrue(f.delete());

        try {
            JSONContactList list = new JSONContactList("tests/contacts.json");
        } catch (IOException ignored) {
        }

        Assert.assertTrue("The JSONContactList did not create a new file.", f.exists());
    }

    /**
     * Verify that getting a contact by name works or throws a {@link NoSuchContactException} if the contact does not exist
     *
     * @throws NoSuchContactException expected: the contact does not exist
     */
    @Test(expected = NoSuchContactException.class)
    public void testGetNonExistentContact() throws NoSuchContactException, IOException {
        Contact c = new Contact("Zebulon", "0692123456", false);

        JSONContactList list = new JSONContactList("tests/contacts.json");

        list.addContact(c);

        // Try to get an existing contact
        Assert.assertEquals(list.getContactByName("Zebulon"), c);

        // This should throw an exception
        list.getContactByName("NonExistent");
    }

    /**
     * Verify that adding a null Contact throws an exception.
     */
    @Test(expected = NullArgumentException.class)
    public void testAddNullContact() throws IOException {
        JSONContactList contactList;
        contactList = new JSONContactList("tests/contacts.json");
        contactList.addContact(null);
    }

    /**
     * Verify that adding contacts to the available list works.
     */
    @Test
    public void testAddContacts() {
        Contact contact1 = new Contact("Name 1", "Phone 1", false);
        Contact contact2 = new Contact("Name 2", "Phone 2", false);
        Contact contact3 = new Contact("Name 3", "Phone 3", false);
        Contact contact4 = new Contact("Name 4", "Phone 4", false);

        JSONContactList contactList = null;
        try {
            contactList = new JSONContactList("tests/contacts.json");
        } catch (IOException ex) {
            Assert.fail("could not create the contacts.json file.");
        }

        contactList.addContact(contact1);
        contactList.addContact(contact2);
        contactList.addContact(contact3);
        contactList.addContact(contact4);

        Assert.assertTrue("Contact 1 is not in the list", contactList.getAvailableContacts().contains(contact1));
        Assert.assertTrue("Contact 2 is not in the list", contactList.getAvailableContacts().contains(contact2));
        Assert.assertTrue("Contact 3 is not in the list", contactList.getAvailableContacts().contains(contact3));
        Assert.assertTrue("Contact 4 is not in the list", contactList.getAvailableContacts().contains(contact4));
    }

    /**
     * Verify that the list correctly saves the file after adding a new contact and that a new {@link JSONContactList} finds the correct data in the file.
     *
     * @throws IOException if the file cannot be created
     */
    @Test
    public void testWriteAndLoadContacts() throws IOException {
        Contact contact1 = new Contact("Contact", "Phone number", false);

        JSONContactList contactList = null;
        try {
            contactList = new JSONContactList("tests/contacts.json");
        } catch (IOException ex) {
            Assert.fail("could not create the contacts.json file.");
        }

        contactList.addContact(contact1);
        contactList.write();

        try {
            contactList = new JSONContactList("tests/contacts.json");
        } catch (IOException ex) {
            Assert.fail("could not create the contacts.json file.");
        }

        Assert.assertTrue("The contact list did not load the contacts.", contactList.getAvailableContacts().contains(contact1));
    }

    /**
     * Test that adding a contact to the enabled list actually adds it and that removing one from the names also removes it from the available list.
     */
    @Test
    public void testUpdateAndCleanContacts() {
        Contact contact1 = new Contact("Keyboard", "0", false);
        Contact contact2 = new Contact("Mouse", "1", false);
        Contact contact3 = new Contact("Screen", "2", false);
        Contact contact4 = new Contact("Computer", "3", false);

        JSONContactList contactList = null;
        try {
            contactList = new JSONContactList("tests/contacts.json");
        } catch (IOException ex) {
            Assert.fail("could not create the contacts.json file.");
        }

        // Add all the contacts
        contactList.addContact(contact1);
        contactList.addContact(contact2);
        contactList.addContact(contact3);
        contactList.addContact(contact4);

        Assert.assertTrue("The enabled contacts should be empty.", contactList.getEnabledContacts().isEmpty());

        // Only keep three of them (that means one was removed from the web interface)
        // This means the user has chosen to call Keyboard three times, then Mouse, then Screen, then Mouse again
        List<String> names = new ArrayList<>(Arrays.asList("Keyboard", "Keyboard", "Keyboard", "Mouse", "Screen", "Mouse"));

        // Update the enabled contacts
        contactList.updateCallList(names);

        // Remove all the unused contacts
        contactList.clean(names);

        // Verify 'Mouse' was correctly added to the enabled contacts
        Assert.assertTrue("'Mouse' was not in the enabled contacts.", contactList.getEnabledContacts().contains(contact2));

        // Verify 'Computer' has been deleted from the list
        Assert.assertFalse("'Computer' was not removed from the AvailableList", contactList.getAvailableContacts().contains(contact4));
    }

}
