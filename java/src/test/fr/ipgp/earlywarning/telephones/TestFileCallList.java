/*
  Created Mar , 2008 11:01:05 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.telephones;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * @author Patrice Boissier
 */
public class TestFileCallList {
    public String fileReference1;
    public String fileReference2;
    public String fileReference3;

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestFileCallList.class);
    }

    @Before
    public void setUp() {
        fileReference1 = "callList.voc";
        fileReference2 = "callList.txt";
        fileReference3 = "callList.toto";
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreateValidCallList() {
        try {
            File file1 = new File(fileReference1);
            File file2 = new File("/home/boissier/" + fileReference2);
            FileCallList frcl1 = new FileCallList(fileReference1);
            FileCallList frcl2 = new FileCallList(file2);
            Assert.assertEquals(fileReference1, frcl1.getFileName());
            Assert.assertEquals(CallListType.VOC, frcl1.getType());
            Assert.assertNull(frcl1.getFile().getParent());
            Assert.assertEquals(file1, frcl1.getFile());
            Assert.assertEquals(fileReference2, frcl2.getFileName());
            Assert.assertEquals(CallListType.TEXT, frcl2.getType());
            Assert.assertEquals("/home/boissier", frcl2.getFile().getParent());
            Assert.assertEquals(file2, frcl2.getFile());
        } catch (InvalidFileNameException | FileNotFoundException ifne) {
            System.out.println(ifne.getMessage());
        }
    }

    @Test
    public void testCreateInvalidCallList() {
        try {
            File file3 = new File("/home/boissier/toto.sdf");
            FileCallList frcl3 = new FileCallList(file3);
        } catch (InvalidFileNameException | FileNotFoundException ifne) {
            System.out.println(ifne.getMessage());
        }
    }
}
