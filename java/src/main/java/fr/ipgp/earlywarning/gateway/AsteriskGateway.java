package fr.ipgp.earlywarning.gateway;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.asterisk.CallOriginator;
import fr.ipgp.earlywarning.messages.GSMWarningMessage;
import fr.ipgp.earlywarning.messages.WAVWarningMessage;
import fr.ipgp.earlywarning.messages.WarningMessage;
import fr.ipgp.earlywarning.telephones.Contact;
import fr.ipgp.earlywarning.telephones.FileCallList;
import fr.ipgp.earlywarning.telephones.JSONContactList;
import fr.ipgp.earlywarning.triggers.Trigger;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static fr.ipgp.earlywarning.asterisk.CallOriginator.CallResult.CorrectCode;
import static fr.ipgp.earlywarning.asterisk.CallOriginator.CallResult.Initial;

public class AsteriskGateway implements Gateway {
    private String host;
    private int port;
    private String username;
    private String password;

    ManagerConnection buildManagerConnection()
    {
        ManagerConnectionFactory factory = new ManagerConnectionFactory(host, port, username, password);
        ManagerConnection managerConnection = factory.createManagerConnection();
        return managerConnection;
    }

    public AsteriskGateway(String host, int port, String username, String password)
    {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    @Override
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

    @Override
    public String callTillConfirm(String logFile, String messageFile, String confirmCode, String[] phoneNumbers) {
        return null;
    }

    @Override
    public String callTillConfirm(String logFile, String messageFile, String confirmCode, FileCallList callList) {
        return null;
    }

    @Override
    public String callTillConfirm(String callList, String messageFile, String confirmCode) throws IOException {
        File f = new File(callList);
        if (!f.exists())
            throw new FileNotFoundException("CallList doesn't exist.");

        CallOriginator originator = new CallOriginator(buildManagerConnection(), confirmCode);

        JSONContactList list = new JSONContactList(callList);

        List<Contact> contacts = list.getEnabledContacts();
        Iterator<Contact> iterator = contacts.iterator();

        // Assert: there is at least one enabled contact
        assert iterator.hasNext();

        CallOriginator.CallResult result = Initial;
        do {
            if (!iterator.hasNext())
                iterator = contacts.iterator();

            Contact toCall = iterator.next();

            try {
                result = originator.call(toCall.phone);
            } catch (AuthenticationFailedException ignored) {
                // We can ignore this exception since we test at launch time that the credentials are correct.
            } catch (TimeoutException e) {
                EarlyWarning.appLogger.error("Origination request timed out for contact " + toCall.name);
            }

        } while (result != CorrectCode);

        return "0";
    }

    @Override
    public String callTillConfirm(Trigger trigger, WarningMessage defaultWarningMessage) throws IOException {
        String confirmCode = trigger.getConfirmCode();
        String file;
        switch (trigger.getMessage().getType()) {
            case GSM:
                file = ((WAVWarningMessage)trigger.getMessage()).getFile();
                break;
            default:
                file = ((GSMWarningMessage) defaultWarningMessage).getFile();
        }

        return callTillConfirm(trigger.getCallList().getName(), file, confirmCode);
    }
}
