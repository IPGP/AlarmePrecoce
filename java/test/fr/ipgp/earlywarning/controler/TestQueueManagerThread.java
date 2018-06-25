package fr.ipgp.earlywarning.controler;
/**
 * Created Mar 13, 2008 10:03:49 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
import org.junit.*;
import fr.ipgp.earlywarning.triggers.*;
import fr.ipgp.earlywarning.utilities.*;
import fr.ipgp.earlywarning.messages.*;
import java.util.concurrent.PriorityBlockingQueue;
/**
 * @author Patrice Boissier
 *
 */
public class TestQueueManagerThread {

	@Before
	public void setUp() {
		
	}
	
	@After
	public void tearDown() {

	}
	
	@Test
	public void testCreateQueueManagerThread() {
		FileWarningMessage mess = new FileWarningMessage("toto.wav");
		Trigger trig1 = new Trigger(CommonUtilities.getUniqueId(),2);
		Trigger trig2 = new Trigger(CommonUtilities.getUniqueId(),2);
		Trigger trig3 = new Trigger(CommonUtilities.getUniqueId(),1);
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
    	Assert.assertEquals(false, queueManagerThread.isMoreTriggers());
	}
	
	@Test
	public void testAddTriggerInQueueManagerThread () {
		FileWarningMessage mess = new FileWarningMessage("toto.wav");
		Trigger trig1 = new Trigger(CommonUtilities.getUniqueId(),2);
		Trigger trig2 = new Trigger(CommonUtilities.getUniqueId(),2);
		Trigger trig3 = new Trigger(CommonUtilities.getUniqueId(),1);		
		QueueManagerThread queueManagerThread = QueueManagerThread.getInstance(mess);
    	//queueManagerThread.start();
    	queueManagerThread.addTrigger(trig1);
    	queueManagerThread.addTrigger(trig2);
    	queueManagerThread.addTrigger(trig3);
	}

	public static junit.framework.Test suite() {
		return new junit.framework.JUnit4TestAdapter(TestQueueManagerThread.class);
	}
}
