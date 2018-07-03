package fr.ipgp.clickatell;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * This is an example of how to use the Clickatell HTTP API. NOTE: this is not
 * the only way, this is just an example. This class can also be used as a
 * library if you wish.
 *
 * @author Dominic Schaff <dominic.schaff@gmail.com>
 * Dec 2, 2014
 */
public class ClickatellHttp {

    /**
     * The URL to use for the base of the HTTP API.
     */
    private static final String CLICKATELL_HTTP_BASE_URL = "https://api.clickatell.com/http/";

    /**
     * The URL to use for the base of the HTTP/UTILS API.
     */
    private static final String CLICKATELL_UTILS_BASE_URL = "https://api.clickatell.com/utils/";

    /**
     * The three private variables to use for authentication.
     */
    private final String userName;
    private final String apiId;
    private final String password;

    /**
     * Create a HTTP object, and set the auth, but not test the auth.
     */
    public ClickatellHttp(String userName, String apiId, String password) {
        this.userName = userName;
        this.apiId = apiId;
        this.password = password;
    }

    /**
     * This tests whether your account details works.
     *
     * @return True if details were accepted, and false otherwise.
     * @throws UnknownHostException if the host can't be reached
     */
    public boolean testAuth() throws UnknownHostException {
        try {
            // Build Parameters:
            String urlParameters = "user="
                    + URLEncoder.encode(this.userName, "UTF-8") + "&api_id="
                    + URLEncoder.encode(this.apiId, "UTF-8") + "&password="
                    + URLEncoder.encode(this.password, "UTF-8");

            // Send Request:
            String result = this.excutePost(CLICKATELL_HTTP_BASE_URL
                    + "auth.php", urlParameters);
            // Check whether an auth failed happened:
            return result.startsWith("OK: ");
        } catch (UnsupportedEncodingException ignored) {
        }
        return false;
    }

    /**
     * This will attempt to get your current balance.
     *
     * @return Your balance.
     * @throws Exception This will be thrown if your auth details were incorrect.
     */
    public double getBalance() throws Exception {
        // Build Parameters:
        String urlParameters = "user="
                + URLEncoder.encode(this.userName, "UTF-8") + "&api_id="
                + URLEncoder.encode(this.apiId, "UTF-8") + "&password="
                + URLEncoder.encode(this.password, "UTF-8");

        // Send Request:
        String result = this.excutePost(CLICKATELL_HTTP_BASE_URL
                + "getbalance.php", urlParameters);
        // Check whether an auth failed happened:
        if (result.contains("Authentication failed")) {
            throw new Exception("Authentication Failed");
        }
        if (result.startsWith("ERR")) {
            throw new Exception(result);
        }
        // We know the balance is the second part of the query:
        String[] a = result.split(" ");
        return Double.parseDouble(a[1]);
    }

    /**
     * This sends a single message.
     *
     * @param number         The number that you wish to send to. This should be in
     *                       international format.
     * @param messageContent The message you want to send,
     * @return A Message object that contains the resulting information.
     * @throws Exception This gets thrown on an auth failure.
     */
    public Message sendMessage(String number, String messageContent)
            throws Exception {
        // Build Parameters:
        String urlParameters = "user="
                + URLEncoder.encode(this.userName, "UTF-8") + "&api_id="
                + URLEncoder.encode(this.apiId, "UTF-8") + "&password="
                + URLEncoder.encode(this.password, "UTF-8") + "&to="
                + URLEncoder.encode(number, "UTF-8") + "&text="
                + URLEncoder.encode(messageContent, "UTF-8");

        // Send Request:
        String result = this.excutePost(CLICKATELL_HTTP_BASE_URL
                + "sendmsg.php", urlParameters);
        // Check whether an auth failed happened:
        if (result.contains("Authentication failed")) {
            throw new Exception("Authentication Failed");
        }
        String a[] = result.split(": ");
        Message message = new Message();
        message.number = number;
        message.content = messageContent;
        // Check whether there is no credit left in the account:
        if (result.toLowerCase().startsWith("err")) {
            message.error = a[1];
            return message;
        }
        message.message_id = a[1].trim();
        return message;
    }

