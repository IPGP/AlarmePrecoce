/*
 * Voicent Communucations, Inc Sample Code
 * http://www.voicent.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL VOICENT COMMUNICATIONS, INC OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * ----------------------------------------------------------------------
 * this class is listed online under the simple call interface java
 */

package com.voicent.webalert;

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class Voicent {
    private String host_;
    private int port_;

    /**
     * Constructor with default localhost:8155
     */
    public Voicent() {
        host_ = "localhost";
        port_ = 8155;
    }

    /**
     * Constructor with Voicent gateway hostname and port.
     *
     * @param host Voicent gateway host machine
     * @param port Voicent gateway port number
     */
    public Voicent(String host, int port) {
        host_ = host;
        port_ = port;
    }

    /* test usage */
    public static void main(String args[])
            throws InterruptedException {
        String mynumber = "1112222"; // replace with your own

        Voicent voicent = new Voicent();
        String reqId = voicent.callText(mynumber, "hello, how are you", true);
        System.out.println("callText: " + reqId);

        reqId = voicent.callAudio(mynumber, "C:/Program    Files/Voicent/MyRecordings/sample_message.wav", false);
        System.out.println("callAudio: " + reqId);

        while (true) {
            Thread.sleep(30000);
            String status = voicent.callStatus(reqId);
            if (status.length() > 0) {
                System.out.println(status);
                voicent.callRemove(reqId);
                break;
            }
        }

        voicent.callTillConfirm("C:/Program    Files/Voicent/BroadcastByPhone/bin/vcast.exe",
                "C:/temp/testctf.voc",
                "C:/Program Files/Voicent/MyRecordings/sample_message.wav",
                "1234");
    }

    /**
     * Make a call to the number specified and play the text message
     * using text-to-speech engine.
     *
     * @param phoneno    Phone number to call, exactly as it should be dialed
     * @param text       Text to play over the phone using text-to-speech
     * @param selfdelete After the call, delete the call request automatically if set to 1
     * @return Call request ID
     */
    public String callText(String phoneno, String text, boolean selfdelete) {

        // call request url
        String urlstr = "/ocall/callreqHandler.jsp";

        // setting the http post string
        String poststr = "info=";
        try {
            poststr += URLEncoder.encode("Simple Text Call " + phoneno, "UTF-8");

            poststr += "&phoneno=";
            poststr += phoneno;

            poststr += "&firstocc=10";

            poststr += "&selfdelete=";
            poststr += (selfdelete ? "1" : "0");

            poststr += "&txt=";
            poststr += URLEncoder.encode(text, "UTF-8");

        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
        // Send Call Request
        String rcstr = postToGateway(urlstr, poststr);

        return getReqId(rcstr);
    }

    /**
     * Make a call to the number specified and play the audio file. The
     * audio file should be of PCM 8KHz, 16bit, mono.
     *
     * @param phoneno    Phone number to call, exactly as it should be dialed
     * @param audiofile  Audio file path name
     * @param selfdelete After the call, delete the call request automatically if set to 1
     * @return Call request ID
     */
    public String callAudio(String phoneno, String audiofile, boolean selfdelete) {
        try {
            // call request url
            String urlstr = "/ocall/callreqHandler.jsp";

            // setting the http post string
            String poststr = "info=";
            poststr += URLEncoder.encode("Simple Audio Call " + phoneno, "UTF-8");

            poststr += "&phoneno=";
            poststr += phoneno;

            poststr += "&firstocc=10";

            poststr += "&selfdelete=";
            poststr += (selfdelete ? "1" : "0");

            poststr += "&audiofile=";
            poststr += URLEncoder.encode(audiofile, "UTF-8");

            // Send Call Request
            String rcstr = postToGateway(urlstr, poststr);

            return getReqId(rcstr);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Get call status of the call with the reqID.
     *
     * @param reqID Call request ID on the gateway
     * @return call status
     */
    public String callStatus(String reqID) {
        try {
            // call status url
            String urlstr = "/ocall/callstatusHandler.jsp";

            // setting the http post string
            String poststr = "reqid=";
            poststr += URLEncoder.encode(reqID, "UTF-8");

            // Send Call Request
            String rcstr = postToGateway(urlstr, poststr);

            return getCallStatus(rcstr);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Remove all request from the gateway
     *
     * @param reqID Call request ID on the gateway
     */
    public void callRemove(String reqID) {
        try {
            // call status url
            String urlstr = "/ocall/callremoveHandler.jsp";

            // setting the http post string
            String poststr = "reqid=";
            poststr += URLEncoder.encode(reqID, "UTF-8");

            // Send Call remove post
            postToGateway(urlstr, poststr);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Invoke BroadcastByPhone and start the call-till-confirm process
     *
     * @param vcastexe Executable file vcast.exe, BroadcastByPhone path name
     * @param vocfile  BroadcastByPhone call list file
     * @param wavfile  Audio file used for the broadcast
     * @param ccode    Confirmation code
     */

    public void callTillConfirm(String vcastexe, String vocfile, String wavfile, String ccode) {
        try {
            // call request url
            String urlstr = "/ocall/callreqHandler.jsp";

            // setting the http post string
            String poststr = "info=";
            poststr += URLEncoder.encode("Simple Call till Confirm", "UTF-8");

            poststr += "&phoneno=1111111"; // any number

            poststr += "&firstocc=10";
            poststr += "&selfdelete=0";

            poststr += "&startexec=";
            poststr += URLEncoder.encode(vcastexe, "UTF-8");

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

            // add -cleanstatus if necessary

            poststr += "&cmdline=";
            poststr += URLEncoder.encode(cmdline, "UTF-8");

            // Send like a Call Request
            postToGateway(urlstr, poststr);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private String postToGateway(String urlstr, String poststr) {
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

            StringBuilder rcstr = new StringBuilder();
            byte[] b = new byte[4096];
            int len;
            while ((len = in.read(b)) != -1)
                rcstr.append(new String(b, 0, len));
            return rcstr.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private String getReqId(String rcstr) {
        int index1 = rcstr.indexOf("[ReqId=");
        if (index1 == -1)
            return "";
        index1 += 7;

        int index2 = rcstr.indexOf("]", index1);
        if (index2 == -1)
            return "";

        return rcstr.substring(index1, index2);
    }

    private String getCallStatus(String rcstr) {
        if (rcstr.contains("^made^"))
            return "Call Made";

        if (rcstr.contains("^failed^"))
            return "Call Failed";

        if (rcstr.contains("^retry^"))
            return "Call Will Retry";

        return "";
    }
}
