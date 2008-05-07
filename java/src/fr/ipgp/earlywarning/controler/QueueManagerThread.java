/**
 * Created Mon 11, 2008 2:54:12 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.triggers.*;
import fr.ipgp.earlywarning.gateway.*;

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
	
	/**
	 * @param useMail the useMail to set
	 */
	protected void setUseMail(boolean useMail) {
		this.useMail = useMail;
	}
	
	public void run() {
    	EarlyWarning.appLogger.debug("Thread creation");
    	gateway = VoicentGateway.getInstance();
    	
    	configureMailerThread();

    	if (useMail) {
    		mailerThread = MailerThread.getInstance(this);
    		mailerThread.start();
        }
    	
    	while (moreTriggers) {
    		if (queue.size() > 0) {
    			try {
    				String vocFile;
    				String vcastExe = EarlyWarning.configuration.getString("gateway.voicent.vcastexe");
    				Trigger trig = queue.poll();
    				String confirmCode = trig.getConfirmCode();
    				String wavFile = "";
    				String [] phoneNumbers = {""};
    				if (trig.getCallList().getType().equals("voc"))
    					vocFile = EarlyWarning.configuration.getString("gateway.voicent.resources_path") + "/" + trig.getCallList().toString();
    				else //TODO Generer le fichier dynamiquement!!!
    					vocFile = EarlyWarning.configuration.getString("gateway.voicent.resources_path") + "/" + "log20080428.voc";
    				gateway.callTillConfirm(vcastExe, vocFile, wavFile, confirmCode, phoneNumbers);
                    if (useMail)
                    	mailerThread.sendNotification(trig.getApplication(), trig.showTrigger());
    			} catch (ConversionException ce) {
    	        	EarlyWarning.appLogger.fatal("has a wrong value in configuration file : check voicent section of earlywarning.xml configuration file. Exiting application.");
    	        } catch (NoSuchElementException nsee) {
    	        	EarlyWarning.appLogger.fatal("is missing in configuration file : check voicent section of earlywarning.xml configuration file. Exiting application.");
    	        } catch (MessagingException me) {
            		EarlyWarning.appLogger.error("Error while sending notification emails : " + me.getMessage());
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
    }
}
