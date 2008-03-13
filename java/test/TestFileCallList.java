/**
 * Created Mar 13, 2008 8:16:53 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */

import org.junit.*;

import java.io.*;
import fr.ipgp.earlywarning.telephones.FileCallList;
/**
 * @author Patrice Boissier
 *
 */
public class TestFileCallList {
	File testFile;
	
	@Before
	public void setUp() {
		testFile = new File("testFile");
	}
	
	@After
	public void tearDown() {
		
	}
	
	@Test
	public void testCreateFileCallList() {
		FileCallList fileCallList = new FileCallList(testFile);
		Assert.assertEquals(testFile,fileCallList.getFile());
	}
	
	public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestTrigger.class);
    }
}
