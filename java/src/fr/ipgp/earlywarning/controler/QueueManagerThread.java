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
    private AudioSerialMessageThread audioSerialMessageThread;
	private boolean useMail;
	private boolean useSound;
	private static FileWarningMessage defaultWarningMessage;
	private int retry;
	
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
	
	/**
	 * @param useSound the useSound to set
	 */
	protected void setUseSound(boolean useSound) {
		this.useSound = useSound;
	}
	
	public void run() {
    	EarlyWarning.appLogger.debug("Thread creation");
    	
    	configureGateway();
    	
    	configureMailerThread();
    	
    	configureAudioSerialMessageThread();
    	
    	while (moreTriggers) {
    		if (queue.size() > 0) {
    			Trigger trig = queue.poll();
    			int retryCounter = 0;
    			while (gateway.callTillConfirm(trig, defaultWarningMessage) == null && retryCounter < retry) {
    				EarlyWarning.appLogger.info("Server response : null. Retrying for the " + retryCounter + " time.");
    				retryCounter++;
    			}
    			if (useMail) {
    				try {
    					mailerThread.sendNotification("[EarlyWarning] Alert from " + trig.getApplication(), trig.mailTrigger());
    	        	} catch (MessagingException me) {
    	        		EarlyWarning.appLogger.error("Error while sending notification emails : " + me.getMessage());
            		}
    			}
    			if (useSound) {
    				EarlyWarning.appLogger.debug("Opening serial port");
    				EarlyWarning.appLogger.debug("Sending text to serial port");
    				EarlyWarning.appLogger.debug("Sleeping for 10 seconds");
    				try {
    					Thread.sleep(5000);
    				} catch (InterruptedException ie) {
    					EarlyWarning.appLogger.error("Error while sleeping!");
    				}
    				EarlyWarning.appLogger.debug("Playing sound for trigger");
    				try {
    					Thread.sleep(5000);
    				} catch (InterruptedException ie) {
    					EarlyWarning.appLogger.error("Error while sleeping!");
    				}
    				EarlyWarning.appLogger.debug("Closing serial port");
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
    
    private void configureAudioSerialMessageThread() {
    	try {
   		 	useSound = EarlyWarning.configuration.getBoolean("audioserial.use_audioserial");
    	} catch (ConversionException ce) {
        	EarlyWarning.appLogger.fatal("audioserial.use_audioserial has a wrong value in configuration file : check audioserial section of earlywarning.xml configuration file. Audio/serial support disabled.");
        	useSound = false;
        } catch (NoSuchElementException nsee) {
        	EarlyWarning.appLogger.fatal("audioserial.use_audioserial is missing in configuration file : check audioserial section of earlywarning.xml configuration file. Audio/serial support disabled.");
        	useSound = false;
        }
    	if (useSound) {
    		audioSerialMessageThread = AudioSerialMessageThread.getInstance(this);
    		audioSerialMessageThread.start();
        }
    }
    
    private void configureGateway() {
    	try {
    		String host = EarlyWarning.configuration.getString("gateway.voicent.host");
    		int port = EarlyWarning.configuration.getInt("gateway.voicent.port");
    		String vcastexe = EarlyWarning.configuration.getString("gateway.voicent.vcastexe");
    		String resourcesPath = EarlyWarning.configuration.getString("gateway.voicent.resources_path");
    		retry = EarlyWarning.configuration.getInt("gateway.voicent.retry");
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
