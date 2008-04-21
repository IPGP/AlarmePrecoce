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
	public void callRemove(String requestID);
	public void callTillConfirm(String vcastexe, String vocfile, String wavfile, String ccode);
}
