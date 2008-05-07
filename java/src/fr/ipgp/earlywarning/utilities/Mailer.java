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
	private static Mailer uniqueInstance;
	private String host;
	private String from;
	private String username;
	private String password;
	private String port;
	private Properties properties;
	
	private Mailer(String host, String from, String username, String password, String port) {	
		this.host = host;
		this.from = from;
		this.username = username;
		this.password = password;
		this.port = port;
		properties = System.getProperties();
		properties.put("mail.smtp.host", this.host);
		properties.put("mail.smtp.user", this.username);
		properties.put("mail.smtp.port", this.port);
	}

	public static synchronized Mailer getInstance(String host, String from, String username, String password, String port) {
    	if (uniqueInstance == null) {
    		uniqueInstance = new Mailer(host, from, username, password, port);
    	}
    	return uniqueInstance;
    }
	
	public void sendNotificationAuth(String to, String subject, String body) throws MessagingException {
	    properties.put("mail.smtp.auth", "true");
		Session session = Session.getDefaultInstance(properties, null);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);
		message.setText(body);

	    Transport transport = session.getTransport("smtp");
	    try {
	        transport.connect(username, password);
	        transport.sendMessage(message, message.getAllRecipients());
	    } finally {
	        transport.close();
	    }
	}
	
	public void sendNotificationsAuth(List<InternetAddress> addresses, String subject, String body) throws MessagingException {
		properties.put("mail.smtp.auth", "true");
		Session session = Session.getDefaultInstance(properties, null);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.setSubject(subject);
		message.setText(body);
		message.saveChanges();
	    Transport transport = session.getTransport("smtp");
	    transport.connect(username, password);
	    System.out.println(addresses.toString());
	    for(InternetAddress address : addresses) {
	    	System.out.println("Sending mail to " + address);
	    	transport.sendMessage(message, new InternetAddress[] { address });
	    }	    
	    transport.close();
	}
}
