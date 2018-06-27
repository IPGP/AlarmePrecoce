package fr.ipgp.earlywarning.asterisk;

import org.asteriskjava.manager.*;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.event.DbGetResponseEvent;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.StatusEvent;
import org.asteriskjava.manager.response.ManagerResponse;

import java.io.IOException;

/**
 * Object used to originate alert calls and handle callee's behaviour.
 * Usage:
 * <ul>
 * <li>Call valued constructor to initialize AMI Credentials (you only need to do this once)</li>
 * <li>OR Call valued constructor with only a confirmation code if you have already called the complete one once.</li>
 * <li>Call the <code>{@link CallOriginator}</code>'s <code>call</code> method, giving a phone number to dial. It will return a status ({@link CallResult} corresponding to the call result: <code>CorrectCode</code> if everything went fine, something else otherwise.</li>
 * </ul>
 *
 * @author Thomas Kowalski
 */
public class CallOriginator implements ManagerEventListener {
    /**
     * Constants to ease working with time.
     */
    @SuppressWarnings("unused")
    private static final int SECOND = 1000;
    @SuppressWarnings("unused")
    private static final int MINUTE = 60 * SECOND;
    /**
     * The maximum amount of time to ring before giving up the call. It's not necessarily the real value: the call will be hung up when the <code>ringTimeout</code> is over AND the welcome audio file finished.
     */
    private static final int ringTimeout = 15 * SECOND;
    /**
     * The timeout (in milliseconds) for the ManagerAction to be accepted by the server
     */
    private static final long TIMEOUT = 30000;
    private static String amiHostname = null;
    private static int amiPort = -1;
    private static String managerUsername = null;
    private static String managerPassword = null;
    /**
     * The connection to AMI.
     */
    private ManagerConnection managerConnection;

    /**
     * Indicates whether or not the AMI connection has been constructed by this object.
     * If it has, we have the liability of log off at the end of the script.
     */
    private boolean localConnection;

    /**
     * Indicates if the callee has acknowledged the welcome message
     */
    private boolean connected = false;
    /**
     * Listener used by the AGI script to let the {@link CallOriginator} know the callee acknowledged the wekcome message.
     */
    // TODO: Java 8 lambda migration
    @SuppressWarnings("Convert2Lambda")
    private final OnConnectedListener onConnectedListener = new OnConnectedListener() {
        @Override
        public void onConnected() {
            connected = true;
        }
    };
    /**
     * Indicates whether or not the call has been hung up (by the callee or the script)
     */
    private boolean hungUp = false;
    /**
     * Listener used by the AGI script to let the {@link CallOriginator} know the call was hung up.
     * It may happen that using this signal method is way faster than waiting for the HangupManagerEvent.
     */
    // TODO: Java 8 lambda migration
    @SuppressWarnings("Convert2Lambda")
    private final OnHangupListener onHangupListener = new OnHangupListener() {
        @Override
        public void onHangup() {
            System.out.println("Received hangup signal");
            hungUp = true;
        }
    };
    /**
     * Next action to take on the AGI script's side: ask for a code again, confirm auth or give up.
     */
    private CallAction callAction = CallAction.Retry;
    /**
     * The confirmation code to use
     */
    private String confirmCode;
    /**
     * The maximum number of times the callee can enter a code again before giving up.
     */
    private int remainingTries = 3;
    /**
     * Listener called by the AGI script when the callee has entered a code.
     */
    private final OnCodeReceivedListener onCodeReceivedListener = new OnCodeReceivedListener() {
        public CallAction onCodeReceived(String code) {
            System.out.println("Received code : " + code);

            if (code.equalsIgnoreCase(confirmCode))
                // If the code is correct, return a Correct state
                callAction = CallAction.Correct;
            else {
                if (--remainingTries > 0)
                    // If the code is incorrect and the user doesn't have any try left
                    callAction = CallAction.Retry;
                else
                    // If the user can try again
                    callAction = CallAction.GiveUp;
            }

            return callAction;
        }
    };

