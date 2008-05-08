/**
 * Created Mon 11, 2008 2:54:12 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.triggers.*;
import fr.ipgp.earlywarning.gateway.*;
import fr.ipgp.earlywarning.messages.*;
import java.util.NoSuchElementException;
import java.util.concurrent.PriorityBlockingQueue;
import javax.mail.MessagingException;
import org.apache.commons.configuration.ConversionException;
/**
 * Manage a trigger queue based on priorities.<br/>
 * Implements the singleton pattern.
 * @author Patrice Boissier
 */
public class QueueManagerThread extends Thread {
	private static QueueManagerThread uniqueInstance;
	private PriorityBlockingQueue<Trigger> queue;
    protected boolean moreTriggers = true;
    private Gateway gateway;
    private MailerThread mailerThread;
	private boolean useMail;
	private static FileWarningMessage defaultWarningMessage;
	
    private QueueManagerThread() {
    	this("QueueManagerThread");
    }
    
    private QueueManagerThread(String name) {
    	super(name);
    	queue = new PriorityBlockingQueue<Trigger>();
    }
    
    public static synchronized QueueManagerThread getInstance(FileWarningMessage warningMessage) {
    	if (uniqueInstance == null) {
    		uniqueInstance = new QueueManagerThread();
    	}
    	defaultWarningMessage = warningMessage;
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
	
	/**
	 * @param useMail the useMail to set
	 */
	protected void setUseMail(boolean useMail) {
		this.useMail = useMail;
	}
	
	public void run() {
    	EarlyWarning.appLogger.debug("Thread creation");
    	
    	configureGateway();
    	
    	configureMailerThread();
    	
    	while (moreTriggers) {
    		if (queue.size() > 0) {
    			Trigger trig = queue.poll();
    			gateway.callTillConfirm(trig, defaultWarningMessage);
    			if (useMail) {
    				try {
    					mailerThread.sendNotification(trig.getApplication(), trig.showTrigger());
    	        	} catch (MessagingException me) {
    	        		EarlyWarning.appLogger.error("Error while sending notification emails : " + me.getMessage());
            		}
    			}
    		} else {
	    		try {
					Thread.sleep(5000);
				} catch (InterruptedException ie) {
					EarlyWarning.appLogger.error("Error while sleeping!");
				}
    		}
    	}
	}
	
    private void configureMailerThread() {
    	try {
   		 	useMail = EarlyWarning.configuration.getBoolean("mail.use_mail");
    	} catch (ConversionException ce) {
        	EarlyWarning.appLogger.fatal("mail.use_mail has a wrong value in configuration file : check mail section of earlywarning.xml configuration file. Mail support disabled.");
        	useMail = false;
        } catch (NoSuchElementException nsee) {
        	EarlyWarning.appLogger.fatal("mail.use_mail is missing in configuration file : check mail section of earlywarning.xml configuration file. Mail support disabled.");
        	useMail = false;
        }
    	if (useMail) {
    		mailerThread = MailerThread.getInstance(this);
    		mailerThread.start();
        }
    }
    
    private void configureGateway() {
    	try {
    		String host = EarlyWarning.configuration.getString("gateway.voicent.host");
    		int port = EarlyWarning.configuration.getInt("gateway.voicent.port");
    		String vcastexe = EarlyWarning.configuration.getString("gateway.voicent.vcastexe");
    		String resourcesPath = EarlyWarning.configuration.getString("gateway.voicent.resources_path");
    		gateway = VoicentGateway.getInstance(host, port, resourcesPath, vcastexe);
    	} catch (ConversionException ce) {
        	EarlyWarning.appLogger.fatal("gateway has wrong values in configuration file : check gateway section of earlywarning.xml configuration file. Exiting...");
        	System.exit(-1);
        } catch (NoSuchElementException nsee) {
        	EarlyWarning.appLogger.fatal("gataway values are missing in configuration file : check gateway section of earlywarning.xml configuration file. Exiting...");
        	System.exit(-1);
        }
    }
}
