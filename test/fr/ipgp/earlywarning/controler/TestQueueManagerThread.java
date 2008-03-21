package fr.ipgp.earlywarning.controler;
/**
 * Created Mar 13, 2008 10:03:49 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
import org.junit.*;
import fr.ipgp.earlywarning.triggers.*;
import fr.ipgp.earlywarning.utilities.*;
import java.util.*;

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
		Trigger trig1 = new Trigger(CommonUtilities.getUniqueId(),2);
		Trigger trig2 = new Trigger(CommonUtilities.getUniqueId(),2);
		Trigger trig3 = new Trigger(CommonUtilities.getUniqueId(),1);
		Vector<Trigger> vect1 = new Vector<Trigger>();
		Vector<Trigger> vect2 = new Vector<Trigger>();
		Vector<Trigger> vect3 = new Vector<Trigger>();
		vect1.add(trig1);
		vect2.add(trig1);
		vect2.add(trig2);
		vect3.add(trig3);
		vect3.add(trig1);
		vect3.add(trig2);
		QueueManagerThread queueManagerThread = new QueueManagerThread();
    	queueManagerThread.start();
    	queueManagerThread.addTrigger(trig1);
    	Assert.assertEquals(vect1, queueManagerThread.getQueue());
    	queueManagerThread.addTrigger(trig2);
    	Assert.assertEquals(vect2, queueManagerThread.getQueue());
    	queueManagerThread.addTrigger(trig3);
    	Assert.assertEquals(vect3, queueManagerThread.getQueue());
	}

public static junit.framework.Test suite() {
    return new junit.framework.JUnit4TestAdapter(TestQueueManagerThread.class);
}
}
