package fr.ipgp.earlywarning.telephones;
/**
 * Created Mar 13, 2008 8:17:09 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
import org.junit.*;
/**
 * @author Patrice Boissier
 *
 */
public class TestTextCallList {
	public String testText;
	
	@Before
	public void setUp() {
		testText = "test text";
	}
	
	@After
	public void tearDown() {

	}
	
	@Test
	public void testCreateTextCallList() {
		TextCallList textCallList = new TextCallList(testText);
        Assert.assertEquals(testText,textCallList.getText());
        Assert.assertEquals(testText,textCallList.toString());
        Assert.assertEquals("text",textCallList.getType());
	}
	
	public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestTextCallList.class);
    }
}
