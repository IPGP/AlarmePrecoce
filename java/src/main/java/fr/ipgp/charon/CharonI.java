package fr.ipgp.charon;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static fr.ipgp.charon.CharonI.CharonCode.*;

/**
 * <h2>Presentation</h2>
 * <p>
 * This class can be used to interface the Charon I module.<br />
 * Technical documentation here: <a href="https://elmicro.com/files/hwgroup/charon1_en.pdf">latest link</a>.<br />
 * It uses the NVT protocol. <br />
 * NVT commands begin with <code>OxFF 0xFA</code> and end with <code>0xFF 0xF0</code> (which is {@code <IAC><SB>} and {@code <IAC><SE>} respectively.<br />
 * RFC2217 defines many commands but the only ones we'll be using here are the GPIO ones and the <code>COM_PORT_OPTION</code> one :
 * <ul>
 * <li><code>0x2C</code> (<code>COM_PORT_OPTION</code>)</li>
 * <li><code>0x32</code> (<code>CAS_OPT_GPIO</code>) : used to get the current GPIO state</li>
 * <li><code>0x33</code> (<code>CAS_SET_GPIO</code>) : used to set the GPIO state (<code>HIGH</code> or <code>LOW</code>)</li>
 * </ul>
 * </p>
 * <h2>Examples</h2>
 * <h3>Command to send to get the current LED state</h3>
 * {@code <IAC><SB><COM_PORT_OPTION><CAS_OPT_GPIO>0x00<IAC><SE>} which is (<code>0xFF 0xFA 0x2C 0x32 0x00 0xFF 0xF0</code>)<br /><br />
 * <b>The response will be</b><br />
 * {@code <IAC><SB><COM_PORT_OPTION><ASC_OPT_GPIO><LED State><IAC><SE>} which is (<code>0xFF 0xFA 0x2C 0x96 <i><b>0xXX</b></i> 0xFF 0xF0</code>)<br /><br />
 * <h3>Command to send to set the current LED state</h3>
 * {@code <IAC><SB><COM_PORT_OPTION><CAS_SET_GPIO><New LED State><IAC><SE>} which is (<code>0xFF 0xFA 0x2C 0x33 0xXX 0xFF 0xF0</code>)
 *
 * @author Jean-Pierre Coudray
 * @author Patrice Boissier
 * @author Thomas Kowalski
 */
public class CharonI {
    private int id;
    private String ip;
    private int tcpPort;
    private int tcpTimeout;
    private final int[] ledsArray = {0, 0, 0, 0, 0, 0, 0, 0};
    private final int ledNumber = ledsArray.length;

    /**
     * Codes used to communicate with the Charon I module
     */
    public enum CharonCode {
        IAC(0xFF),
        BEGIN(0xFA),
        END(0xF0),
        COM_PORT_OPTION(0x2C),
        GET_GPIO(0x32),
        SET_GPIO(0x33);

        private final int ord;

        CharonCode(int ord) {
            this.ord = ord;
        }

        int getOrd() {
            return this.ord;
        }
    }

    /**
     * Returns a String corresponding to the hexadecimal value of the given code
     *
     * @param code the code to be used
     * @return a "hexadecimal String"
     */
    private String getHex(CharonCode code) {
        return Integer.toHexString(code.getOrd());
    }

    /**
     * Returns the hexadecimal representation of an integer
     *
     * @param i the integer to convert
     * @return its hexadecimal representation in the form of a String
     */
    private String getHex(int i) {
        String s = Integer.toHexString(i);
        if (s.length() % 2 == 1)
            s = "0" + s;
        return s;
    }

    /**
     * Constructs the String to send to apply a command with a paramter
     *
     * @param command the command to send
     * @param LED     the parameter for the command
     * @return the datagram to send
     */
    private String getCommand(CharonCode command, int LED) {
        try {
            return getCommand(command, getHex(LED));
        } catch (InvalidLedStateException ignored) {
            // This can't happen, since getHex always returns even-length strings.
            return null;
        }
    }

    /**
     * Constructs the String to send to apply a command with a parameter
     *
     * @param command the command to send
     * @param LED     the parameter for the command
     * @return the datagram to send
     * @throws InvalidLedStateException if <code>LED</code> is a good LED-state representation
     */
    private String getCommand(CharonCode command, String LED) throws InvalidLedStateException {
        if (LED.length() == 1)
            LED = "0" + LED;
        else if (LED.length() != 2)
            throw new InvalidLedStateException("LEDState '" + LED + "' is invalid.");

        @SuppressWarnings("UnnecessaryLocalVariable")
        String s = getHex(IAC) +
                getHex(BEGIN) +
                getHex(COM_PORT_OPTION) +
                getHex(command) +
                LED +
                getHex(IAC) +
                getHex(END);

        return s;
    }

