package contacts;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.contacts.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Random;

import static commons.TestCommons.setUpEnvironment;
import static fr.ipgp.earlywarning.utilities.ConfigurationValidator.getItems;
import static fr.ipgp.earlywarning.utilities.FileSearch.searchForFile;


/**
 * Tests for the {@link ContactListMapper} and the {@link ContactListBuilder}
 *
 * @author Thomas Kowalski
 */
public class TestContactListUtils {
    private static File testsRoot;
    private static String defaultContactListFile;

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestContactListUtils.class);
    }

    @BeforeClass
    public static void setUp() throws ConfigurationException, NoSuchListException, IOException, ContactListBuilder.UnimplementedContactListTypeException {
        String workingDir = setUpEnvironment();

        testsRoot = new File(workingDir + "/tests");
        if (!testsRoot.isDirectory())
            if (!testsRoot.mkdirs())
                throw new IOException("Test files directory cannot be created '" + testsRoot.getCanonicalPath() + "'");

        File configurationFile = searchForFile(new File(workingDir), "earlywarning_test_contactlist.xml");
        EarlyWarning.configuration = new XMLConfiguration(configurationFile.getCanonicalPath());
        EarlyWarning.configuration.setThrowExceptionOnMissing(true);

        ContactListMapper.testDefaultList();

        defaultContactListFile = null;
        for (Map<String, String> contactListEntry : getItems("contacts.lists.list"))
            if (contactListEntry.get("id").equals("default"))
                defaultContactListFile = contactListEntry.get("path");

        if (defaultContactListFile == null)
            throw new NoSuchListException("No default list given.");
    }

    @AfterClass
    public static void tearDown() {
        File testsDir = new File(testsRoot + "/tests");
        testsDir.deleteOnExit();
    }

    /**
     * Tests that building a {@link ContactList} from a JSON file works.
     *
     * @throws IOException should not happen
     */
    @Test
    public void testBuildJson() throws IOException {
        File jsonContactListFile = new File(testsRoot.getCanonicalPath() + "/test.json");

        ContactList list = null;
        try {
            list = ContactListBuilder.build(jsonContactListFile.getCanonicalPath());
        } catch (ContactListBuilder.UnimplementedContactListTypeException e) {
            Assert.fail("JSON Contact List initialization should not fail.");
        }

        Assert.assertEquals(list.getClass(), JSONContactList.class);
    }

    /**
     * Tests that trying to build from an unknown file type will throw an exception
     *
     * @throws IOException                                              should not happen
     * @throws ContactListBuilder.UnimplementedContactListTypeException expected
     */
    @Test(expected = ContactListBuilder.UnimplementedContactListTypeException.class)
    public void testBuildUnknown() throws IOException, ContactListBuilder.UnimplementedContactListTypeException {
        File unknownContactListFile = new File(testsRoot.getCanonicalPath() + "/test.unknowntype");

        ContactList list = ContactListBuilder.build(unknownContactListFile.getPath());
    }

    /**
     * Tests that asking for an existent {@link ContactList} works and actually gives the right one, by requesting the <code>default</code> contact list.
     *
     * @throws NoSuchListException                                      should not happen
     * @throws ContactListBuilder.UnimplementedContactListTypeException should not happen
     * @throws IOException                                              should not happen
     */
    @Test
    public void testExistentMap() throws NoSuchListException, ContactListBuilder.UnimplementedContactListTypeException, IOException {
        ContactList actualDefault = ContactListBuilder.build(defaultContactListFile);
        ContactList givenDefault = ContactListMapper.getInstance().getList("default");

        Assert.assertEquals(givenDefault, actualDefault);
    }

    /**
     * Tests that requesting a non existent {@link ContactList} effectively throws a {@link NoSuchListException}
     *
     * @throws NoSuchListException                                      expected
     * @throws ContactListBuilder.UnimplementedContactListTypeException should not happen
     */
    @Test(expected = NoSuchListException.class)
    public void testNonExistentMap() throws NoSuchListException, ContactListBuilder.UnimplementedContactListTypeException {
        Random rand = new Random();
        ContactListMapper.getInstance().getList("List<" + String.valueOf(rand.nextInt()) + ">");
    }

}
