package fr.ipgp.earlywarning.messages;
/**
 * Created Mar 13, 2008 8:55:11 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */

import java.io.File;
import org.junit.*;
/**
 * @author Patrice Boissier
 *
 */
public class TestFileWarningMessage {
	File testFile;
	
	@Before
	public void setUp() {
		testFile = new File("testFile");
	}
	
	@After
	public void tearDown() {

	}
	
	@Test
	public void testCreateFileWarningMessage() {
		FileWarningMessage fileWarningMessage = new FileWarningMessage(testFile);
		Assert.assertEquals(testFile,fileWarningMessage.getFile());
		Assert.assertEquals(testFile.toString(),fileWarningMessage.toString());
	}
	
	public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestFileWarningMessage.class);
    }
}