    /**
     * Returns the datagram to send to get the current LED state
     *
     * @param LED the LED to get
     * @return the datagram to send
     */
    private String getGetLEDCommand(int LED) {
        return getCommand(GET_GPIO, LED);
    }

    private String getSetLEDCommand(String LED) throws InvalidLedStateException {
        return getCommand(SET_GPIO, LED);
    }

    /**
     * Constructor
     *
     * @param ip         Charon module's IP
     * @param tcpPort    Charon module's TCP port
     * @param tcpTimeout The TCP timeout to use
     */
    public CharonI(String ip, int tcpPort, int tcpTimeout) {
        this(0, ip, tcpPort, tcpTimeout);
    }

    /**
     * Constructor
     *
     * @param id         Charon module's ID
     * @param ip         Charon module's IP
     * @param tcpPort    Charon module's TCP port
     * @param tcpTimeout The TCP timeout to use
     */
    public CharonI(int id, String ip, int tcpPort, int tcpTimeout) {
        this.id = id;
        this.ip = ip;
        this.tcpPort = tcpPort;
        this.tcpTimeout = tcpTimeout;
    }

    /**
     * @return the ID of the module
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the new ID
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the IP of the module
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ip the new IP
     */
    public void setIp(String ip) {
        this.ip = ip;
    }

    /**
     * @return the TCP port used
     */
    public int getTcpPort() {
        return tcpPort;
    }

    /**
     * @param tcpPort the new TCP Port
     */
    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    /**
     * @return the TCP timeout used
     */
    public int getTcpTimeout() {
        return tcpTimeout;
    }

    /**
     * @param tcpTimeout the new TCP timeout
     */
    public void setTcpTimeout(int tcpTimeout) {
        this.tcpTimeout = tcpTimeout;
    }

    /**
     * @return the number of LEDs on the module
     */
    public int getLedNumber() {
        return ledNumber;
    }

    /**
     * Returns the value of a LED at a given index
     *
     * @param index the index of the LED to get
     * @return the value of the LED
     */
    public int getLEDAt(int index) throws IOException, InvalidResponseException {
        if (index < 0 || index > (ledNumber - 1))
            return -1;

        refreshLedState();

        return ledsArray[index];
    }

    /**
     * Sets the LED value at a given index
     *
     * @param index the index of the LED to set
     * @param value the value to set (0 or 1)
     */
    public void setLedAt(int index, int value) throws InvalidLedStateException, IOException, InvalidResponseException {
        if (index >= 0 && index <= (ledNumber - 1) &&
                (value == 0 || value == 1)) {
            ledsArray[index] = value;
            applyLedState();
        } else
            throw new InvalidLedStateException("Cannot set LED state for led: " + index + " and state " + value + ". (ledNumber = " + getLedNumber());
    }

    /**
     * @return a String representation of the CharonI object
     */
    public String toString() {
        return "CharonI " + ip + ":" + tcpPort + " : " + ledStatesToString();
    }

//    /**
//     * Updates the LED state using the provided String. <br />
//     * <b>Example :</b> <code>setLedState("01001100");</code><br />
//     * The provided String should always be of length the number of LEDs on the module.
//     *
//     * @param newLedState the new LED state byte in the form of a String
//     */
//    private void setLedState(String newLedState) throws InvalidLedStateException, IOException, InvalidResponseException {
//        // Complete the String in case it's not already the right size
//        StringBuilder newLedStateBuilder = new StringBuilder(newLedState);
//        while (newLedStateBuilder.length() < ledNumber)
//            newLedStateBuilder.insert(0, "0");
//
//        newLedState = newLedStateBuilder.toString();
//
//        // Validate the given String using a regular expression (ledNumber times 0 or 1)
//        @SuppressWarnings("Annotator")
//        Pattern pattern = Pattern.compile("^[0-1]{" + ledNumber + "}$");
//        Matcher matcher = pattern.matcher(newLedState);
//        if (matcher.find()) {
//            // Affect the LED values in the LED states array
//            for (int i = 0; i < ledNumber; i++)
//                // TODO: verify this
//                ledsArray[i] = Integer.parseInt(Character.toString(newLedState.charAt(i)));
//            // ledsArray[ledNumber - 1 - i] = Integer.parseInt(Character.toString(newLedState.charAt(i)));
//
//            // Apply the new LED state on the module
//            applyLedState();
//        } else
//            throw new InvalidLedStateException("Invalid LED value String: '" + newLedState + "'");
//    }

