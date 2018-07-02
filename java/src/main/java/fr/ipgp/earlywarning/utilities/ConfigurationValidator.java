package fr.ipgp.earlywarning.utilities;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.telephones.ContactList;
import fr.ipgp.earlywarning.telephones.ContactListBuilder;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.IOException;
import java.util.*;

import static fr.ipgp.earlywarning.utilities.ConfigurationValidator.OnError.Exit;

/**
 * Configuration validator.
 * Reads a given <code>XMLConfiguration</code> and verifies everything it has to contain exists and is correct.
 * All the tests that can be made are made (file existence for adequate fields, etc.) and in case of doubt, warnings are emitted.
 *
 * @author Thomas Kowalski
 */
public class ConfigurationValidator {
    public enum OnError {
        Exit,
        Warn
    }

    private XMLConfiguration configuration;
    private OnError onError;

    public static final OnError defaultBehaviour = Exit;

    public ConfigurationValidator(XMLConfiguration configuration, OnError behaviour) {
        onError = behaviour;
        this.configuration = configuration;
    }

    public ConfigurationValidator(XMLConfiguration configuration) {
        this(configuration, defaultBehaviour);
    }

    public void validate() {
        EarlyWarning.appLogger.info("------------------------------------");
        EarlyWarning.appLogger.info("Verifying configuration.");
        try {
            validateContactsServer();
            validateSounds();
        } catch (ValidationException ex) {
            if (onError == Exit) {
                EarlyWarning.appLogger.fatal("Configuration validation error: ");
                EarlyWarning.appLogger.fatal(ex.toString());
                System.exit(-1);
            } else {
                EarlyWarning.appLogger.warn("Configuration validation error: ");
                EarlyWarning.appLogger.warn(ex.toString());
            }
        }
        EarlyWarning.appLogger.info("Configuration validation finished.");
    }

    private void validateContactsServer() throws ValidationException {
        String home;
        try {
            home = configuration.getString("contacts.home");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("contacts.home", "Key doesn't exist.");
        }

        int port;
        try {
            port = configuration.getInt("contacts.port");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("contaacts.port", "Key doesn't exist.");
        } catch (ConversionException ex) {
            throw new ValidationException("contacts.port", "Not an integer");
        }

        String defaultPath;
        try {
            defaultPath = configuration.getString("contacts.lists.default");
            ContactList list = ContactListBuilder.build(defaultPath);
            if (list.getEnabledContacts().isEmpty())
                EarlyWarning.appLogger.warn("Your default contact list ('" + defaultPath + "') currently does not have anyone on the call list. You should fix this or no call will be emitted.");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("contacts.lists.default", "Key doesn't exist.");
        } catch (IOException e) {
            throw new ValidationException("contacts.lists.default", "File cannot be read or written.");
        }
    }

    private List<String> getEntries(String prefix) {
        List<String> result = new ArrayList<>();
        for (Iterator<String> it = configuration.getKeys(prefix); it.hasNext(); )
            result.add(it.next());

        return result;
    }

    private int occurrences(String s, char c) {
        int count = 0;
        for (char c2 : s.toCharArray())
            if (c2 == c)
                count++;
        return count;
    }

    private Set<String> getTopLevelEntries(String prefix) {
        List<String> all = getEntries(prefix);
        Set<String> result = new HashSet<>();
        for (String entry : all) {
            String[] split = entry.split("\\.");
            result.add(split[occurrences(prefix, '.') + 1]);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private void validateSounds() {
        Set<String> soundNames = new HashSet<>(getTopLevelEntries("sounds"));
        Set<String> gateways = new HashSet<>();
        Map<String, Set<String>> availableGateways = new HashMap<>();

        for (String sound : soundNames) {
            availableGateways.put(sound, new HashSet<String>());

            for (String gateway : getTopLevelEntries("sounds." + sound + "")) {
                gateways.add(gateway);
                availableGateways.get(sound).add(gateway);
            }
        }

        for (String gateway : gateways)
            for (String sound : soundNames)
                if (!availableGateways.get(sound).contains(gateway))
                    EarlyWarning.appLogger.warn("Sound '" + sound + "' has no mapping for gateway '" + gateway + "'");
    }

    protected class ValidationException extends Throwable {
        String parameter;
        String problem;

        public ValidationException(String parameter, String problem) {
            this.parameter = parameter;
            this.problem = problem;
        }

        @Override
        public String toString() {
            return "ValidationException on parameter '" + parameter + "': " + problem;
        }
    }

}