    /**
     * This is to send the same message to multiple people. Only use this
     * function to send a maximum of 300 messages, and a minimum of 2.
     *
     * @param numbers        The array of numbers that are to be sent to.
     * @param messageContent The message that you would like to send.
     * @return The returned array contains the messages sent with their
     * resulting information.
     * @throws Exception This gets thrown on auth errors.
     */
    public Message[] sendMessage(String[] numbers, String messageContent)
            throws Exception {
        if (numbers.length < 2 || numbers.length > 300) {
            throw new Exception("Illegal arguments passed");
        }
        ArrayList<Message> messages = new ArrayList<>();
        // Build Parameters:
        StringBuilder urlParameters = new StringBuilder("user="
                + URLEncoder.encode(this.userName, "UTF-8") + "&api_id="
                + URLEncoder.encode(this.apiId, "UTF-8") + "&password="
                + URLEncoder.encode(this.password, "UTF-8") + "&text="
                + URLEncoder.encode(messageContent, "UTF-8") + "&to=");
        urlParameters.append(numbers[0]);
        for (int x = 1; x < numbers.length; x++) {
            urlParameters.append(",").append(numbers[x]);
        }

        // Send Request:
        String result = this.excutePost(CLICKATELL_HTTP_BASE_URL
                + "sendmsg.php", urlParameters.toString());
        // Check whether an auth failed happened:
        if (result.contains("Authentication failed")) {
            throw new Exception("Authentication Failed");
        }
        // We don't throw an exception here, as maybe only part of your
        // messages failed:
        String lines[] = result.split("\n");
        for (String l: lines) {
            String n[] = l.split(" To: ");
            Message message = new Message();
            message.number = n[1].trim();
            String q[] = n[0].split(": ");
            if (q[0].equalsIgnoreCase("err")) {
                message.error = q[1];
            } else {
                message.message_id = q[1];
            }
            messages.add(message);
        }
        return messages.toArray(new Message[0]);
    }

    /**
     * This will attempt to get the message status of a single message.
     *
     * @param messageId This is the message ID that you received when sending the
     *                  message.
     * @return The status of the message.
     * @throws Exception if there is an error with the request.
     */
    public int getMessageStatus(String messageId) throws Exception {
        String urlParameters;
        // Build Parameters:
        urlParameters = "user=" + URLEncoder.encode(this.userName, "UTF-8")
                + "&api_id=" + URLEncoder.encode(this.apiId, "UTF-8")
                + "&password=" + URLEncoder.encode(this.password, "UTF-8")
                + "&apimsgid=" + URLEncoder.encode(messageId, "UTF-8");

        // Send Request:
        String result = this.excutePost(CLICKATELL_HTTP_BASE_URL
                + "querymsg.php", urlParameters);
        // Check whether an auth failed happened:
        if (result.contains("Authentication failed")) {
            throw new Exception("Authentication Failed");
        }
        // If there was an error, throw it.
        if (result.startsWith("ERR:")) {
            throw new Exception(result);
        }
        // We know the status will always be the fourth part:
        // Syntax: ID: xxx Status: xxx
        String[] a = result.split(" ");
        return Integer.parseInt(a[3].trim());
    }

    /**
     * This will get the status and charge of the message given by the
     * messageId.
     *
     * @param messageId The message ID that should be searched for.
     * @return A Message object with the requested data will be returned.
     * @throws Exception If there was an error with the request.
     */
    public Message getMessageCharge(String messageId) throws Exception {
        String urlParameters;
        // Build Parameters:
        urlParameters = "user=" + URLEncoder.encode(this.userName, "UTF-8")
                + "&api_id=" + URLEncoder.encode(this.apiId, "UTF-8")
                + "&password=" + URLEncoder.encode(this.password, "UTF-8")
                + "&apimsgid=" + URLEncoder.encode(messageId, "UTF-8");

        // Send Request:
        String result = this.excutePost(CLICKATELL_HTTP_BASE_URL
                + "getmsgcharge.php", urlParameters);
        // Check whether an auth failed happened:
        if (result.contains("Authentication failed")) {
            throw new Exception("Authentication Failed");
        }
        Message message = new Message(messageId);
        // If there was an error, throw it.
        if (result.startsWith("ERR:")) {
            message.error = result.substring(4);
        } else {
            String[] a = result.split(" ");
            message.status = a[5].trim();
            message.charge = a[3].trim();
        }
        return message;
    }

    /**
     * This will try to stop a message that has been sent. Note that only
     * messages that are going to be sent in the future can be stopped. Or if by
     * some luck you message has not been sent to the operator yet.
     *
     * @param messageId The message ID that is to be stopped.
     * @return The status after requesting the message to be stopped.
     * @throws Exception If there was something wrong with the request.
     */
    public int stopMessage(String messageId) throws Exception {
        String urlParameters;
        // Build Parameters:
        urlParameters = "user=" + URLEncoder.encode(this.userName, "UTF-8")
                + "&api_id=" + URLEncoder.encode(this.apiId, "UTF-8")
                + "&password=" + URLEncoder.encode(this.password, "UTF-8")
                + "&apimsgid=" + URLEncoder.encode(messageId, "UTF-8");

        // Send Request:
        String result = this.excutePost(
                CLICKATELL_HTTP_BASE_URL + "delmsg.php", urlParameters);
        // Check whether an auth failed happened:
        if (result.contains("Authentication failed")) {
            throw new Exception("Authentication Failed");
        }
        // If there was an error, throw it.
        if (result.startsWith("ERR")) {
            throw new Exception(result);
        }
        // Split the result we know that the status will always the fourth
        // part:
        // Format: ID: xxx Status: xxx
        String[] a = result.split(" ");
        return Integer.parseInt(a[3].trim());
    }

