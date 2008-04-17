/**
 * 
 */
package fr.ipgp.earlywarning.gateway;

/**
 * @author patriceboissier
 *
 */
public class TestGateway implements Gateway {
	public String callText(String phoneno, String text, boolean selfdelete) {
		return "";
	}
	public String callAudio(String phoneno, String audiofile, boolean selfdelete) {
		return "";
	}
	public String callStatus(String reqID) {
		return "";
	}
	public void callRemove(String reqID) {
		
	}
	public void callTillConfirm(String vcastexe, String vocfile, String wavfile, String ccode) {
		
	}
}