    /**
     * Setter constructor.
     * Only needs to be called once, to set the static auth fields.
     *
     * @param host     the AMI host
     * @param port     the AMI port
     * @param user     the AMI Manager's username
     * @param password the AMI Manager's password
     * @param code     the confirmation code to be used ; can be changed later
     */
    public CallOriginator(String host, int port, String user, String password, String code) {
        amiHostname = host;
        amiPort = port;
        managerUsername = user;
        managerPassword = password;

        ManagerConnectionFactory factory = new ManagerConnectionFactory(amiHostname, amiPort, managerUsername, managerPassword);
        this.managerConnection = factory.createManagerConnection();
        this.localConnection = true;

        this.confirmCode = code;
    }

    /**
     * External ManagerConnection constructor. Allows us to keep the same ManagerConnection for multiple calls.
     *
     * @param connection the ManagerConnection to use
     * @param code       the confirmation code
     */
    public CallOriginator(ManagerConnection connection, String code) {
        // If we get a null ManagerConnection, we can't create a default one without credentials.
        assert connection != null;

        this.managerConnection = connection;
        this.localConnection = false;
        this.confirmCode = code;
    }

    /**
     * Shortcut constructor: builds a new <code>CallManager</code> with the same credentials as before, only changing the confirmation code.
     *
     * @param code the new confirmation code
     */
    public CallOriginator(String code) {
        // Verify that none of the authentication fields is empty
        if (amiHostname == null || amiHostname.isEmpty()
                || amiPort < 0
                || managerUsername == null || managerUsername.isEmpty()
                || managerPassword == null || managerPassword.isEmpty())
            throw new NullPointerException("The shortcut constructor should only be called after calling the full constructor.");

        // Initialize a new ManagerConnection
        ManagerConnectionFactory factory = new ManagerConnectionFactory(amiHostname, amiPort, managerUsername, managerPassword);
        this.managerConnection = factory.createManagerConnection();
        this.localConnection = true;

        // Set the confirmation code
        confirmCode = code;
    }

