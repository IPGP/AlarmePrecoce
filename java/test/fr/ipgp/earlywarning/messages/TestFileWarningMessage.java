package fr.ipgp.earlywarning.messages;
/**
 * Created Mar 13, 2008 8:55:11 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Patrice Boissier
 */
public class TestFileWarningMessage {
    String testFile;

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestFileWarningMessage.class);
    }

    @Before
    public void setUp() {
        testFile = new String("testFile");
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreateFileWarningMessage() {
        FileWarningMessage fileWarningMessage = new FileWarningMessage(testFile);
        Assert.assertEquals(testFile, fileWarningMessage.getFile());
        Assert.assertEquals(testFile.toString(), fileWarningMessage.toString());
    }
}
