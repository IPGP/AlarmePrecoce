package fr.ipgp .earlywarning.asterisk;

import org.asteriskjava.fastagi.*;

import java.util.logging.Logger;

/*
 * LES FICHIERS SONS SONT A PLACER DANS /usr/share/asterisk/sounds/<nom>.gsm
 */

public class AlertCallScript extends BaseAgiScript {
    private static OnCodeReceivedListener onCodeReceivedListener;
    private static OnHangupListener onHangupListener;
    private static OnConnectedListener onConnectedListener;
    private static boolean hangupRequested = false;
    private static int maxCodeLength = 0;
    private final Logger logger = Logger.getLogger("AlertCall");

    public static void setOnCodeReceivedListener(OnCodeReceivedListener listener, int maxCodeLength) {
        AlertCallScript.maxCodeLength = maxCodeLength;
        onCodeReceivedListener = listener;
    }

    public static void setOnHangupListener(OnHangupListener listener) {
        onHangupListener = listener;
    }

    public static void setOnConnectedListener(OnConnectedListener listener) {
        onConnectedListener = listener;
    }

    public static void requestHangup() {
        hangupRequested = true;
    }

    public static void setOnCodeReceivedListener(OnCodeReceivedListener listener) {
        setOnCodeReceivedListener(listener, 0);
    }

    public void service(AgiRequest request, AgiChannel channel) throws AgiException {
        logger.info("-------------------------------------");

        hangupRequested = false;

        logger.info("Handling new call.");

        try {
            // Answer the phone
            logger.info("Answering.");
            answer();

            // Stream an alert file
            logger.info("Waiting for input");
            while (true) {
                String data = getData("accueilovpf", 0, 2);

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

            logger.info("Code confirmation");

            CallOriginator.CallAction action = CallOriginator.CallAction.Retry;
            // Asks for a confirmation code (that ends with #)
            String code = getData("agent-user", 3500, maxCodeLength);

            // The action is determined by either its initial value or the return value of OnCodeReceivedListener.onCodeReceived
            while (action == CallOriginator.CallAction.Retry) {
                logger.info("Callee entered code: " + code);

                // Wait to hang up before sending the code
                action = onCodeReceivedListener.onCodeReceived(code);

                // Print the string equivalent of callAction
                System.out.println(CallOriginator.CallAction.values()[action.ordinal()]);

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
            logger.info("Hanging up.");
            hangup();

        } catch (AgiHangupException ex) {
            // If the user hangs up, notify the listeners and end the script.
            onHangupListener.onHangup();
            logger.info("Call was hung up. Terminating.");
        }
    }
}
