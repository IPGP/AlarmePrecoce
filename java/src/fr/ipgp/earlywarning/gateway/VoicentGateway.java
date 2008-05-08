/**
 * Created Mar 25, 2008 09:29:15 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;
import fr.ipgp.earlywarning.messages.*;
import fr.ipgp.earlywarning.telephones.*;
import fr.ipgp.earlywarning.triggers.Trigger;
import fr.ipgp.earlywarning.EarlyWarning;
/**
 * Implementation of the voicent phone gateway.<br/>
 * Implements the singleton pattern.<br/>
 * <br/>
 * Outbound API is documented here : http://www.voicent.com/devnet/docs/callreqref.htm<br/>
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
 * Return values when the call is not picked :<br/>
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
 * @author Patrice Boissier
 */
public class VoicentGateway implements Gateway{
	private static VoicentGateway uniqueInstance;
	private String host;
	private String resources;
	private int port;
	private String vcastexe;
	private String encoding = "UTF-8";
	
	private VoicentGateway () {
	    this.host = "localhost";
	    this.port = 8155;
	    this.resources = null;
	}
	
	private VoicentGateway (String host, int port, String resources, String vcastexe) {
		this.host = host;
		this.port = port;
		this.resources = resources;
		this.vcastexe = vcastexe;
	}
	
    public static synchronized VoicentGateway getInstance() {
    	if (uniqueInstance == null) {
    		uniqueInstance = new VoicentGateway();
    	}
    	return uniqueInstance;
    }

