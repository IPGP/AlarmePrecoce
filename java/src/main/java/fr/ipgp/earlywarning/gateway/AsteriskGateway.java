package fr.ipgp.earlywarning.gateway;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.asterisk.CallOriginator;
import fr.ipgp.earlywarning.asterisk.LocalAgiServer;
import fr.ipgp.earlywarning.messages.WarningMessageMapper;
import fr.ipgp.earlywarning.telephones.Contact;
import fr.ipgp.earlywarning.telephones.ContactList;
import fr.ipgp.earlywarning.triggers.Trigger;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static fr.ipgp.earlywarning.asterisk.CallOriginator.CallResult.CorrectCode;
import static fr.ipgp.earlywarning.asterisk.CallOriginator.CallResult.Initial;

public class AsteriskGateway implements Gateway {
    private String host;
    private int port;
    private String username;
    private String password;

    LocalAgiServer server;

    ManagerConnection buildManagerConnection() {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(host, port, username, password);
        ManagerConnection managerConnection = factory.createManagerConnection();
        return managerConnection;
    }

    public AsteriskGateway(String host, int port, String username, String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;

        server = new LocalAgiServer();
    }

    public String callAudio(String phoneNumber, String audioFile, boolean selfDelete) {
        CallOriginator originator = new CallOriginator(buildManagerConnection(), "");
        try {
            CallOriginator.CallResult result = originator.call(phoneNumber);
            if (result == CorrectCode)
                return "Correct";
            else
                return "Incorrect";
        } catch (IOException e) {
            return "IOException";
        } catch (AuthenticationFailedException e) {
            return "AuthenticationFailed";
        } catch (TimeoutException e) {
            return "RequestTimeout";
        }
    }

    public void callTillConfirm(List<String> numbers, String messageFile, String confirmCode) {
        CallOriginator originator = new CallOriginator(buildManagerConnection(), confirmCode);

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

    public void callTillConfirm(ContactList list, String messageFile, String confirmCode) {
        List<Contact> contacts = list.getCallList();

        List<String> numbers = new ArrayList<>();
        for (Contact c : contacts)
            numbers.add(c.phone);

        callTillConfirm(numbers, messageFile, confirmCode);
    }


    @Override
    public void callTillConfirm(Trigger trigger) {
        String confirmCode = trigger.getConfirmCode();
        String message = WarningMessageMapper.getInstance(this).getNameOrDefault(trigger.getMessage());

        callTillConfirm(trigger.getContactList(), message, confirmCode);
    }

    @Override
    public void callTest(String number) {
        // TODO: implement callTest in AsteriskGateway
    }

    @Override
    public String getSettingsQualifier() {
        return null;
    }
}
