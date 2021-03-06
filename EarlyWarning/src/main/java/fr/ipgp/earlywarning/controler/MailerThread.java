/*
  Created May 07, 2008 05:28:45 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.utilities.Mailer;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Sends e-mails to a mailing list with the trigger information.<br/>
 * Implements the singleton pattern
 *
 * @author Patrice Boissier
 */
public class MailerThread extends Thread {
    private static MailerThread uniqueInstance;
    private static QueueManagerThread queueManagerThread;
    private List<InternetAddress> emails;
    private Mailer mailer;
    private String smtpUsername;
    private String smtpPassword;
    private String smtpHost;
    private String smtpPort;
    private String smtpFrom;
    private boolean useSSL;

    private MailerThread() {
        this("MailerThread");
    }

    @SuppressWarnings("SameParameterValue")
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
        } catch (ConversionException ex) {
            EarlyWarning.appLogger.error("mail or use_mail has a wrong value in configuration file: check mail section of earlywarning.xml configuration file. Mailer disabled.");
            queueManagerThread.setUseMail(false);
            return;
        } catch (NoSuchElementException ex) {
            EarlyWarning.appLogger.error("mail or use_mail is missing in configuration file: check mail section of earlywarning.xml configuration file. Mailer disabled.");
            queueManagerThread.setUseMail(false);
            return;
        }
        if (emails == null) {
            EarlyWarning.appLogger.error("No valid mails found in configuration file: check mail section of earlywarning.xml configuration file. Mailer disabled.");
            queueManagerThread.setUseMail(false);
            return;
        }
        if (emails.size() == 0) {
            EarlyWarning.appLogger.error("No valid mails found in configuration file: check mail section of earlywarning.xml configuration file. Mailer disabled.");
            queueManagerThread.setUseMail(false);
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
        useSSL = EarlyWarning.configuration.getBoolean("mail.smtp.use_ssl");

        List<HierarchicalConfiguration> fields = EarlyWarning.configuration.configurationsAt("mail.mailinglist.contact");
        List<InternetAddress> mails = new ArrayList<>();
        for (HierarchicalConfiguration sub : fields) {
            String mail = sub.getString("email");
            try {
                InternetAddress internetAddress = new InternetAddress(mail);
                internetAddress.validate();
                mails.add(internetAddress);
            } catch (AddressException ignored) {
                // Emails validity has already been checked at launch
            }
        }
        if (mails.size() == 0) {
            return null;
        }
        mailer = Mailer.getInstance(smtpHost, smtpFrom, smtpUsername, smtpPassword, smtpPort, useSSL);
        return mails;
    }

    /**
     * Send a notification to a mailing list
     *
     * @param subject the mail subject
     * @param body    the mail body
     * @throws MessagingException if the notifications cannot be sent
     */
    public void sendNotification(String subject, String body) throws MessagingException {
        mailer.sendNotificationsAuth(emails, subject, body);
    }
}
