/**
 * Created Mon 11, 2008 2:54:12 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.triggers.*;
import java.util.*;

/**
 * Manage a trigger queue based on priorities. Launch the CallManager thread.
 * @author Patrice Boissier
 *
 */
public class QueueManagerThread extends Thread {
	private Vector<Trigger> queue;
	private int nbTriggers = 0;
    protected boolean moreTriggers = true;
	
    public QueueManagerThread() {
    	this("QueueManagerThread");
    }
    
    public QueueManagerThread(String name) {
    	super(name);
    	queue = new Vector<Trigger>();
    }
    
	/**
	 * Reorder the queue
	 *
	 */
	private void sortQueue() {
		Collections.sort(queue);
	}
	
	/**
	 * Add a trigger in the queue
	 * @param trigger the trigger to add
	 */
	public void addTrigger(Trigger trigger) {
		queue.add(trigger);
		nbTriggers++;
		sortQueue();
	}
	
	/**
	 * @return queue the Queue to get
	 */
	public Vector<Trigger> getQueue() {
		return queue;
	}
	
	/**
	 * @return a string representing the queue manager 
	 */
	public String toString() {
		return nbTriggers + " Triggers : " + queue.toString();
	}
	
	public void run() {
    	EarlyWarning.appLogger.debug("Thread creation");
    	while (moreTriggers) {
    		try {
				Thread.sleep(5000);
				System.out.println("QueueManager : " + toString());
			} catch (InterruptedException ie) {
				EarlyWarning.appLogger.error("Error while sleeping!");
			}
    	}
	}
}
