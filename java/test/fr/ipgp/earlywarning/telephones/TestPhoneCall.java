/**
 * Created Mar 25, 2008 13:35:19 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.telephones;

import org.junit.*;

import fr.ipgp.earlywarning.telephones.PhoneCall;
import fr.ipgp.earlywarning.triggers.Trigger;
import fr.ipgp.earlywarning.utilities.CommonUtilities;
/**
 * @author boissier
 *
 */
public class TestPhoneCall {

	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {

	}
	
	@Test
	public void testCreatePhoneCall() {
		Trigger trig1 = new Trigger(CommonUtilities.getUniqueId(),2);
		PhoneCall phoneCall = new PhoneCall();
		phoneCall.setTrigger(trig1);
		Assert.assertEquals(trig1, phoneCall.getTrigger());
		Assert.assertEquals(true, phoneCall.isCallInProgress());
		phoneCall.setCallInProgress(false);
		Assert.assertEquals(null,phoneCall.getTrigger());
		phoneCall.setMoreTriggers(false);
		Assert.assertEquals(false,phoneCall.isMoreTriggers());
	}
	
	public static junit.framework.Test suite() {
		return new junit.framework.JUnit4TestAdapter(TestPhoneCall.class);
	}
}
