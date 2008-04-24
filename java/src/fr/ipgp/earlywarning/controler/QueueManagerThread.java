/**
 * Created Mon 11, 2008 2:54:12 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.telephones.PhoneCall;
import fr.ipgp.earlywarning.triggers.*;
import java.util.concurrent.PriorityBlockingQueue;
/**
 * Manage a trigger queue based on priorities. Launch the CallManager thread.
 * @author Patrice Boissier
 *
 */
public class QueueManagerThread extends Thread {
	private static QueueManagerThread uniqueInstance;
	private PriorityBlockingQueue<Trigger> queue;
    protected boolean moreTriggers = true;
	
    private QueueManagerThread() {
    	this("QueueManagerThread");
    }
    
    private QueueManagerThread(String name) {
    	super(name);
    	queue = new PriorityBlockingQueue<Trigger>();
    }
    
    public static synchronized QueueManagerThread getInstance() {
    	if (uniqueInstance == null) {
    		uniqueInstance = new QueueManagerThread();
    	}
    	return uniqueInstance;
    }
    	
	/**
	 * Add a trigger in the queue
	 * @param trigger the trigger to add
	 */
	public void addTrigger(Trigger trigger) {
		queue.add(trigger);
	}
	
	/**
	 * @return the moreTriggers
	 */
	public boolean isMoreTriggers() {
		return moreTriggers;
	}

	/**
	 * @param moreTriggers the moreTriggers to set
	 */
	public void setMoreTriggers(boolean moreTriggers) {
		this.moreTriggers = moreTriggers;
	}

	/**
	 * @return queue the Queue to get
	 */
	public PriorityBlockingQueue<Trigger> getQueue() {
		return queue;
	}
	
	/**
	 * @return a string representing the queue manager 
	 */
	public String toString() {
		return queue.size() + " Triggers : " + queue.toString();
	}
	
	public void run() {
    	EarlyWarning.appLogger.debug("Thread creation");
		PhoneCall phoneCall = new PhoneCall();
    	while (moreTriggers) {
    		if (queue.size() > 0) {
    			if (phoneCall.isCallInProgress()) {
    	    		try {
    					Thread.sleep(5000);
    					System.out.println("Another call is in progress. Sleeping for 5 seconds...");
    				} catch (InterruptedException ie) {
    					EarlyWarning.appLogger.error("Error while sleeping!");
    				}    				
    			} else {
    				phoneCall.setTrigger(queue.poll());
    				phoneCall.callTillConfirm();
    			}
    		} else {
	    		try {
					Thread.sleep(5000);
					//System.out.println("Waiting for triggers. Sleeping for 5 seconds...");
				} catch (InterruptedException ie) {
					EarlyWarning.appLogger.error("Error while sleeping!");
				}
    		}
    	}
	}
}
