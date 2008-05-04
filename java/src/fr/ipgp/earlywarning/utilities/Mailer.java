/**
 * Created May 04, 2008 10:38:05 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.utilities;
import javax.mail.*;
import javax.mail.internet.*;
import java.util.*;
/**
 * Sends notification to a mailing list.
 * @author patriceboissier
 */
public class Mailer {
	private String host;
	private String from;
	private Properties properties;
	
	public Mailer(String host, String from) {
		this.host = host;
		this.from = from;
		properties = System.getProperties();
		properties.put("mail.smtp.host", this.host);
	}

	public void sendNotification(String to, String subject, String body) throws MessagingException {
		Session session = Session.getDefaultInstance(properties, null);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject("Hello JavaMail");
		message.setText("Welcome to JavaMail");
		Transport.send(message);
	}
	
	public void sendNotifications(String[] to, String subject, String body) throws MessagingException {
		for (String dest : to) {
			sendNotification(dest,subject,body);
		}
	}
}
