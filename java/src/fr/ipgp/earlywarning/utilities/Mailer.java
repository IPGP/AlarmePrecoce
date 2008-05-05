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
	private Properties properties;
	
	private Mailer(String host, String from, String username, String password) {	
		this.host = host;
		this.from = from;
		this.username = username;
		this.password = password;
		properties = System.getProperties();
		properties.put("mail.smtp.host", this.host);
	}

	public static synchronized Mailer getInstance(String host, String from, String username, String password) {
    	if (uniqueInstance == null) {
    		uniqueInstance = new Mailer(host, from, username, password);
    	}
    	return uniqueInstance;
    }
	
	public void sendNotification(String to, String subject, String body) throws MessagingException {
	    String protocol = "smtp";
	    properties.put("mail." + protocol + ".auth", "true");
		Session session = Session.getDefaultInstance(properties, null);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);
		message.setText(body);

	    Transport transport = session.getTransport(protocol);
	    try {
	        transport.connect(username, password);
	        transport.sendMessage(message, message.getAllRecipients());
	    } finally {
	        transport.close();
	    }
	}
	
	public void sendNotifications(String[] tos, String subject, String body) throws MessagingException {
	    String protocol = "smtp";
	    properties.put("mail." + protocol + ".auth", "true");
		Session session = Session.getDefaultInstance(properties, null);
		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(from));
//		message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
		message.setSubject(subject);
		message.setText(body);
		message.saveChanges();
	    Transport transport = session.getTransport(protocol);
	    transport.connect(username, password);
	    for(String to : tos) {
	    	Address internetAddress = new InternetAddress(to);
	    	transport.sendMessage(message, new Address[] { internetAddress });
	    }	    
	    transport.close();
	}
}
