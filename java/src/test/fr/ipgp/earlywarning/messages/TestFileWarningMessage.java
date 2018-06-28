package fr.ipgp.earlywarning.messages;
/*
  Created Mar 13, 2008 8:55:11 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
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
        testFile = "testFile";
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreateFileWarningMessage() {
        AudioWarningMessage audioWarningMessage = new AudioWarningMessage(testFile);
        Assert.assertEquals(testFile, audioWarningMessage.getFile());
        Assert.assertEquals(testFile, audioWarningMessage.toString());
    }
}
