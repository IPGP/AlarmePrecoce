package fr.ipgp.earlywarning.gateway;

import fr.ipgp.charon.CharonI;
import fr.ipgp.charon.InvalidLedStateException;
import fr.ipgp.charon.InvalidResponseException;
import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.messages.WarningMessageMapper;
import fr.ipgp.earlywarning.triggers.Trigger;

import java.io.IOException;

/**
 * A Gateway using the Charon I module in case the Asterisk Server is out of service.
 *
 * @author Patrice Boissier
 * @author Thomas Kowalski
 */
public class CharonGateway implements Gateway {

    private final static int defaultLed = 5;
    private static CharonGateway uniqueInstance;
    private final CharonI charon;

    private CharonGateway(String moduleIP, int modulePort, int tcpTimeout) {
        charon = new CharonI(moduleIP, modulePort, tcpTimeout);
    }

    /**
     * Returns the {@link CharonGateway}'s unique instance.
     *
     * @param moduleIP   the Charon I module's IP
     * @param modulePort the Charon I module's TCP port
     * @param tcpTimeout the TCP timeout to use
     * @return the singleton's unique instance
     */
    public static synchronized CharonGateway getInstance(String moduleIP, int modulePort, int tcpTimeout) {
        if (uniqueInstance == null) {
            uniqueInstance = new CharonGateway(moduleIP, modulePort, tcpTimeout);
        }
        return uniqueInstance;
    }

    /**
     * Calls the duty phone until the operator acknowledges the message.
     *
     * @param trigger that triggered the call
     * @return always {@link CallLoopResult}<code>.Confirmed</code> (it cannot fail)
     */
    public CallLoopResult callTillConfirm(Trigger trigger) {
        EarlyWarning.appLogger.info("CharonGateway: calling duty phone.");

        String ledString = WarningMessageMapper.getInstance(this).getNameOrDefaultIgnoreCase(trigger.getMessage());

        EarlyWarning.appLogger.info("CharonGateway: Using LED " + ledString + " for message '" + trigger.getMessage() + "'");

        int led;
        try {
            led = Integer.parseInt(ledString);
        } catch (NumberFormatException ex) {
            led = defaultLed;
            EarlyWarning.appLogger.error("'" + ledString + "' cannot be converted to an Integer. Defaulting to LED " + defaultLed);
        }

        if (led == -1) {
            EarlyWarning.appLogger.error("Sound '-1' was mapped in CharonGateway. '-1' mappings should be used for sounds that are not played by CharonGateway. Exiting.");
            return CallLoopResult.Error;
        }

        return callTillConfirm(led);
    }

    /**
     * Calls the duty phone until the operator acknowledges the message.
     */
    private CallLoopResult callTillConfirm(int triggeringLed) {
        if (call(triggeringLed))
            return CallLoopResult.Confirmed;
        else
            return CallLoopResult.Error;
    }

    /**
     * Calls the duty phone until the operator confirms his / her code.
     *
     * @param number as described by the interface, the phone number to call (<b>though it will not be called by this gateway.</b>
     */
    @Override
    public void callTest(String number) {
        EarlyWarning.appLogger.warn("CharonGateway can't call a specified number. Calling duty phone.");
        callTillConfirm(defaultLed);
    }

    @Override
    public String getSettingsQualifier() {
        return "charon";
    }

    /**
     * Verify that the Charon I module is reachable by refreshing the LED states.
     *
     * @return <code>true</code> if the module is reachable, <code>false</code> otherwise
     */
    public boolean checkConnected() {
        try {
            charon.refreshLedState();
        } catch (IOException | InvalidResponseException e) {
            return false;
        }

        return true;
    }

    /**
     * Use the {@link CharonI} class in order to emit a call.
     *
     * @param triggeringLed the LED triggering the call (which will determine what is said)
     * @return whether or not the call was emitted
     */
    private boolean call(int triggeringLed) {
        try {
            // Get latest LED state (for debugging purposes)
            charon.refreshLedState();
            System.out.println(charon.toString());

            // Power the LED off
            charon.setLedAt(triggeringLed, 0);
            charon.applyLedState();

            charon.refreshLedState();
            System.out.println(charon.toString());

            // Power the LED on (forces the call to go through)
            charon.setLedAt(triggeringLed, 1);
            charon.applyLedState();

            return true;
        } catch (InvalidLedStateException ignored) {
            // This can't happen: we have not given a custom LED state String
            return false;
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            return false;
        } catch (InvalidResponseException e) {
            EarlyWarning.appLogger.error("CharonGateway: Received invalid response from Charon Gateway: " + e.getResponse());
            EarlyWarning.appLogger.error(e.getMessage());
            return false;
        }
    }

    @Override
    public String toString()
    {
        return "Charon Gateway (" + charon.getIp() + ":" + charon.getTcpPort() + ")";
    }
}
