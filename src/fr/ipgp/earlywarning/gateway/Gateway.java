/**
 * 
 */
package fr.ipgp.earlywarning.gateway;

/**
 * @author patriceboissier
 *
 */
public interface Gateway {
	public String callText(String phoneno, String text, boolean selfdelete);
	public String callAudio(String phoneno, String audiofile, boolean selfdelete);
	public String callStatus(String reqID);
	public void callRemove(String reqID);
	public void callTillConfirm(String vcastexe, String vocfile, String wavfile, String ccode);
}
