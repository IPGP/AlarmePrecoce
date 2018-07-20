/*
  Created May 04, 2008 10:38:05 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.utilities;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Properties;

/**
 * Sends notification to a mailing list.
 *
 * @author Patrice Boissier
 */
public class Mailer {
    private static Mailer uniqueInstance;
    private String host;
    private String from;
    private String username;
    private String password;
    private String port;
    private boolean useSSL;
    private Properties properties;

    private Mailer(String host, String from, String username, String password, String port, boolean useSSL) {
        this.host = host;
        this.from = from;
        this.username = username;
        this.password = password;
        this.port = port;
        this.useSSL = useSSL;
        properties = System.getProperties();
        if (this.useSSL) {
            properties.put("mail.smtp.host", this.host);
            properties.put("mail.smtp.socketFactory.port", this.port);
            properties.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.put("mail.smtp.port", this.port);
            properties.put("mail.smtp.user", this.username);
            properties.put("mail.smtp.auth", "true");
        } else {
            properties.put("mail.smtp.host", this.host);
            properties.put("mail.smtp.port", this.port);
            properties.put("mail.smtp.user", this.username);
            properties.put("mail.smtp.auth", "true");
        }

    }

    public static synchronized Mailer getInstance(String host, String from, String username, String password, String port, boolean useSSL) {
        if (uniqueInstance == null) {
            uniqueInstance = new Mailer(host, from, username, password, port, useSSL);
        }
        return uniqueInstance;
    }

    public void testAuthentication() throws MessagingException {
        Session session = Session.getDefaultInstance(properties, null);
        Transport transport = session.getTransport("smtp");
        transport.connect(username, password);
    }

    public void sendNotificationAuth(String to, String subject, String body) throws MessagingException {
        Session session = Session.getDefaultInstance(properties, null);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setText(body);

        try (Transport transport = session.getTransport("smtp")) {
            transport.connect(username, password);
            transport.sendMessage(message, message.getAllRecipients());
        }
    }

    public void sendNotificationsAuth(List<InternetAddress> addresses, String subject, String body) throws MessagingException {
        Session session = Session.getDefaultInstance(properties, null);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setSubject(subject);
        message.setText(body);
        message.saveChanges();
        Transport transport = session.getTransport("smtp");
        transport.connect(username, password);
        System.out.println(addresses.toString());
        for (InternetAddress address : addresses) {
            System.out.println("Sending mail to " + address);
            transport.sendMessage(message, new InternetAddress[]{address});
        }
        transport.close();
    }
}
