/**
 * Created Mar 25, 2008 13:44:50 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.telephones;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.triggers.*;
/**
 * @author boissier
 *
 */
public class PhoneCall extends Thread {
	private boolean callInProgress = false;
	private boolean moreTriggers = true;
	private Trigger trigger;
	
    public PhoneCall() {
    	this("PhoneCallManagerThread");
    }
    
    public PhoneCall(String name) {
    	super(name);
    }
   
    /**
     * @param trigger the trigger to set
     */
    public void setTrigger(Trigger trigger) {
    	this.trigger = trigger;
    	this.callInProgress = true;
    }
    
    /**
     * @return the trigger being processed
     */
    public Trigger getTrigger() {
    	if (callInProgress)
    		return trigger;
    	else
    		return null;
    }
    
	/**
	 * @return the callInProgress
	 */
	public boolean isCallInProgress() {
		return callInProgress;
	}

	/**
	 * @param callInProgress the callInProgress to set
	 */
	public void setCallInProgress(boolean callInProgress) {
		this.callInProgress = callInProgress;
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

	public void run() {
    	EarlyWarning.appLogger.debug("Thread creation");
	}
}
