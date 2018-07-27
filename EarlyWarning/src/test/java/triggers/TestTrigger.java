package triggers;

/*
  Created Mar 12, 2008 10:06:05 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */

import fr.ipgp.earlywarning.contacts.*;
import fr.ipgp.earlywarning.triggers.Trigger;
import fr.ipgp.earlywarning.utilities.CommonUtilities;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.*;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import static commons.TestCommons.setUpEnvironment;

/**
 * @author Patrice Boissier
 */
public class TestTrigger {

    private long id;
    private int priority;
    private ContactList contactList;
    private String message;
    private InetAddress inetAddress;
    private String application;
    private String type;
    private boolean repeat;
    private String date;
    private String confirmCode;
    private Map<String, String> properties;

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestTrigger.class);
    }

    @BeforeClass
    public static void setUpSuite() throws IOException, ConfigurationException, NoSuchListException, ContactListBuilder.UnimplementedContactListTypeException {
        setUpEnvironment();
        ContactListMapper.testDefaultList();
    }

    @Before
    public void setUp() throws IOException {
        id = 1635132135;
        priority = 1;
        contactList = new SingleContactList("0692703856");
        message = "Alerte: tout brule!!";
        inetAddress = InetAddress.getByName("localhost");
        application = "Sismo";
        type = "v1";
        properties = new HashMap<>();
        repeat = true;
        properties.put("key1", "value1");
        properties.put("key2", "value2");
        date = "2007/02/12 10:00:00";
        confirmCode = "11";
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreateTrigger() {
        Trigger trig = new Trigger(id, 1);
        trig.setApplication(application);
        trig.setContactList(contactList);
        trig.setInetAddress(inetAddress);
        trig.setMessage(message);
        trig.setPriority(priority);
        trig.setProperty("key1", "value1");
        trig.setProperty("key2", "value2");
        trig.setType(type);
        trig.setRepeat(repeat);
        trig.setDate(date);
        trig.setConfirmCode(confirmCode);
        Assert.assertEquals(application, trig.getApplication());
        Assert.assertEquals(contactList, trig.getContactList());
        Assert.assertEquals(id, trig.getId());
        Assert.assertEquals(inetAddress, trig.getInetAddress());
        Assert.assertEquals(message, trig.getMessage());
        Assert.assertEquals(priority, trig.getPriority());
        Assert.assertEquals(properties, trig.getProperties());
        Assert.assertEquals(type, trig.getType());
        Assert.assertEquals(repeat, trig.getRepeat());
        Assert.assertEquals(date, trig.getDate());
        Assert.assertEquals(confirmCode, trig.getConfirmCode());
        Assert.assertEquals("Id: " + id + " - Priority: " + priority, trig.toString());
        Long idLong = id;
        Integer priorityInteger = priority;
        Assert.assertEquals(idLong.hashCode() + priorityInteger.hashCode(), trig.hashCode());
    }

    @Test
    public void testTriggerComparison() {
        Trigger trigger1 = new Trigger(CommonUtilities.getUniqueId(), 4);
        Trigger trigger2 = new Trigger(CommonUtilities.getUniqueId(), 1);
        Trigger trigger3 = new Trigger(CommonUtilities.getUniqueId(), 2);
        Trigger trigger4 = new Trigger(CommonUtilities.getUniqueId(), 1);
        Trigger trigger5 = new Trigger(CommonUtilities.getUniqueId(), 2);
        Assert.assertEquals(trigger1.compareTo(trigger2), 1);
        Assert.assertEquals(trigger2.compareTo(trigger3), -1);
        Assert.assertEquals(trigger3.compareTo(trigger4), 1);
        //noinspection EqualsWithItself
        Assert.assertEquals(trigger1.compareTo(trigger1), 0);
        Assert.assertEquals(trigger2.compareTo(trigger4), -1);
        Assert.assertEquals(trigger5.compareTo(trigger3), 1);
        //noinspection SimplifiableJUnitAssertion
        Assert.assertEquals(trigger1.equals(trigger2), false);
        //noinspection SimplifiableJUnitAssertion
        Assert.assertEquals(trigger2.equals(trigger4), false);
        Assert.assertEquals(trigger1, trigger1);
    }

}