    /**
     * This attempts to get coverage data for the given number. A -1 means no
     * coverage, all else is the minimum cost the message could charge.
     *
     * @param number The number the lookup should be done on.
     * @return The minimum possible cost, or a -1 on error.
     * @throws Exception If there was something wrong with the request.
     */
    public double getCoverage(String number) throws Exception {
        // Build Parameters:
        String urlParameters = "user="
                + URLEncoder.encode(this.userName, "UTF-8") + "&api_id="
                + URLEncoder.encode(this.apiId, "UTF-8") + "&password="
                + URLEncoder.encode(this.password, "UTF-8") + "&msisdn="
                + URLEncoder.encode(number, "UTF-8");

        // Send Request:
        String result = this.excutePost(CLICKATELL_UTILS_BASE_URL
                + "routecoverage.php", urlParameters);
        // Check whether an auth failed happened:
        if (result.contains("Authentication failed")) {
            throw new Exception("Authentication Failed");
        }
        if (result.startsWith("ERR")) {
            return -1;
        }
        String[] a = result.split("Charge: ");
        return Double.parseDouble(a[1]);
    }

    /**
     * This will allow you to use any feature of the API. Note that you can do
     * more powerful things with this function. And as such should only be used
     * once you have read the documentation, as the parameters are passed
     * directly to the API.
     *
     * @param numbers        The list of numbers that must be sent to.
     * @param messageContent The message that is to be sent.
     * @param features       The extra features that should be included.
     * @return An array of Messages which will contain the data for each message
     * sent.
     * @throws Exception If there is anything wrong with the submission this will get
     *                   thrown.
     */
    public Message[] sendAdvancedMessage(String[] numbers, String messageContent, HashMap<String, String> features) throws Exception {
        @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
        ArrayList<Message> messages = new ArrayList<>();

        StringBuilder urlParameters;
        // Build Parameters:
        urlParameters = new StringBuilder("user=" + URLEncoder.encode(this.userName, "UTF-8")
                + "&api_id=" + URLEncoder.encode(this.apiId, "UTF-8")
                + "&password=" + URLEncoder.encode(this.password, "UTF-8")
                + "&text=" + URLEncoder.encode(messageContent, "UTF-8"));
        StringBuilder number = new StringBuilder(numbers[0]);
        for (int x = 1; x < numbers.length; x++) {
            number.append(",").append(numbers[x]);
        }
        urlParameters.append("&to=").append(number);
        for (Entry<String, String> entry: features.entrySet()) {
            urlParameters.append("&").append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        // Send Request:
        String result = this.excutePost(CLICKATELL_HTTP_BASE_URL
                + "sendmsg.php", urlParameters.toString());
        // Check whether an auth failed happened:
        if (result.contains("Authentication failed")) {
            throw new Exception("Authentication Failed");
        }

        // This does some fancy swapping:
        String lines[] = result.split("\n");
        if (lines.length > 1) { // Sent more than one message
            for (String l: lines) {
                Message message = new Message();
                String i[] = l.split(" To: ");
                message.number = i[1];
                String n[] = i[0].split(": ");
                if (n[0].equalsIgnoreCase("err")) {
                    message.error = n[1];
                } else {
                    message.message_id = n[1];
                }
            }
        } else { // Sent one message
            String n[] = lines[0].split(": ");
            Message message = new Message();
            message.number = numbers[0];
            if (n[0].equalsIgnoreCase("err")) {
                message.error = n[1];
            } else {
                message.message_id = n[1];
            }
        }
        return messages.toArray(new Message[0]);
    }

    /**
     * This executes a POST query with the given parameters.
     *
     * @param targetURL     The URL that should get hit.
     * @param urlParameters The data you want to send via the POST.
     * @return The content of the request.
     * @throws UnknownHostException if the host can't be reached
     */
    private String excutePost(String targetURL, String urlParameters) throws UnknownHostException {
        URL url;
        HttpURLConnection connection = null;
        try {
            // Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length",
                    "" + Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            // Send request
            DataOutputStream wr = new DataOutputStream(
                    connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            // Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\n');
            }
            rd.close();
            return response.toString();
        } catch (UnknownHostException ex) {
            throw ex;
        } catch (Exception ex) {
            return "";
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * This is the Message class that gets used as return values for some of the
     * functions.
     *
     * @author Dominic Schaff <dominic.schaff@gmail.com>
     */
    public class Message {
        public String number = null, message_id = null, content = null,
                charge = null, status = null, error = null;

        public Message(String message_id) {
            this.message_id = message_id;
        }

        public Message() {
        }

        public String toString() {
            if (message_id != null) {
                return number + ": " + message_id;
            }
            return number + ": " + error;
        }
    }
}
