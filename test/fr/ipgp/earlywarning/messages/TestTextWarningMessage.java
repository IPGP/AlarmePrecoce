package fr.ipgp.earlywarning.messages;
/**
 * Created Mar 13, 2008 9:38:42 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
import org.junit.*;

import fr.ipgp.earlywarning.messages.*;

/**
 * @author Patrice Boissier
 *
 */
public class TestTextWarningMessage {
	public String testText;
	
	@Before
	public void setUp() {
		testText = new String("test text");
	}
	
	@After
	public void tearDown() {

	}
	
	@Test
	public void testCreateTextWarningMessage () {
		TextWarningMessage textWarningMessage = new TextWarningMessage(testText);
		Assert.assertEquals(testText,textWarningMessage.getText());
        Assert.assertEquals(testText,textWarningMessage.toString());
	}
	
	public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestTextWarningMessage.class);
    }
}
