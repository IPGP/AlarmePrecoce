/**
 * Created Mar 25, 2008 09:20:21 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.gateway;

/**
 * The phone gateway interface
 * @author Patrice Boissier
 */
public interface Gateway {
	public String callText(String phoneNumber, String text, boolean selfDelete);
	public String callAudio(String phoneNumber, String audioFile, boolean selfDelete);
	public String callStatus(String requestID);
	public String callRemove(String requestID);
	public String callTillConfirm(String vcastexe, String vocfile, String wavfile, String ccode, String [] phoneNumbers);
}