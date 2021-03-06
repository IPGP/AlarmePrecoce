package controler;/*
  Created Mar 13, 2008 10:03:49 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.contacts.ContactListBuilder;
import fr.ipgp.earlywarning.contacts.ContactListMapper;
import fr.ipgp.earlywarning.contacts.NoSuchListException;
import fr.ipgp.earlywarning.controler.QueueManagerThread;
import fr.ipgp.earlywarning.messages.NoSuchMessageException;
import fr.ipgp.earlywarning.messages.WarningMessageMapper;
import fr.ipgp.earlywarning.triggers.Trigger;
import fr.ipgp.earlywarning.utilities.CommonUtilities;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static commons.TestCommons.setUpEnvironment;
import static fr.ipgp.earlywarning.utilities.FileSearch.searchForFile;

/**
 * @author Patrice Boissier
 */
public class TestQueueManagerThread {

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestQueueManagerThread.class);
    }

    @Before
    public void setUp() throws IOException, ConfigurationException, NoSuchListException, ContactListBuilder.UnimplementedContactListTypeException, NoSuchMessageException {
        String workingDir = setUpEnvironment();

        File configurationFile = searchForFile(new File(workingDir), "earlywarning.xml");
        EarlyWarning.configuration = new XMLConfiguration(configurationFile.getCanonicalPath());
        EarlyWarning.configuration.setThrowExceptionOnMissing(true);

        ContactListMapper.testDefaultList();
        WarningMessageMapper.testDefaultMessage(EarlyWarning.configuration.getString("gateway.active"));
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreateQueueManagerThread() {
        Trigger trig1 = new Trigger(CommonUtilities.getUniqueId(), 2);
        Trigger trig2 = new Trigger(CommonUtilities.getUniqueId(), 2);
        Trigger trig3 = new Trigger(CommonUtilities.getUniqueId(), 1);

        QueueManagerThread queueManagerThread = QueueManagerThread.getInstance();
        queueManagerThread.start();

        queueManagerThread.addTrigger(trig1);
        queueManagerThread.addTrigger(trig2);
        queueManagerThread.addTrigger(trig3);

        Assert.assertEquals(trig3, queueManagerThread.getQueue().poll());
        Assert.assertEquals(trig1, queueManagerThread.getQueue().poll());
        Assert.assertEquals(trig2, queueManagerThread.getQueue().poll());

        queueManagerThread.setMoreTriggers(false);
        Assert.assertFalse(queueManagerThread.isMoreTriggers());
    }

    @Test
    public void testAddTriggerInQueueManagerThread() {
        Trigger trig1 = new Trigger(CommonUtilities.getUniqueId(), 2);
        Trigger trig2 = new Trigger(CommonUtilities.getUniqueId(), 2);
        Trigger trig3 = new Trigger(CommonUtilities.getUniqueId(), 1);
        QueueManagerThread queueManagerThread = QueueManagerThread.getInstance();
        //queueManagerThread.start();
        queueManagerThread.addTrigger(trig1);
        queueManagerThread.addTrigger(trig2);
        queueManagerThread.addTrigger(trig3);
    }
}
