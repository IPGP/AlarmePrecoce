package fr.ipgp.earlywarning.triggers;

/**
 * Created Mar 12, 2008 10:06:05 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */

import java.net.*;
import java.util.*;
import java.io.*;
import org.junit.*;
import fr.ipgp.earlywarning.messages.*;
import fr.ipgp.earlywarning.telephones.*;
import fr.ipgp.earlywarning.utilities.*;
/**
 * @author Patrice Boissier
 *
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
	private Map<String,String> properties;
	
	@Before
	public void setUp() throws UnknownHostException {
		id = 1635132135;
		priority = 1;
		callList = new TextCallList("0692703856");
		message = new TextWarningMessage("Alerte : tout brule!!");
		inetAddress = InetAddress.getByName("localhost");
		application = new String("Sismo");
		type = new String("v1");
		properties = new HashMap<String,String>();
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
		Trigger trig = new Trigger(id,1);
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
		Assert.assertEquals(application,trig.getApplication());
		Assert.assertEquals(callList,trig.getCallList());
		Assert.assertEquals(id,trig.getId());
		Assert.assertEquals(inetAddress,trig.getInetAddress());
		Assert.assertEquals(message,trig.getMessage());
		Assert.assertEquals(priority,trig.getPriority());
		Assert.assertEquals(properties,trig.getProperties());
		Assert.assertEquals(type,trig.getType());
		Assert.assertEquals(repeat,trig.getRepeat());
		Assert.assertEquals(date,trig.getDate());
		Assert.assertEquals(confirmCode,trig.getConfirmCode());
        Assert.assertEquals("Id : " + id + " - Priority : " + priority, trig.toString());
        Long idLong = new Long(id);
        Integer priorityInteger = new Integer(priority);
        Assert.assertEquals(idLong.hashCode()+priorityInteger.hashCode(),trig.hashCode());
	}
		
	@Test
	public void testTriggerComparison() {
        Trigger trigger1 = new Trigger(CommonUtilities.getUniqueId(),4);
        Trigger trigger2 = new Trigger(CommonUtilities.getUniqueId(),1);
        Trigger trigger3 = new Trigger(CommonUtilities.getUniqueId(),2);
        Trigger trigger4 = new Trigger(CommonUtilities.getUniqueId(),1);
        Trigger trigger5 = new Trigger(CommonUtilities.getUniqueId(),2);
        Assert.assertEquals(trigger1.compareTo(trigger2),1);
        Assert.assertEquals(trigger2.compareTo(trigger3),-1);
        Assert.assertEquals(trigger3.compareTo(trigger4),1);
        Assert.assertEquals(trigger1.compareTo(trigger1),0);
        Assert.assertEquals(trigger2.compareTo(trigger4),-1);
        Assert.assertEquals(trigger5.compareTo(trigger3),1);
        Assert.assertFalse(trigger1.equals(trigger2));
        Assert.assertFalse(trigger2.equals(trigger4));
        Assert.assertTrue(trigger1.equals(trigger1));
	}
	
	public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestTrigger.class);
    }

}
