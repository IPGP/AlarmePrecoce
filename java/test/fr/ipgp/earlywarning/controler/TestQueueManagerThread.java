package fr.ipgp.earlywarning.controler;
/*
  Created Mar 13, 2008 10:03:49 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */

import fr.ipgp.earlywarning.messages.FileWarningMessage;
import fr.ipgp.earlywarning.triggers.Trigger;
import fr.ipgp.earlywarning.utilities.CommonUtilities;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * @author Patrice Boissier
 */
public class TestQueueManagerThread {

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestQueueManagerThread.class);
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreateQueueManagerThread() {
        FileWarningMessage mess = new FileWarningMessage("toto.wav");
        Trigger trig1 = new Trigger(CommonUtilities.getUniqueId(), 2);
        Trigger trig2 = new Trigger(CommonUtilities.getUniqueId(), 2);
        Trigger trig3 = new Trigger(CommonUtilities.getUniqueId(), 1);
        PriorityBlockingQueue<Trigger> pbq1 = new PriorityBlockingQueue<>();
        pbq1.offer(trig1);
        pbq1.offer(trig2);
        pbq1.offer(trig3);
        QueueManagerThread queueManagerThread = QueueManagerThread.getInstance(mess);
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
        FileWarningMessage mess = new FileWarningMessage("toto.wav");
        Trigger trig1 = new Trigger(CommonUtilities.getUniqueId(), 2);
        Trigger trig2 = new Trigger(CommonUtilities.getUniqueId(), 2);
        Trigger trig3 = new Trigger(CommonUtilities.getUniqueId(), 1);
        QueueManagerThread queueManagerThread = QueueManagerThread.getInstance(mess);
        //queueManagerThread.start();
        queueManagerThread.addTrigger(trig1);
        queueManagerThread.addTrigger(trig2);
        queueManagerThread.addTrigger(trig3);
    }
}
