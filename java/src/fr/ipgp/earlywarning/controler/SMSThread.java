/**
 * Created June 02, 2016 04:04:00 PM
 * Copyright 2016 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.clickatell.ClickatellHttp;
import fr.ipgp.earlywarning.EarlyWarning;

/**
 * Sends e-mails to a mailing list with the trigger information.<br/>
 * Implements the singleton pattern
 *
 * @author Patrice Boissier
 */
public class SMSThread extends Thread {
    private static SMSThread uniqueInstance;
    private static QueueManagerThread queueManagerThread;
    private ClickatellHttp clickatell;
    private String username;
    private String password;
    private String apiId;
    private String dutyPhone;

    private SMSThread() {
        this("SMSThread");
    }

    private SMSThread(String name) {
        super(name);
    }

    public static synchronized SMSThread getInstance(QueueManagerThread queue) {
        if (uniqueInstance == null) {
            uniqueInstance = new SMSThread();
        }
        queueManagerThread = queue;
        return uniqueInstance;
    }

    public void run() {
        EarlyWarning.appLogger.debug("SMS Thread creation");
        try {
            configureSMS();
        } catch (Exception e) {
            EarlyWarning.appLogger.error("check sms section of earlywarning.xml configuration file. SMS disabled.");
            queueManagerThread.setUseSMS(false);
            return;
        }
    }

    /**
     * Configure SMS facility
     */
    private void configureSMS() {
        username = EarlyWarning.configuration.getString("sms.clickatell.username");
        password = EarlyWarning.configuration.getString("sms.clickatell.password");
        apiId = EarlyWarning.configuration.getString("sms.clickatell.api_id");
        dutyPhone = EarlyWarning.configuration.getString("sms.duty_phone");
        clickatell = new ClickatellHttp(username, apiId, password);
    }

    /**
     * Send a SMS to a duty phone
     *
     * @param message the SMS message
     * @throws Exception if the message can't be sent
     */
    public void sendSMS(String message) throws Exception {
        ClickatellHttp.Message response = clickatell.sendMessage(dutyPhone, message);
        EarlyWarning.appLogger.debug("Clickatell Response: " + response);
        if (response.error != null) {
            EarlyWarning.appLogger.debug("Clickatell Error: " + response.error);
        } else {
            EarlyWarning.appLogger.debug("Clickatell Status: " + clickatell.getMessageStatus(response.message_id));
            ClickatellHttp.Message replies = clickatell.getMessageCharge(response.message_id);
            EarlyWarning.appLogger.debug("Clickatell Charge: " + replies.charge);
        }
    }
}
