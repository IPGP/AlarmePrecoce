/**
 * Created May 07, 2008 05:28:45 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.mail.internet.AddressException;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.utilities.Mailer;
/**
 * Sends e-mails to a mailing list with the trigger information.<br/>
 * Implements the singleton pattern
 * @author Patrice Boissier
 */
public class MailerThread extends Thread {
	private static MailerThread uniqueInstance;
	private List<InternetAddress> emails;
	private Mailer mailer;
	private String smtpUsername;
	private String smtpPassword;
	private String smtpHost;
	private String smtpPort;
	private String smtpFrom;
	private static QueueManagerThread queueManagerThread;
	
	private MailerThread() {
		this("MailerThread");
	}
	
	private MailerThread(String name) {
		super(name);
	}
	
	public static synchronized MailerThread getInstance(QueueManagerThread queue) {
		if (uniqueInstance == null) {
    		uniqueInstance = new MailerThread();
    	}
		queueManagerThread = queue;
    	return uniqueInstance;
	}
	
    public void run() {
    	EarlyWarning.appLogger.debug("Mailer Thread creation");
    	try {
    		emails = configureMailer();
    	} catch (ConversionException ce) {
        	EarlyWarning.appLogger.error("mail or use_mail has a wrong value in configuration file : check mail section of earlywarning.xml configuration file. Mailer disabled.");
        	queueManagerThread.setUseMail(false);
        	return;
        } catch (NoSuchElementException nsee) {
        	EarlyWarning.appLogger.error("mail or use_mail is missing in configuration file : check mail section of earlywarning.xml configuration file. Mailer disabled.");
        	queueManagerThread.setUseMail(false);
        	return;
        }
        if (emails == null) {
        	EarlyWarning.appLogger.error("No valid mails found in configuration file : check mail section of earlywarning.xml configuration file. Mailer disabled.");
        	queueManagerThread.setUseMail(false);
        	return;        	
        }
        if (emails.size() == 0) {
        	EarlyWarning.appLogger.error("No valid mails found in configuration file : check mail section of earlywarning.xml configuration file. Mailer disabled.");
        	queueManagerThread.setUseMail(false);
        	return;
        }
    }
    
    /**
     * Configure Mail facility
     */
    private List<InternetAddress> configureMailer() throws ConversionException, NoSuchElementException {
    	smtpHost = EarlyWarning.configuration.getString("mail.smtp.host");
    	smtpUsername = EarlyWarning.configuration.getString("mail.smtp.username");
    	smtpPassword = EarlyWarning.configuration.getString("mail.smtp.password");
    	smtpFrom = EarlyWarning.configuration.getString("mail.smtp.from");
    	smtpPort = EarlyWarning.configuration.getString("mail.smtp.port");
		List<XMLConfiguration> fields = EarlyWarning.configuration.configurationsAt("mail.mailinglist.contact");
		List<InternetAddress> mails = new ArrayList<InternetAddress>();
		for(Iterator<XMLConfiguration> it = fields.iterator(); it.hasNext();) {
			HierarchicalConfiguration sub = (HierarchicalConfiguration) it.next();
			String mail = sub.getString("email");
			try {
				InternetAddress internetAddress = new InternetAddress(mail);
				internetAddress.validate();
				mails.add(internetAddress);
			} catch (AddressException ae) {
				EarlyWarning.appLogger.error("Invalid E-mail address in configuration file : " + mail + " check mail.mailinglist section of earlywarning.xml configuration file. Address not added to the notification system.");
			}    
		}
		if (mails.size() == 0) {
			return null;
		}
		mailer = Mailer.getInstance(smtpHost, smtpFrom, smtpUsername, smtpPassword, smtpPort);
		return mails;
    }
    
    /**
     * Send a notification to a mailing list
     * @param subject the mail subject
     * @param body the mail body
     * @throws MessagingException
     */
    public void sendNotification(String subject, String body) throws MessagingException{
    	mailer.sendNotificationsAuth(emails, subject, body);
    }
}
