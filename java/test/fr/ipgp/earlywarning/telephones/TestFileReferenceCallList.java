/**
 * 
 */
package fr.ipgp.earlywarning.telephones;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author patriceboissier
 *
 */
public class TestFileReferenceCallList {
	public String fileReference1;
	public String fileReference2;
	public String fileReference3;
	
	@Before
	public void setUp() {
		fileReference1 = new String("callList.voc");
		fileReference2 = new String("callList.txt");
		fileReference3 = new String("callList.toto");
	}
	
	@After
	public void tearDown() {

	}
	
	@Test
	public void testCreateTextCallList() {
		try {
			FileCallList frcl1 = new FileCallList(fileReference1);
			FileCallList frcl2 = new FileCallList(fileReference2);
			FileCallList frcl3 = new FileCallList(fileReference3);
	        Assert.assertEquals(fileReference1,frcl1.toString());
	        Assert.assertEquals(fileReference1,frcl1.getFileName());
	        Assert.assertEquals("voc",frcl1.getType());
	        Assert.assertEquals(fileReference2,frcl2.toString());
	        Assert.assertEquals(fileReference2,frcl2.getFileName());
	        Assert.assertEquals("txt",frcl2.getType());
	        Assert.assertEquals(fileReference3,frcl3.toString());
		} catch (InvalidFileNameException ifne) {
			System.out.println(ifne.getMessage());
		}
	}
	
	public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestFileReferenceCallList.class);
    }
}
