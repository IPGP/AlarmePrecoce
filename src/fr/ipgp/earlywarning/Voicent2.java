package fr.ipgp.earlywarning;

import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.io.PrintWriter;
import java.io.InputStream;


public class Voicent2
{
  /**
   * Constructor with default localhost:8155
   */
  public Voicent2()
  {
    host_ = "localhost";
    port_ = 8155;
  }

  /**
   * Constructor with Voicent gateway hostname and port.
   * @param host Voicent gateway host machine
   * @param port Voicent gateway port number
   */
  public Voicent2(String host, int port)
  {
    host_ = host;
    port_ = port;
  }

  /**
   * Make a call to the number specified and play the text message
   * using text-to-speech engine.
   *
   * @param phoneno Phone number to call, exactly as it should be dialed
   * @param text Text to play over the phone using text-to-speech
   * @param selfdelete After the call, delete the call request automatically if set to 1
   * @return Call request ID
   */
  public String callText(String phoneno, String text, boolean selfdelete)
  {
    // call request url
    String urlstr = "/ocall/callreqHandler.jsp";

    // setting the http post string
    String poststr = "info=";
    poststr += URLEncoder.encode("Simple Text Call " + phoneno);

    poststr += "&phoneno=";
    poststr += phoneno;

    poststr += "&firstocc=10";

    poststr += "&selfdelete=";
    poststr += (selfdelete ? "1" : "0");

    poststr += "&txt=";
    poststr += URLEncoder.encode(text);

    // Send Call Request
    String rcstr = postToGateway(urlstr, poststr);

    return getReqId(rcstr);
}

  /**
   * Make a call to the number specified and play the audio file. The
   * audio file should be of PCM 8KHz, 16bit, mono.
   *
   * @param phoneno Phone number to call, exactly as it should be dialed
   * @param audiofile Audio file path name
   * @param selfdelete After the call, delete the call request automatically if set to 1
   * @return Call request ID
   */
  public String callAudio(String phoneno, String audiofile, boolean selfdelete)
  {
    // call request url
    String urlstr = "/ocall/callreqHandler.jsp";

    // setting the http post string
    String poststr = "info=";
    poststr += URLEncoder.encode("Simple Audio Call " + phoneno);

    poststr += "&phoneno=";
    poststr += phoneno;

    poststr += "&firstocc=10";

    poststr += "&selfdelete=";
    poststr += (selfdelete ? "1" : "0");

    poststr += "&audiofile=";
    poststr += URLEncoder.encode(audiofile);

    // Send Call Request
    String rcstr = postToGateway(urlstr, poststr);

    return getReqId(rcstr);
}

  /**
   * Get call status of the call with the reqID.
   *
   * @param reqID Call request ID on the gateway
   * @return call status
   */
  public String callStatus(String reqID)
  {
    // call status url
    String urlstr = "/ocall/callstatusHandler.jsp";

    // setting the http post string
    String poststr = "reqid=";
    poststr += URLEncoder.encode(reqID);

    // Send Call Request
    String rcstr = postToGateway(urlstr, poststr);

    return getCallStatus(rcstr);
  }

  /**
   * Remove all request from the gateway
   *
   * @param reqID Call request ID on the gateway
   * @return call status
   */
  public void callRemove(String reqID)
  {
    // call status url
    String urlstr = "/ocall/callremoveHandler.jsp";

    // setting the http post string
    String poststr = "reqid=";
    poststr += URLEncoder.encode(reqID);

    // Send Call remove post
    postToGateway(urlstr, poststr);
  }

  /**
   * Invoke BroadcastByPhone and start the call-till-confirm process
   *
   * @param vcastexe Executable file vcast.exe, BroadcastByPhone path name
   * @param vocfile BroadcastByPhone call list file
   * @param wavfile Audio file used for the broadcast
   * @param ccode Confirmation code
   */
  public void callTillConfirm(String vcastexe, String vocfile, String wavfile, String ccode)
  {
    // call request url
    String urlstr = "/ocall/callreqHandler.jsp";

    // setting the http post string
    String poststr = "info=";
    poststr += URLEncoder.encode("Simple Call till Confirm");

    poststr += "&phoneno=0692703856"; // any number

    poststr += "&firstocc=10";
    poststr += "&selfdelete=0";

    poststr += "&startexec=";
    poststr += URLEncoder.encode(vcastexe);

    String cmdline = "\"";
    cmdline += vocfile;
    cmdline += "\"";
    cmdline += " -startnow";
    cmdline += " -confirmcode ";
    cmdline += ccode;
    cmdline += " -wavfile ";
    cmdline += "\"";
    cmdline += wavfile;
    cmdline += "\"";
    cmdline += " -numbers";
    cmdline += " \"0692703856 0692703856\"";


    // add -cleanstatus if necessary

    poststr += "&cmdline=";
    poststr += URLEncoder.encode(cmdline);

    System.out.println("URL : " + urlstr + "?" + poststr);

    // Send like a Call Request
    postToGateway(urlstr, poststr);
  }

  private String postToGateway(String urlstr, String poststr)
  {
    try {
      URL url = new URL("http", host_, port_, urlstr);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();

      conn.setDoInput(true);
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");

      PrintWriter out = new PrintWriter(conn.getOutputStream());
      out.print(poststr);
      out.close();

      InputStream in = conn.getInputStream();

      StringBuffer rcstr = new StringBuffer();
      byte[] b = new byte[4096];
      int len;
      while ((len = in.read(b)) != -1)
        rcstr.append(new String(b, 0, len));
      return rcstr.toString();
    }
    catch (Exception e) {
      e.printStackTrace();
      return "";
    }
  }

  private String getReqId(String rcstr)
  {
    int index1 = rcstr.indexOf("[ReqId=");
    if (index1 == -1)
      return "";
    index1 += 7;

    int index2 = rcstr.indexOf("]", index1);
    if (index2 == -1)
      return "";

    return rcstr.substring(index1, index2);
  }

  private String getCallStatus(String rcstr)
  {
    if (rcstr.indexOf("^made^") != -1)
      return "Call Made";

    if (rcstr.indexOf("^failed^") != -1)
      return "Call Failed";

    if (rcstr.indexOf("^retry^") != -1)
      return "Call Will Retry";

    return "";
  }





  private String host_;
  private int port_;
}