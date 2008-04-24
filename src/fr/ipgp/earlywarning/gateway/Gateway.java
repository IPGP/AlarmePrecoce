/**
 * 
 */
package fr.ipgp.earlywarning.gateway;

/**
 * @author patriceboissier
 *
 */
public interface Gateway {
	public String callText(String phoneNumber, String text, boolean selfDelete);
	public String callAudio(String phoneNumber, String audioFile, boolean selfDelete);
	public String callStatus(String requestID);
	public String callRemove(String requestID);
	public String callTillConfirm(String vcastexe, String vocfile, String wavfile, String ccode, String [] phoneNumbers);
}
