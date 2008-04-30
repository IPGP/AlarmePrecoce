/**
 * Outbound API is documented here : http://www.voicent.com/devnet/docs/callreqref.htm
 * 
 * Example of use of a simple text to speech call :<br/>
 * http://195.83.188.145:8155/ocall/callreqHandler.jsp?info=SimpleTextCall0692703856&phoneno=0692703856&firstocc=10&selfdelete=0&txt=Test<br/>
 * Your call is scheduled for Mon Apr 21 11:51:18 BST 2008. [ReqId=1208775078314]<br/>
 * <br/>
 * To get status of the call :<br/>
 * http://195.83.188.145:8155/ocall/callstatusHandler.jsp?reqid=1208431448218<br/>
 * <br/>
 * Various output values from the gateway :<br/>
 * Call in progress :<br/>
 * [] <br/>
 * <br/>
 * Call made :<br/>
 * [0^null^made^2008 3 17 12 26^2008 3 17 12 25^Message left on answering machine^^^^^<br/>
 * ]<br/>
 * [0^null^made^2008 3 17 12 24^2008 3 17 12 24^Call succeeded^^^^^<br/>
 * ]<br/>
 * <br/>
 * Wrong reqID :<br/>
 * ERROR: no such call record: 1208431448217<br/>
 * <br/>
 * Test  : ne pas r�pondre ou ne pas laisser le message �tre dit :<br/>
 * Retry :<br/>
 * [0^null^retry^2008 3 18 6 18^2008 3 18 6 18^No answer. Try in 1 minute ^^^^^<br/>
 * ]<br/>
 * [0^null^retry^2008 3 18 6 18^2008 3 18 6 18^No answer. Try in 1 minute ^^^^^0^null^retry^2008 3 18 6 19^2008 3 18 6 18^No answer. Try in 1 minute ^^^^^<br/>
 * ]<br/>
 * [0^null^retry^2008 3 18 6 18^2008 3 18 6 18^No answer. Try in 1 minute ^^^^^0^null^retry^2008 3 18 6 19^2008 3 18 6 18^No answer. Try in 1 minute ^^^^^0^null^retry^2008 3 18 6 21^2008 3 18 6 18^No answer. Try in 1 minute ^^^^^<br/>
 * ]<br/>
 * [0^null^retry^2008 3 18 6 18^2008 3 18 6 18^No answer. Try in 1 minute ^^^^^0^null^retry^2008 3 18 6 19^2008 3 18 6 18^No answer. Try in 1 minute ^^^^^0^null^retry^2008 3 18 6 21^2008 3 18 6 18^No answer. Try in 1 minute ^^^^^0^null^retry^2008 3 18 6 22^2008 3 18 6 18^No answer. Try in 1 minute ^^^^^<br/>
 * ]<br/>
 * [0^null^retry^2008 3 21 13 2^2008 3 21 13 2^No answer. Try in 1 minute ^^^^^0^null^made^2008 3 21 13 3^2008 3 21 13 2^Call succeeded^^^^^<br/>
 * ]<br/>
 * <br/>
 * To remove the call :<br/>
 * http://195.83.188.145:8155/ocall/callremoveHandler.jsp?reqid=1208431448218<br/>
 * [removed]<br/>
 * If it does not exist :<br/>
 * ERROR: no such call record: 1208431448218<br/>
 */
package fr.ipgp.earlywarning.gateway;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.net.ProtocolException;
/**
 * @author patriceboissier
 *
 */
public class VoicentGateway implements Gateway{
	private static VoicentGateway uniqueInstance;
	private String host;
	private int port;
	private String encoding = "UTF-8";
	
	private VoicentGateway () {
	    this.host = "localhost";
	    this.port = 8155;
	}
	
	private VoicentGateway (String host, int port) {
		this.host = host;
		this.port = port;
	}
	
    public static synchronized VoicentGateway getInstance() {
    	if (uniqueInstance == null) {
    		uniqueInstance = new VoicentGateway();
    	}
    	return uniqueInstance;
    }

