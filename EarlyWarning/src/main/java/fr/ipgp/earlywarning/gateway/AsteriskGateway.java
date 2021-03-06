package fr.ipgp.earlywarning.gateway;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.asterisk.CallOriginator;
import fr.ipgp.earlywarning.asterisk.LocalAgiServer;
import fr.ipgp.earlywarning.contacts.Contact;
import fr.ipgp.earlywarning.contacts.ContactList;
import fr.ipgp.earlywarning.contacts.ContactListMapper;
import fr.ipgp.earlywarning.messages.WarningMessageMapper;
import fr.ipgp.earlywarning.triggers.Trigger;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static fr.ipgp.earlywarning.asterisk.CallOriginator.CallResult.*;

/**
 * A gateway that uses an Asterisk server to originate and handle calls.
 *
 * @author Thomas Kowalski
 */
public class AsteriskGateway implements Gateway {
    private static AsteriskGateway uniqueInstance;
    /**
     * The {@link LocalAgiServer} to use to execute the {@link fr.ipgp.earlywarning.asterisk.AlertCallScript}
     */
    private final LocalAgiServer server;
    /**
     * The AMI host
     */
    private final String host;
    /**
     * The AMI port
     */
    private final int port;
    /**
     * The AMI username
     */
    private final String username;
    /**
     * The AMI password
     */
    private final String password;
    /**
     * The number of retries on errors before switching to failover
     */
    private final int retriesBeforeFailover;

    /**
     * Valued constructor with the necessary parameters to use an {@link AsteriskGateway}, which are the AMI information and credentials
     *
     * @param host                  the AMI host
     * @param port                  the AMI port
     * @param username              the AMI manager's name
     * @param password              the AMI manager's password
     * @param retriesBeforeFailover the number of retries on errors before switching to failover
     */
    private AsteriskGateway(String host, int port, String username, String password, int retriesBeforeFailover) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
        this.retriesBeforeFailover = retriesBeforeFailover;

        server = new LocalAgiServer();
    }

    /**
     * @param host     the AMI host
     * @param port     the AMI port
     * @param username the AMI manager's name
     * @param password the AMI manager's password
     * @param retriesBeforeFailover the number of retries on errors before switching to failover
     */
    public static AsteriskGateway getInstance(String host, int port, String username, String password, int retriesBeforeFailover) {
        if (uniqueInstance == null)
            uniqueInstance = new AsteriskGateway(host, port, username, password, retriesBeforeFailover);

        return uniqueInstance;
    }

    /**
     * Builds a {@link ManagerConnection} to use in the {@link CallOriginator}
     *
     * @return a new {@link ManagerConnection} that uses the local <code>host, port, username, password</code>
     */
    private ManagerConnection buildManagerConnection() {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(host, port, username, password);
        return factory.createManagerConnection();
    }

    /**
     * Originates calls until a call is confirmed by the callee
     *
     * @param numbers            the phone numbers to call
     * @param warningMessageFile the warning message file to play
     * @param confirmCode        the confirmation code to use
     */
    private CallLoopResult callTillConfirm(List<String> numbers, String warningMessageFile, String confirmCode) {
        CallOriginator originator = new CallOriginator(buildManagerConnection(), confirmCode, warningMessageFile);

        Iterator<String> iterator = numbers.iterator();

        // Assert: there is at least one enabled contact
        assert iterator.hasNext();

        int originateTrials = 0;
        // Retries before failover default value
        CallOriginator.CallResult result = Initial;
        do {
            if (result != Initial) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ignored) {
                }
            }

            if (!iterator.hasNext())
                iterator = numbers.iterator();

            String toCall = iterator.next();
            EarlyWarning.appLogger.info("Calling " + toCall);

            result = CallOriginator.CallResult.DialRequested;
            try {
                result = originator.call(toCall);
            } catch (org.asteriskjava.manager.AuthenticationFailedException ignored) {
                // We can ignore this exception since we test at launch time that the credentials are correct.
                EarlyWarning.appLogger.error("Wrong authentication.");
            } catch (org.asteriskjava.manager.TimeoutException ex) {
                EarlyWarning.appLogger.error("Origination request timed out for number " + toCall);
            } catch (IOException ex) {
                EarlyWarning.appLogger.error("Could not originate call (IOException): " + ex.getMessage());
            }

            if (result == DialRequested)
                // An exception has occurred
                result = CallOriginator.CallResult.Error;

            if (result == CallOriginator.CallResult.Error) {
                originateTrials++;
                if (originateTrials >= this.retriesBeforeFailover) {
                    EarlyWarning.appLogger.error("could not originate call after " + originateTrials + " retries. Using failover system.");
                    return CallLoopResult.Error;
                } else {
                	EarlyWarning.appLogger.error("Error while originating call. Retrying.");
                }
            }
        } while (result != CorrectCode);

        return CallLoopResult.Confirmed;
    }

    /**
     * Transforms the {@link ContactList} into a real<code>{@link List} of {@link Contact}</code> and calls a more specialized method to originate the call.
     *
     * @param list               the {@link ContactList} to use
     * @param warningMessageFile the warning message file to play
     * @param confirmCode        the confirmation code to use
     */
    private CallLoopResult callTillConfirm(ContactList list, String warningMessageFile, String confirmCode) {
        List<Contact> contacts = list.getCallList();

        List<String> numbers = new ArrayList<>();
        for (Contact c : contacts)
            numbers.add(c.phone);

        if (numbers.size() > 0)
            return callTillConfirm(numbers, warningMessageFile, confirmCode);

        if (!ContactListMapper.getInstance().getDefaultList().equals(list)) {
            EarlyWarning.appLogger.error("No numbers in requested call list. Calling again with default call list.");
            return callTillConfirm(ContactListMapper.getInstance().getDefaultList(), warningMessageFile, confirmCode);
        } else {
            EarlyWarning.appLogger.fatal("Default call list is empty. No call can be originated.");
            return CallLoopResult.EmptyDefaultList;
        }

    }

    /**
     * Extracts the data from the trigger and calls a more specialized method to originate the call.
     *
     * @param trigger that triggered the call
     */
    public CallLoopResult callTillConfirm(Trigger trigger) {
        String confirmCode = trigger.getConfirmCode();
        String warningMessageFile = WarningMessageMapper.getInstance(this).getNameOrDefault(trigger.getMessage());

        return callTillConfirm(trigger.getContactList(), warningMessageFile, confirmCode);
    }

    public void callTest(String number) {
        // TODO: implement callTest in AsteriskGateway
    }

    @Override
    public String toString() {
        return "Asterisk Gateway (" + username + "@" + host + ":" + port + ")";
    }

    public String getSettingsQualifier() {
        return "asterisk";
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null)
            return false;

        if (o == this)
            return true;

        if (o instanceof Gateway)
            return equals((Gateway)o);

        return false;
    }

    private boolean equals(Gateway g)
    {
        if (!g.getSettingsQualifier().equals(getSettingsQualifier()))
            return false;

        return true;
    }
}
