package fr.ipgp.earlywarning.gateway;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.asterisk.CallOriginator;
import fr.ipgp.earlywarning.asterisk.LocalAgiServer;
import fr.ipgp.earlywarning.messages.AudioWarningMessage;
import fr.ipgp.earlywarning.telephones.Contact;
import fr.ipgp.earlywarning.telephones.FileCallList;
import fr.ipgp.earlywarning.telephones.JSONContactList;
import fr.ipgp.earlywarning.triggers.Trigger;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;

import java.io.File;
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

    public String callTillConfirm(String logFile, String messageFile, String confirmCode, String[] phoneNumbers) {
        return callTillConfirm(Arrays.asList(phoneNumbers), messageFile, confirmCode);
    }

    @Override
    public String callTillConfirm(String logFile, String messageFile, String confirmCode, FileCallList callList) {
        EarlyWarning.appLogger.fatal("AsteriskGateway doesn't handle FileCallList");
        return null;
    }


    public String callTillConfirm(List<String> numbers, String messageFile, String confirmCode) {
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

        return "OK";
    }

    @Override
    public String callTillConfirm(String callList, String messageFile, String confirmCode) {
        File f = new File(callList);
        if (!f.exists()) {
            if (callList.contains(",")) {
                EarlyWarning.appLogger.warn("AsteriskGateway callList is not a file: " + callList + ". Treating as a text-list.");
                List<String> contacts = Arrays.asList(callList.split(","));
                return callTillConfirm(contacts, messageFile, confirmCode);
            } else {
                EarlyWarning.appLogger.warn("AsteriskGateway callList is not a file: " + callList + ". Using default JSON contact list.");
                try {
                    JSONContactList list = new JSONContactList(EarlyWarning.configuration.getString("contacts.file"));
                    List<Contact> contacts = list.getCallList();

                    List<String> numbers = new ArrayList<>();
                    for (Contact c : contacts)
                        numbers.add(c.phone);

                    return callTillConfirm(numbers, messageFile, confirmCode);
                } catch (IOException ignored) {
                    // This can't happen: we verified that the file exists
                    return null;
                }
            }
        } else {
            try {
                JSONContactList list = new JSONContactList(callList);
                List<Contact> contacts = list.getCallList();

                List<String> numbers = new ArrayList<>();
                for (Contact c : contacts)
                    numbers.add(c.phone);

                return callTillConfirm(numbers, messageFile, confirmCode);
            } catch (IOException ignored) {
                // This can't happen: we verified that the file exists
                return null;
            }
        }
    }

    @Override
    public String callTillConfirm(Trigger trigger, AudioWarningMessage defaultWarningMessage) {
        String confirmCode = trigger.getConfirmCode();
        String file;
        switch (trigger.getMessage().getType()) {
            case AUDIO:
                file = ((AudioWarningMessage) trigger.getMessage()).getFile();

                // In typical triggers, audio files' names end with .wav, we want to remove this
                if (file.toLowerCase().endsWith(".wav"))
                    file = file.substring(0, file.length() - 4);

                break;

            default:
                file = defaultWarningMessage.getFile();
        }

        return callTillConfirm(trigger.getCallList().getName(), file, confirmCode);
    }
}
