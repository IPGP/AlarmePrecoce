package fr.ipgp.earlywarning.asterisk;

import fr.ipgp.earlywarning.EarlyWarning;
import org.asteriskjava.fastagi.*;

/*
 * Important note:
 * Asterisk custom sounds should be placed in
 * /usr/share/asterisk/sounds/<soundName>.gsm
 */

/**
 * The (to this day) only AGI script we have.
 * <b>What does it do?</b>
 * <ul>
 * <li>Immediatly answer the code (for now, we can't detect when the callee actually answers)</li>
 * <li>Plays a "welcome" sound in a loop until the callee enters "11" on his dial pad</li>
 * <li>// TODO plays an adequate sound, depending on the situation</li>
 * <li>Asks for a confirmation code</li>
 * <li>While the confirmation code is incorrect and the CallOriginator tells the script the user can carry on trying, it keeps asking for the code</li>
 * <li>Finally hangs up.</li>
 * </ul>
 *
 * @author Thomas Kowalski
 */
public class AlertCallScript extends BaseAgiScript {
    private static OnCodeReceivedListener onCodeReceivedListener;
    private static OnHangupListener onHangupListener;
    private static OnConnectedListener onConnectedListener;
    private static boolean hangupRequested = false;
    private static int maxCodeLength = 0;

    /**
     * Setter for the {@link OnCodeReceivedListener}
     *
     * @param listener      the listener to use
     * @param maxCodeLength the maximum length of the code the callee should be able to type
     */
    public static void setOnCodeReceivedListener(OnCodeReceivedListener listener, int maxCodeLength) {
        AlertCallScript.maxCodeLength = maxCodeLength;
        onCodeReceivedListener = listener;
    }

    /**
     * Setter for the {@link OnHangupListener}
     *
     * @param listener the listener to use
     */
    public static void setOnHangupListener(OnHangupListener listener) {
        onHangupListener = listener;
    }

    /**
     * Setter for the {@link OnConnectedListener}<br />
     * <b>It should be noted that the <code>onConnected</code> event is not actually triggered when the callee is connected, but rather when he acknowledges the welcome message.</b>
     *
     * @param listener the listener to use
     */
    public static void setOnConnectedListener(OnConnectedListener listener) {
        onConnectedListener = listener;
    }

    /**
     * Used to ask the script to hang up as soon as possible.<br />
     * <b>Note: </b> since the script is totally synchronous, it will only hang up when its current task (playing a sound, etc.) is finished.
     */
    public static void requestHangup() {
        hangupRequested = true;
    }

    /**
     * Setter for the {@link OnCodeReceivedListener}
     *
     * @param listener the listener to use
     */
    public static void setOnCodeReceivedListener(OnCodeReceivedListener listener) {
        setOnCodeReceivedListener(listener, 0);
    }

    /**
     * The main script
     *
     * @param request the {@link AgiRequest}
     * @param channel the {@link AgiChannel}
     * @throws AgiException if something unexpected - but not a {@link AgiHangupException} - happens.
     */
    public void service(AgiRequest request, AgiChannel channel) throws AgiException {
        // Rest the hangup request state
        hangupRequested = false;

        EarlyWarning.appLogger.debug("Handling new call.");

        try {
            // Answer the call
            EarlyWarning.appLogger.debug("Answering.");
            answer();

            // Stream the welcome file
            EarlyWarning.appLogger.debug("Waiting for input");
            while (true) {
                String data = getData("accueilovpf", 0, 2);

                // Check if hangup as been requested
                if (hangupRequested)
                    hangup();

                if (onConnectedListener != null)
                    // Notify the OnConnectedListener, if it's available
                    onConnectedListener.onConnected();

                if (data.startsWith("11"))
                    // If the user has entered 11 (Acknowledge welcome message),
                    // stop playing the welcome message
                    break;
            }

            EarlyWarning.appLogger.debug("Waiting for code confirmation");

            // action will be our loop variable for the retry-give up mechanism.
            CallOriginator.CallAction action = CallOriginator.CallAction.Retry;

            // Asks for a confirmation code (that ends with #)
            String code = getData("agent-user", 3500, maxCodeLength);

            // The action is determined by either its initial value or the return value of OnCodeReceivedListener.onCodeReceived
            while (action == CallOriginator.CallAction.Retry) {
                EarlyWarning.appLogger.debug("Callee entered code: " + code);

                // Wait to hang up before sending the code
                action = onCodeReceivedListener.onCodeReceived(code);

                // Print the string equivalent of callAction
                EarlyWarning.appLogger.debug("Action to take: " + CallOriginator.CallAction.values()[action.ordinal()]);

                switch (action) {
                    case Correct:
                        streamFile("agent-loginok");
                        break;
                    case Retry:
                        code = getData("agent-incorrect", 3500, maxCodeLength);
                        break;
                    case GiveUp:
                        streamFile("bye");
                        break;
                    default:
                        System.err.println("Unknown CallAction value returned: " + action);
                }
            }

            // Hang up
            EarlyWarning.appLogger.debug("Hanging up.");
            hangup();

        } catch (AgiHangupException ex) {
            // If the call is hung up (by the user or the script), notify the listeners.
            onHangupListener.onHangup();
            EarlyWarning.appLogger.debug("Call was hung up. Terminating.");
        }
    }
}