    @SuppressWarnings("SameParameterValue")
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {

        }
    }

    /**
     * Logs off the AMI, verifying first that:
     * <ul>
     * <li><code>managerConnection</code> is not <code>null</code></li>
     * <li><code>managerConnection</code> is in state <code>CONNECTED</code> (otherwise it's impossible to log off)</li>
     * </ul>
     */
    private void logoff() {
        if (managerConnection == null)
            return;

        if (managerConnection.getState() == ManagerConnectionState.CONNECTED)
            managerConnection.logoff();
    }

    /**
     * Used to factor the channel name generation.
     *
     * @param number the phone number to call
     * @return the channel name to use: "SIP/pstn-out/[number]
     */
    private String getChannelName(String number) {
        return "SIP/pstn-out/" + number;
    }

    /**
     * Initializes the AMI conection and binds the AGI script listeners.
     *
     * @throws AuthenticationFailedException if the AMI credentials are incorrect
     * @throws TimeoutException              if the AMI authentication times out
     * @throws IOException                   if ManagerEvents listeners can't be set
     */
    private void init() throws AuthenticationFailedException, TimeoutException, IOException {
        hungUp = false;
        connected = false;

        AlertCallScript.setOnCodeReceivedListener(onCodeReceivedListener, confirmCode.length());
        AlertCallScript.setOnHangupListener(onHangupListener);
        AlertCallScript.setOnConnectedListener(onConnectedListener);

        if (managerConnection.getState() != ManagerConnectionState.CONNECTED
                && managerConnection.getState() != ManagerConnectionState.CONNECTING) {
            managerConnection.login();
            managerConnection.addEventListener(this);
            managerConnection.registerUserEventClass(DbGetResponseEvent.class);
            managerConnection.registerUserEventClass(HangupEvent.class);
            managerConnection.registerUserEventClass(StatusEvent.class);
        }
    }

    /**
     * Builds an OriginateAction for a phone number
     *
     * @param number the phone number to call
     * @return an initialized and usable OriginateAction
     */
    private OriginateAction buildOriginateAction(String number) {
        OriginateAction originateAction;
        originateAction = new OriginateAction();
        originateAction.setChannel(getChannelName(number));
        originateAction.setExten(number);
        originateAction.setTimeout(new Long(10000));
        originateAction.setApplication("AGI");
        originateAction.setData("agi://localhost/alertcall.agi");
        return originateAction;
    }

    /**
     * The main function of this class.
     * It is mainly a state machine that communicates with the AGI script in order to retrieve the code and validate user input.
     *
     * @param number the phone number to call
     * @return The {@link CallResult} corresponding to the call state, see {@link CallResult}
     * @throws AuthenticationFailedException if AMI credentials are incorrect
     * @throws TimeoutException              if the AMI authentication times out
     */
    public CallResult call(String number) throws IOException, AuthenticationFailedException, TimeoutException {
        // Initialize the ManagerConnection and the event listeners
        init();

        // Construct the OriginateAction and originate the call
        OriginateAction originateAction = buildOriginateAction(number);
        ManagerResponse originateResponse = managerConnection.sendAction(originateAction, TIMEOUT);

        String originateResponseString = originateResponse.getResponse();
        if (!originateResponseString.equalsIgnoreCase("Success")) {
            if (localConnection)
                logoff();

            return CallResult.Error;
        }

        long callBeginTime = System.currentTimeMillis();

        while (!connected) {
            if (System.currentTimeMillis() - callBeginTime > ringTimeout) {
                // If the ringing timeout is over
                System.err.println("Requesting hangup");
                AlertCallScript.requestHangup();

                // Wait for the call to be hung up before returning
                //noinspection WhileLoopSpinsOnField
                while (!hungUp)
                    sleep(100);

                if (localConnection)
                    logoff();
                return CallResult.Busy;
            }
            sleep(100);
        }

        /*
        // If the call was hung up before any code was entered
        if (hungUp) {
            logoff();
            return CallResult.Hangup;
        }
        */

        // Wait for the code to be received.
        // callAction is not modified in this loop but can be modified by the OnCodeReceivedListener
        // While the code entered is wrong (and there are retries left), callAction value stays Retry
        while (callAction == CallAction.Retry) {
            if (hungUp) {
                if (localConnection)
                    logoff();

                return CallResult.Hangup;
            }

            sleep(100);
        }

        // Wait for the call to be hung up
        // The point of doing this is to prevent a case where the call would still be active
        // And the CallOriginator's owner would try to originate a new call
        while (!hungUp)
            sleep(100);

        System.out.println("Hung up!");

        switch (callAction) {
            case Correct:
                if (localConnection)
                    logoff();

                return CallResult.CorrectCode;
            case GiveUp:
                if (localConnection)
                    logoff();

                return CallResult.IncorrectCode;
            case Retry:
                throw new IllegalStateException("CallAction should not be Retry.");
            default:
                throw new IllegalStateException("Unknown CallAction value: " + callAction);
        }
    }

    /**
     * ManagerEvent handler
     *
     * @param managerEvent the ManagerEvent to be handled.
     */
    @Override
    public void onManagerEvent(ManagerEvent managerEvent) {
        if (managerEvent.getClass() == HangupEvent.class)
            hungUp = true;
    }

    /**
     * What the AGI script should do next (after reading a DTMF code)
     */
    public enum CallAction {
        /**
         * The code was correct.
         */
        Correct,
        /**
         * The code was incorrect, but the callee has the right to retry.
         */
        Retry,
        /**
         * The code was incorrect and the callee doesn't have any try left.
         */
        GiveUp
    }

    /**
     * The result of a call session.
     */
    public enum CallResult {
        /**
         * No call emitted yet.
         */
        Initial,
        /**
         * The callee did not answer.
         */
        Busy,
        /**
         * The callee didn't enter the correct confirmation code [after several tries].
         */
        IncorrectCode,
        /**
         * The callee confirmed his / her identity with the confirmation code.
         */
        CorrectCode,
        /**
         * The callee hung up during the call and before entering the confirmation code.
         */
        Hangup,
        /**
         * The call could not be emitted
         */
        Error
    }
}