    public static synchronized VoicentGateway getInstance(String host, int port) {
    	if (uniqueInstance == null) {
    		uniqueInstance = new VoicentGateway(host, port);
    	}
    	uniqueInstance.setHost(host);
    	uniqueInstance.setPort(port);
    	return uniqueInstance;
    }

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port) {
		this.port = port;
	}

	/**  
	 * Make a call to the number specified and play the text message using text-to-speech engine.<br/>
	 * <br/>
	 * Example of return value from the Voicent Gateway :<br/>
	 * Your call is scheduled for Mon Apr 21 11:51:18 BST 2008. [ReqId=1208775078314]<br/>
	 *
	 * @param phoneno Phone number to call, exactly as it should be dialed
	 * @param text Text to play over the phone using text-to-speech
	 * @param selfdelete After the call, delete the call request automatically if set to 1
	 * @return Call request ID
	 */
	public String callText(String phoneNumber, String text, boolean selfDelete){
		//http://195.83.188.145:8155/ocall/callreqHandler.jsp?info=SimpleTextCall0692703856&phoneno=0692703856&firstocc=10&selfdelete=0&txt=Test
	    try {
			String urlString = "/ocall/callreqHandler.jsp";
		    String postString = "info="+URLEncoder.encode("Simple Text Call " + phoneNumber, encoding);
		    postString += "&phoneno="+phoneNumber;
		    postString += "&firstocc=10";
		    postString += "&selfdelete="+(selfDelete ? "1" : "0");
		    postString += "&txt="+URLEncoder.encode(text,encoding);		
		    String requestCallString = postToGateway(urlString, postString);
		    return getRequestId(requestCallString);
	    } catch (UnsupportedEncodingException uee) {
	    	return null;
	    }
	}
	
	/**
	 * Make a call to the number specified and play the audio file. The audio file should be of PCM 8KHz, 16bit, mono.
	 *
	 * @param phoneno Phone number to call, exactly as it should be dialed
	 * @param audiofile Audio file path name
	 * @param selfdelete After the call, delete the call request automatically if set to 1
	 * @return Call request ID
	 */
	public String callAudio(String phoneNumber, String audioFile, boolean selfDelete){
	    try {
			String urlString = "/ocall/callreqHandler.jsp";
		    String postString = "info="+URLEncoder.encode("Simple Audio Call " + phoneNumber, encoding);
		    postString += "&phoneno="+phoneNumber;
		    postString += "&firstocc=10";
		    postString += "&selfdelete="+(selfDelete ? "1" : "0");
		    postString += "&audiofile="+URLEncoder.encode(audioFile, encoding);
		    String requestCallString = postToGateway(urlString, postString);
		    return getRequestId(requestCallString);
	    } catch (UnsupportedEncodingException uee) {
	    	return null;
	    }
	}
	
	/**
	 * Get call status of the call with the reqID.
	 * 
	 * http://195.83.188.145:8155/ocall/callstatusHandler.jsp
	 * returns :
	 * @param reqID Call request ID on the gateway
	 * @return call status
	 */
	public String callStatus(String requestID) {
		try {  
		    String urlString = "/ocall/callstatusHandler.jsp";
		    String postString = "reqid=";
		    postString += URLEncoder.encode(requestID, encoding);
		    String requestCallString = postToGateway(urlString, postString);
		    return getCallStatus(requestCallString);
	    } catch (UnsupportedEncodingException uee) {
	    	return null;
	    }
	}
	
	/**
	 * Remove a call if this call is not in progress
	 * @param reqID Call request ID on the gateway
	 * @return the result of the remove command
	 */
	public String callRemove(String requestID) {
		try {
		    String urlString = "/ocall/callremoveHandler.jsp";
		    String postString = "reqid=";
		    postString += URLEncoder.encode(requestID, encoding);
		    String requestCallRemove = postToGateway(urlString, postString);
		    return getCallRemovedStatus(requestCallRemove);
		} catch (UnsupportedEncodingException uee) {
			return null;
		}
	}
	
	public String callTillConfirm(String vcastexe, String vocFile, String waveFile, String confirmCode, String [] phoneNumbers) {
		try {
			
			String phoneNumberList = "";
			boolean firstPhoneNumber = true;
			for (String phoneNumber : phoneNumbers) {
				if (firstPhoneNumber) {
					phoneNumberList += phoneNumber;
					firstPhoneNumber = false;
				} else {
					phoneNumberList += " " + phoneNumber;
				}
			}
			
		    String urlString = "/ocall/callreqHandler.jsp";
		    String postString = "info=" + URLEncoder.encode("Simple Call till Confirm", encoding);
		    postString += "&phoneno=666";
		    postString += "&firstocc=10"; // the call can happen any time between now and 10 minutes later. After 10 minutes, the gateway will issue a "Too late to call" error message.
		    postString += "&selfdelete=0";
		    postString += "&startexec=" + URLEncoder.encode(vcastexe, encoding);
		
		    String cmdline = "\"" + vocFile + "\"";
		    cmdline += " -startnow";
		    cmdline += " -confirmcode " + confirmCode;
		    cmdline += " -wavfile " + "\"" +  waveFile + "\"";
		    cmdline += " -numbers" + " \"" + phoneNumberList + "\"";
		    //cmdline += " -import" + " \"C:/temp/test.txt\"";
		    //cmdline += " -cleanstatus";
		
		    postString += "&cmdline=" + URLEncoder.encode(cmdline, encoding);
		
		    System.out.println("URL : " + urlString + "?" + postString);
		    String requestCallTillConfirm = postToGateway(urlString, postString);
		    System.out.println("Server answer : " + requestCallTillConfirm);
		    return requestCallTillConfirm;
	    } catch (UnsupportedEncodingException uee) {
	    	return null;
	    }
	}
	
	/**
	 * Sends a request to the gateway using the HTTP interface.
	 * @param urlString the URL string for the gateway
	 * @param postString the POST arguments for the gateway
	 * @return the return message of the gateway
	 */
	private String postToGateway(String urlString, String postString) {
		try {
			URL url = new URL("http", host, port, urlString);
			HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();

			httpConnection.setDoInput(true);
			httpConnection.setDoOutput(true);
			httpConnection.setRequestMethod("POST");

			PrintWriter out = new PrintWriter(httpConnection.getOutputStream());
			out.print(postString);
			out.close();

			InputStream in = httpConnection.getInputStream();

			StringBuffer requestCallString = new StringBuffer();
			byte[] b = new byte[4096];
			int len;
			while ((len = in.read(b)) != -1)
				requestCallString.append(new String(b, 0, len));
			return requestCallString.toString();
		} catch (MalformedURLException mue) {
			return null;
		} catch (ProtocolException mue) {
			return null;
	    } catch (IOException ioe) {
	    	return null;
	    }
	}

	/**
	 * Get the call ID from the output string from the gateway.
	 * @param receivedString from the gateway
	 * @return the call ID
	 */
	private String getRequestId(String receivedString) {
		if (receivedString == null)
			return null;
		int index1 = receivedString.indexOf("[ReqId=");
	    if (index1 == -1)
	    	return null;
	    index1 += 7;
	    int index2 = receivedString.indexOf("]", index1);
	    if (index2 == -1)
	    	return null;	
	    return receivedString.substring(index1, index2);
	}
	
	/**
	 * Get call status from the gateway.
	 * @param receivedString from the gateway
	 * @return the call status
	 */
	private String getCallStatus(String receivedString) {
	    if (receivedString.equals("[]"))
	    	return "Call in progress";
	    if (receivedString.indexOf("ERROR: no such call record:") != -1)
	    	return "No such call record";
	    String [] receivedStringSplitted = null;
	    receivedStringSplitted =receivedString.split("\\^");
	    if (receivedStringSplitted.length < 11)
	    	return null;
	    String callStatusMessage = receivedStringSplitted[receivedStringSplitted.length-6];
		if (!callStatusMessage.equals(""))
			return callStatusMessage;
	    return null;
	}
	
	/**
	 * Get the result of the remove call from the gateway.
	 * @param receivedString from the gateway
	 * @return the result of the remove command
	 */
	private String getCallRemovedStatus(String receivedString) {
	    if (receivedString.equals("[removed]"))
	    	return "Call successfully removed";
	    else
	    	return "Call id unknown"; 
	}
}
