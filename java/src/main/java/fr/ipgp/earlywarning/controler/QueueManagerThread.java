/*
  Created Mon 11, 2008 2:54:12 PM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.audio.AudioSerialMessage;
import fr.ipgp.earlywarning.gateway.AsteriskGateway;
import fr.ipgp.earlywarning.gateway.Gateway;
import fr.ipgp.earlywarning.messages.NoSuchMessageException;
import fr.ipgp.earlywarning.messages.WarningMessageMapper;
import fr.ipgp.earlywarning.contacts.ContactListMapper;
import fr.ipgp.earlywarning.contacts.NoSuchListException;
import fr.ipgp.earlywarning.triggers.Trigger;
import org.apache.commons.configuration.ConversionException;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * Manage a trigger queue based on priorities.<br/>
 * Implements the singleton pattern.
 *
 * @author Patrice Boissier
 */
public class QueueManagerThread extends Thread {
    private static QueueManagerThread uniqueInstance;
    protected boolean moreTriggers = true;
    private PriorityBlockingQueue<Trigger> queue;
    private Gateway gateway;
    private String resourcesPath;
    private MailerThread mailerThread;
    private SMSThread smsThread;
    private AudioSerialMessage audioSerialMessage;
    private boolean useMail;
    private boolean useSMS;
    private boolean useSound;
    private int retry;

    private QueueManagerThread() {
        this("QueueManagerThread");
    }

    @SuppressWarnings("SameParameterValue")
    private QueueManagerThread(String name) {
        super(name);
        queue = new PriorityBlockingQueue<>();
    }

    public static synchronized QueueManagerThread getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new QueueManagerThread();
        }
        return uniqueInstance;
    }

    /**
     * Add a trigger in the queue
     *
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
     * @return a <code>String</code> representing the <code>QueueManager</code>
     */
    public String toString() {
        return queue.size() + " Trigger" + (queue.size() > 1 ? 's' : '\0') + ": " + queue.toString();
    }

    /**
     * @param useMail the useMail to set
     */
    @SuppressWarnings("SameParameterValue")
    protected void setUseMail(boolean useMail) {
        this.useMail = useMail;
    }

    /**
     * @param useSMS the useSMS to set
     */
    @SuppressWarnings("SameParameterValue")
    protected void setUseSMS(boolean useSMS) {
        this.useSMS = useSMS;
    }

    /**
     * @param useSound the useSound to set
     */
    public void setUseSound(boolean useSound) {
        this.useSound = useSound;
    }

    public void run() {
        EarlyWarning.appLogger.debug("Thread creation");

        configureGateway();

        configureMailerThread();

        configureSMSThread();

        configureAudioSerialMessage();

        while (moreTriggers) {
            if (queue.size() > 0) {
                Trigger trig = queue.poll();
                assert trig != null;

                gateway.callTillConfirm(trig);

                if (useMail) {
                    try {
                        mailerThread.sendNotification("[EarlyWarning] Alert from " + trig.getApplication(), trig.mailTrigger());
                        EarlyWarning.appLogger.info("Alert from " + trig.getApplication());
                    } catch (MessagingException me) {
                        EarlyWarning.appLogger.error("Error while sending notification emails : " + me.getMessage());
                    }
                }
                if (useSMS) {
                    try {
                        smsThread.sendSMS(trig.mailTrigger());
                    } catch (Exception e) {
                        EarlyWarning.appLogger.error("Error while sending SMS notification: " + e.getMessage());
                    }
                }
                if (useSound) {
                    audioSerialMessage.sendMessage(trig, resourcesPath);
                    while (audioSerialMessage.isPlaying()) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                            EarlyWarning.appLogger.error("Thread.sleep was interrupted.");
                        }
                    }
                }
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException ie) {
                    EarlyWarning.appLogger.error("Thread.sleep was interrupted.");
                }
            } else {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    EarlyWarning.appLogger.error("Thread.sleep was interrupted.");
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

    private void configureSMSThread() {
        try {
            useSMS = EarlyWarning.configuration.getBoolean("sms.use_sms");
        } catch (ConversionException ce) {
            EarlyWarning.appLogger.fatal("sms.use_sms has a wrong value in configuration file : check sms section of earlywarning.xml configuration file. SMS support disabled.");
            useSMS = false;
        } catch (NoSuchElementException nsee) {
            EarlyWarning.appLogger.fatal("sms.use_sms is missing in configuration file : check sms section of earlywarning.xml configuration file. SMS support disabled.");
            useSMS = false;
        }
        if (useSMS) {
            smsThread = SMSThread.getInstance(this);
            smsThread.start();
        }
    }


    private void configureAudioSerialMessage() {
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
            audioSerialMessage = AudioSerialMessage.getInstance(this);
        }
    }

    private void configureGateway() {
        String active = EarlyWarning.configuration.getString("gateway.active");
        if (active.equalsIgnoreCase("asterisk")) {
            configureAsteriskGateway();
        } else {
            EarlyWarning.appLogger.fatal("Unknown gateway in configuration: " + active + ", should be 'asterisk' (only available option at the moment).");
            System.exit(-1);
        }

        // Verify that the default warning message is available for this gateway
        try {
            WarningMessageMapper.testDefaultMessage(gateway);
        } catch (NoSuchMessageException ex) {
            EarlyWarning.appLogger.fatal("Can't find default warning sound for gateway '" + gateway.getClass().getName() + "'");
            System.exit(-1);
        }

        // Verify that the default ContactList is available for this gateway
        try {
            ContactListMapper.testDefaultList();
        } catch (NoSuchListException ex) {
            EarlyWarning.appLogger.fatal("No default contact list given.");
            System.exit(-1);
        } catch (IOException ex) {
            EarlyWarning.appLogger.fatal("Can't initialize default contact list for gateway.");
            System.exit(-1);
        }
    }

    private void configureAsteriskGateway() {
        try {
            String host = EarlyWarning.configuration.getString("gateway.asterisk.settings.ami_host");
            int port = EarlyWarning.configuration.getInt("gateway.asterisk.settings.ami_port");
            String username = EarlyWarning.configuration.getString("gateway.asterisk.settings.ami_user");
            String password = EarlyWarning.configuration.getString("gateway.asterisk.settings.ami_password");

            gateway = new AsteriskGateway(host, port, username, password);
        } catch (ConversionException e) {
            EarlyWarning.appLogger.fatal("Wrong value.");
            System.exit(-1);
        } catch (NoSuchElementException e) {
            EarlyWarning.appLogger.fatal("Missing config.");
            System.exit(-1);
        }
    }
}