    /**
     * Returns the current LED state in the form of a "byte String"
     *
     * @return a byte in the form of a String representing the LED states
     */
    public String ledStatesToString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < ledNumber; i++)
            s.append(ledsArray[i]);

        return s.toString();
    }

    /**
     * Refreshes the current LED states (in the <code>ledsArray</code>)
     */
    public void refreshLedState() throws IOException, InvalidResponseException {
        StringBuilder ledValues;

        SocketAddress sockaddr = new InetSocketAddress(ip, tcpPort);
        Socket socket_module = new Socket();
        socket_module.connect(sockaddr);

        InputStream is = socket_module.getInputStream();
        OutputStream os = socket_module.getOutputStream();

        // TODO: why 0x00 ?
        String command = getGetLEDCommand(0x00);
        System.out.println("Command to send: " + command);
        os.write(DatatypeConverter.parseHexBinary(command));

        byte[] data = new byte[100];
        int count = is.read(data);

        is.close();
        os.close();
        socket_module.close();

        // The response should be 7 character (bytes) long
        if (count == 7) {
            // Convert the raw result to a String
            String result = DatatypeConverter.printHexBinary(Arrays.copyOfRange(data, 0, 7));

            // Verify the result String matches the expected pattern (IAC SB ... VALUES ... IAC SE)
            Pattern pattern = Pattern.compile("^FFFA2C96([A-F0-9]{2})FFF0");
            Matcher matcher = pattern.matcher(result);

            if (matcher.find()) {
                // If the result matches the pattern, transform the interesting part (the first group) to an Integer
                int intValue = Integer.parseInt(matcher.group(1), 16);

                // Convert this result to a binary String
                ledValues = new StringBuilder(Integer.toBinaryString(intValue));

                // If the first LEDs are not on (for example, state is 00001000) then the result might be 1000 instead of 00001000 (which makes more sense)
                // So we 0-pad our String while it is not the right length
                while (ledValues.length() < ledNumber)
                    ledValues.insert(0, "0");

                // And then we fill our array
                for (int i = 0; i < ledValues.length(); i++)
                    // TODO: verify this
                    ledsArray[i] = ledValues.charAt(i) == '1' ? 1 : 0;
                // ledsArray[ledNumber - 1 - i] = ledValues.charAt(i) == '1' ? 1 : 0;

            } else
                // If no match for the result has been found (which probably can't happen)
                throw new InvalidResponseException("The response didn't contain the LED state.", result);
        } else
            // If the result doesn't match our expected format
            throw new InvalidResponseException("The response length wasn't the expected one (" + count + ", should have been 7).", new String(data));

    }

    /**
     * Applies the current LED state<br />
     * Establishes a TCP connection to the Charon module and applies the local LED state via the socket.
     *
     * @throws IOException              if the connection can't be established
     * @throws InvalidResponseException if the response from the module in invalid
     */
    public void applyLedState() throws IOException, InvalidResponseException {
        String ledValues = ledStatesToString();

        // Print it
        System.out.println("Affecting LED values: '" + ledValues + "'");

        // Convert the String (eg 00010010) to its integer counterpart
        int decimalValue = Integer.parseInt(ledValues, 2);

        // Convert the integer value to its hexadecimal counterpart
        String hexadecimalState = Integer.toString(decimalValue, 16);

        // Build the command to send
        String command = null;
        try {
            command = getSetLEDCommand(hexadecimalState);
        } catch (InvalidLedStateException ignored) {
            // This can't happen: the String was built by the programme.
        }

        assert command != null;

        // Create a socket, connect
        SocketAddress sockaddr = new InetSocketAddress(ip, tcpPort);
        Socket socket_module = new Socket();
        socket_module.connect(sockaddr);

        // Get the output stream and send the command
        OutputStream os = socket_module.getOutputStream();
        os.write(DatatypeConverter.parseHexBinary(command));
        os.close();

        System.out.println("Sent: " + command);

        socket_module.close();
        refreshLedState();
    }
}