    public static synchronized VoicentGateway getInstance(String host, int port, String resources, String vcastexe) {
    	if (uniqueInstance == null) {
    		uniqueInstance = new VoicentGateway(host, port, resources, vcastexe);
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
	 * @param phoneNumber Phone number to call, exactly as it should be dialed
	 * @param text Text to play over the phone using text-to-speech
	 * @param selfDelete After the call, delete the call request automatically if set to 1
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
	 * @param phoneNumber Phone number to call, exactly as it should be dialed
	 * @param audioFile Audio file path name
	 * @param selfDelete After the call, delete the call request automatically if set to 1
	 * @return Call request ID
	 */
	public String callAudio(String phoneNumber, String audioFile, boolean selfDelete){
	    try {
			String urlString = "/ocall/callreqHandler.jsp";
		    String postString = "info="+URLEncoder.encode("Simple Audio Call " + phoneNumber, encoding);
		    postString += "&phoneno="+phoneNumber;
		    postString += "&firstocc=10";
		    postString += "&selfdelete="+(selfDelete ? "1" : "0");
		    postString += "&audiofile="+URLEncoder.encode(resources + "/" + audioFile, encoding);
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
	 * @param requestID Call request ID on the gateway
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
	 * @param requestID Call request ID on the gateway
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
	
	/**
	 * Keep calling a list of people until anyone enters the confirmation code. The message is the specified audio file. 
	 * This is ideal for using it in a phone notification escalation process.
	 * @param vocFile the voc file used for logging
	 * @param waveFile the wave file to be played on the phone
	 * @param confirmCode the confirm code to be entered
	 * @param phoneNumbers the phone numbers
	 * @return call status
	 */
	public String callTillConfirm(String vocFile, String waveFile, String confirmCode, String [] phoneNumbers) {
		try {
			
			System.out.println("Voc file : " + vocFile);
			System.out.println("Wave file : " + waveFile);
			System.out.println("Confirm code : " + confirmCode);
			System.out.println("Call list : " + phoneNumbers.toString());
			
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
		    String postString = createCallTillConfirmPostString(confirmCode);
		    String cmdline = "\"" + resources + "/" + vocFile + "\"";
		    cmdline += " -startnow";
		    cmdline += " -confirmcode " + confirmCode;
		    cmdline += " -wavfile " + "\"" + resources + "/" +  waveFile + "\"";
		    cmdline += " -numbers " + "\"" + phoneNumberList + "\"";
		
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
	 * Keep calling a list of people until anyone enters the confirmation code. The message is the specified audio file. 
	 * This is ideal for using it in a phone notification escalation process.
	 * @param vocFile the voc file used for logging
	 * @param waveFile the wave file to be played on the phone
	 * @param confirmCode the confirm code to be entered
	 * @param phoneNumbers the phone numbers
	 * @return call status
	 */
	public String callTillConfirm(String vocFile, String waveFile, String confirmCode, String phoneNumbers) {
		try {			

			System.out.println("Voc file : " + vocFile);
			System.out.println("Wave file : " + waveFile);
			System.out.println("Confirm code : " + confirmCode);
			System.out.println("Call list : " + phoneNumbers);

			String urlString = "/ocall/callreqHandler.jsp";
		    String postString = createCallTillConfirmPostString(confirmCode);
		    String cmdline = "\"" + resources + "/" + vocFile + "\"";
		    cmdline += " -startnow";
		    cmdline += " -confirmcode " + confirmCode;
		    cmdline += " -wavfile " + "\"" + resources + "/" +  waveFile + "\"";
		    cmdline += " -numbers " + "\"" + phoneNumbers + "\"";
		
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
	 * Keep calling a list of people until anyone enters the confirmation code. The message is the specified audio file. 
	 * This is ideal for using it in a phone notification escalation process.
	 * @param vocFile the voc file used for logging
	 * @param waveFile the wave file to be played on the phone
	 * @param confirmCode the confirm code to be entered
	 * @param callList the phone numbers
	 * @return call status
	 */
	public String callTillConfirm(String vocFile, String waveFile, String confirmCode, FileCallList callList) {
		try {

			System.out.println("Voc file : " + vocFile);
			System.out.println("Wave file : " + waveFile);
			System.out.println("Confirm code : " + confirmCode);
			System.out.println("Call list : " + callList.toString());

		    String urlString = "/ocall/callreqHandler.jsp";
		    String postString = createCallTillConfirmPostString(confirmCode);
		    String cmdline = "\"" + resources + "/" + vocFile + "\"";
		    cmdline += " -startnow";
		    cmdline += " -confirmcode " + confirmCode;
		    cmdline += " -wavfile " + "\"" + resources + "/" +  waveFile + "\"";
		    cmdline += " -import" + "\"" + resources + "/" + callList.getFileName() + "\"";
		
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
	 * Keep calling a list of people until anyone enters the confirmation code. The message is the specified audio file. 
	 * This is ideal for using it in a phone notification escalation process.
	 * @param vocFile the voc file used for logging
	 * @param waveFile the wave file to be played on the phone
	 * @param confirmCode the confirm code to be entered
	 * @return call status
	 */
	public String callTillConfirm(String vocFile, String waveFile, String confirmCode) {
		try {
			
			System.out.println("Voc file : " + vocFile);
			System.out.println("Wave file : " + waveFile);
			System.out.println("Confirm code : " + confirmCode);

		    String urlString = "/ocall/callreqHandler.jsp";
		    String postString = createCallTillConfirmPostString(confirmCode);
		    String cmdline = "\"" + resources + "/" + vocFile + "\"";
		    cmdline += " -startnow";
		    cmdline += " -confirmcode " + confirmCode;
		    cmdline += " -wavfile " + "\"" + resources + "/" +  waveFile + "\"";
		
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
	 * 
	 */
	public String callTillConfirm(Trigger trigger, FileWarningMessage defaultWarningMessage) {		

		System.out.println("Trigger : " + trigger.showTrigger());
		System.out.println("Warning message : " + defaultWarningMessage.getFile());

		String confirmCode = trigger.getConfirmCode();
		String wavFile;
		switch (trigger.getMessage().getType()) {
			case WAV :
				FileWarningMessage fileWarningMessage = (FileWarningMessage) trigger.getMessage();
				wavFile = fileWarningMessage.getFile();
			break;
			default :
				wavFile = defaultWarningMessage.getFile();
			break;
		}
		
		try {
			switch (trigger.getCallList().getType()) {
				case VOC :
					String vocFile3 = createLogVocFile(trigger.getCallList().getName());
					return this.callTillConfirm(vocFile3, wavFile, confirmCode);
				case TXT :
					String vocFile1 = createLogVocFile();
					return this.callTillConfirm(vocFile1, wavFile, confirmCode, (FileCallList)trigger.getCallList());
				case TEXT :
					String vocFile2 = createLogVocFile();
					TextCallList callList = (TextCallList)trigger.getCallList();
					return this.callTillConfirm(vocFile2, wavFile, confirmCode, callList.getText());
				default :
					return "";
			}
		} catch (IOException ioe) {
			EarlyWarning.appLogger.fatal("Fatal error while creating log file : " + ioe.getMessage() + ". Exiting.");
			System.exit(-1);
		}
		return null;
	}

	
	/**
	 * Create the postString for the gateway
	 * @param confirmCode the confirm code
	 * @return the postString
	 */
	private String createCallTillConfirmPostString(String confirmCode){
		try {
			String postString = "info=" + URLEncoder.encode("Simple Call till Confirm", encoding);
			postString += "&phoneno=911911";
			postString += "&firstocc=10"; // the call can happen any time between now and 10 minutes later. After 10 minutes, the gateway will issue a "Too late to call" error message.
			postString += "&selfdelete=0";
			postString += "&startexec=" + URLEncoder.encode(vcastexe, encoding);
			return postString;
		} catch (UnsupportedEncodingException uee) {
	    	return null;
	    }
	}
	
	/**
	 * 
	 * @return the created log voc file
	 */
	private String createLogVocFile() throws FileNotFoundException, IOException {
		SimpleDateFormat  simpleFormat = new SimpleDateFormat("yyyyMMdd-HH-mm-ss");
		Date date = new Date();
		String logVoc = simpleFormat.format(date) + ".voc";
		File logVocFile = new File(resources + "/" + logVoc);
		File emptyVocFile = new File(resources + "/empty.voc");
		copyFile(emptyVocFile, logVocFile);
		return logVoc;
	}

	/**
	 * 
	 * @return the created log voc file
	 */
	private String createLogVocFile(String voc) throws FileNotFoundException, IOException {
		SimpleDateFormat  simpleFormat = new SimpleDateFormat("yyyyMMdd-HH-mm-ss");
		Date date = new Date();
		String logVoc = simpleFormat.format(date) + ".voc";
		File logVocFile = new File(resources + "/" + logVoc);
		File vocFile = new File(resources + "/" + voc);
		copyFile(vocFile, logVocFile);
		return logVoc;
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
	
	private void copyFile(File srcFile, File dstFile) throws FileNotFoundException, IOException {
		InputStream in = new FileInputStream(srcFile);
		OutputStream out = new FileOutputStream(dstFile);
		try{
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0){
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			System.out.println("File copied.");
		} catch(FileNotFoundException ex) {
			System.out.println(ex.getMessage() + " in the specified directory.");
		} catch(IOException e) {
			System.out.println(e.getMessage());      
		} finally {
	        if (in != null) in.close();
	        if (out != null) out.close();
	    }
	}
}
