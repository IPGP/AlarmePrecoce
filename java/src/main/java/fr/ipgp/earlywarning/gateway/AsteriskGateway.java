package fr.ipgp.earlywarning.gateway;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.asterisk.CallOriginator;
import fr.ipgp.earlywarning.asterisk.LocalAgiServer;
import fr.ipgp.earlywarning.messages.WarningMessageMapper;
import fr.ipgp.earlywarning.contacts.Contact;
import fr.ipgp.earlywarning.contacts.ContactList;
import fr.ipgp.earlywarning.triggers.Trigger;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static fr.ipgp.earlywarning.asterisk.CallOriginator.CallResult.CorrectCode;
import static fr.ipgp.earlywarning.asterisk.CallOriginator.CallResult.Initial;

/**
 * A gateway that uses an Asterisk server to originate and handle calls.
 *
 * @author Thomas Kowalski
 */
public class AsteriskGateway implements Gateway {
    /**
     * The {@link LocalAgiServer} to use to execute the {@link fr.ipgp.earlywarning.asterisk.AlertCallScript}
     */
    LocalAgiServer server;

    /**
     * The AMI host
     */
    private String host;

    /**
     * The AMI port
     */
    private int port;

    /**
     * The AMI username
     */
    private String username;

    /**
     * The AMI password
     */
    private String password;

    /**
     * Valued constructor with the necessary parameters to use an {@link AsteriskGateway}, which are the AMI information and credentials
     *
     * @param host     the AMI host
     * @param port     the AMI port
     * @param username the AMI manager's name
     * @param password the AMI manager's password
     */
    public AsteriskGateway(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;

        server = new LocalAgiServer();
    }

    /**
     * Builds a {@link ManagerConnection} to use in the {@link CallOriginator}
     *
     * @return a new {@link ManagerConnection} that uses the local <code>host, port, username, password</code>
     */
    ManagerConnection buildManagerConnection() {
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
    private void callTillConfirm(List<String> numbers, String warningMessageFile, String confirmCode) {
        CallOriginator originator = new CallOriginator(buildManagerConnection(), confirmCode, warningMessageFile);

        Iterator<String> iterator = numbers.iterator();

        // Assert: there is at least one enabled contact
        assert iterator.hasNext();

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

            try {
                result = originator.call(toCall);
            } catch (org.asteriskjava.manager.AuthenticationFailedException ignored) {
                // We can ignore this exception since we test at launch time that the credentials are correct.
                EarlyWarning.appLogger.error("Wrong authentication.");
            } catch (org.asteriskjava.manager.TimeoutException e) {
                EarlyWarning.appLogger.error("Origination request timed out for number " + toCall);
            } catch (IOException ex) {
                EarlyWarning.appLogger.error(ex.getMessage());
            }

        } while (result != CorrectCode);
    }

    /**
     * Transforms the {@link ContactList} into a real<code>{@link List} of {@link Contact}</code> and calls a more specialized method to originate the call.
     *
     * @param list               the {@link ContactList} to use
     * @param warningMessageFile the warning message file to play
     * @param confirmCode        the confirmation code to use
     */
    public void callTillConfirm(ContactList list, String warningMessageFile, String confirmCode) {
        List<Contact> contacts = list.getCallList();

        List<String> numbers = new ArrayList<>();
        for (Contact c : contacts)
            numbers.add(c.phone);

        callTillConfirm(numbers, warningMessageFile, confirmCode);
    }

    /**
     * Extracts the data from the trigger and calls a more specialized method to originate the call.
     *
     * @param trigger that triggered the call
     */
    @Override
    public void callTillConfirm(Trigger trigger) {
        String confirmCode = trigger.getConfirmCode();
        String warningMessageFile = WarningMessageMapper.getInstance(this).getNameOrDefault(trigger.getMessage());

        callTillConfirm(trigger.getContactList(), warningMessageFile, confirmCode);
    }

    @Override
    public void callTest(String number) {
        // TODO: implement callTest in AsteriskGateway
    }

    @Override
    public String getSettingsQualifier() {
        return "asterisk";
    }
}
