package fr.ipgp.earlywarning.telephones;
/**
 * Created Mar 13, 2008 8:16:53 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */

import org.junit.*;
import java.io.*;
/**
 * @author Patrice Boissier
 *
 */
public class TestFileCallList {
	File testFile;
	
	@Before
	public void setUp() {
		testFile = new File("defaultCallList.csv");
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void testCreateFileCallList() {
		try {
			FileCallList fileCallList = new FileCallList(testFile);
			Assert.assertEquals(testFile,fileCallList.getFile());
			Assert.assertEquals(testFile.toString(),fileCallList.toString());
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestFileCallList.class);
    }
}