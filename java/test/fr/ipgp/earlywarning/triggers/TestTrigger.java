package fr.ipgp.earlywarning.triggers;

/*
  Created Mar 12, 2008 10:06:05 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */

import fr.ipgp.earlywarning.messages.TextWarningMessage;
import fr.ipgp.earlywarning.messages.WarningMessage;
import fr.ipgp.earlywarning.telephones.CallList;
import fr.ipgp.earlywarning.telephones.TextCallList;
import fr.ipgp.earlywarning.utilities.CommonUtilities;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Patrice Boissier
 */
public class TestTrigger {

    public long id;
    public int priority;
    public CallList callList;
    public WarningMessage message;
    public InetAddress inetAddress;
    public String application;
    public String type;
    public boolean repeat;
    public String date;
    public String confirmCode;
    private Map<String, String> properties;

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestTrigger.class);
    }

    @Before
    public void setUp() throws UnknownHostException {
        id = 1635132135;
        priority = 1;
        callList = new TextCallList("0692703856");
        message = new TextWarningMessage("Alerte : tout brule!!");
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
        trig.setCallList(callList);
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
        Assert.assertEquals(callList, trig.getCallList());
        Assert.assertEquals(id, trig.getId());
        Assert.assertEquals(inetAddress, trig.getInetAddress());
        Assert.assertEquals(message, trig.getMessage());
        Assert.assertEquals(priority, trig.getPriority());
        Assert.assertEquals(properties, trig.getProperties());
        Assert.assertEquals(type, trig.getType());
        Assert.assertEquals(repeat, trig.getRepeat());
        Assert.assertEquals(date, trig.getDate());
        Assert.assertEquals(confirmCode, trig.getConfirmCode());
        Assert.assertEquals("Id : " + id + " - Priority : " + priority, trig.toString());
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
        Assert.assertEquals(trigger1.equals(trigger2), false);
        Assert.assertEquals(trigger2.equals(trigger4), false);
        Assert.assertEquals(trigger1, trigger1);
    }

}
