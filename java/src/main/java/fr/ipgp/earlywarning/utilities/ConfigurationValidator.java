package fr.ipgp.earlywarning.utilities;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.telephones.ContactList;
import fr.ipgp.earlywarning.telephones.ContactListBuilder;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.XMLConfiguration;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;

import java.io.IOException;
import java.util.*;

/**
 * Configuration validator.
 * Reads a given <code>XMLConfiguration</code> and verifies everything it has to contain exists and is correct.
 * All the tests that can be made are made (file existence for adequate fields, etc.) and in case of doubt, warnings are emitted.
 *
 * @author Thomas Kowalski
 */
public class ConfigurationValidator {
    /**
     * The default behaviour on invalid configuration.
     */
    private static final OnError defaultBehaviour = OnError.Exit;
    /**
     * A reference to the main <code>XMLConfiguration</code>
     */
    private final XMLConfiguration configuration;

    /**
     * The action to take when an invalid configuration entry is detected.
     */
    private final OnError onError;

    /**
     * Constructor with custom On Error behaviour.
     *
     * @param configuration the configuration to validate
     * @param behaviour     the behaviour to adopt on error
     */
    @SuppressWarnings("SameParameterValue")
    private ConfigurationValidator(XMLConfiguration configuration, OnError behaviour) {
        onError = behaviour;
        this.configuration = configuration;
    }

    /**
     * The simplest constructor that uses the class' default on error behaviour.
     *
     * @param configuration the configuration to validate
     */
    public ConfigurationValidator(XMLConfiguration configuration) {
        this(configuration, defaultBehaviour);
    }

    /**
     * Validates the whole <code>XMLConfiguration</code>, taking measures depending on the <code>onError</code> behaviour.
     */
    public void validate() {
        EarlyWarning.appLogger.info("------------------------------------");
        EarlyWarning.appLogger.info("Verifying configuration.");
        try {
            validateContactsServer();
            validateSounds();
            validateGatewaySettings();
        } catch (ValidationException ex) {
            switch (onError) {
                case Exit:
                    EarlyWarning.appLogger.fatal("Configuration validation error: ");
                    EarlyWarning.appLogger.fatal(ex.toString());
                    System.exit(-1);
                    break;
                case Warn:
                    EarlyWarning.appLogger.warn("Configuration validation error: ");
                    EarlyWarning.appLogger.warn(ex.toString());
                    break;
            }
        }
        EarlyWarning.appLogger.info("Configuration validation finished.");
    }

    /**
     * Validates the main gateway settings (gateway type choice) and calls the adequate validation method for the gateway.
     *
     * @throws ValidationException if a configuration error is detected
     */
    private void validateGatewaySettings() throws ValidationException {
        String activeGateway;
        try {
            activeGateway = configuration.getString("gateway.active");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("gateway.active", "Key does not exist.");
        }

        /* All the gateways that can be used */
        Set<String> availableGateways = new HashSet<>();
        availableGateways.add("asterisk");

        if (!availableGateways.contains(activeGateway.toLowerCase()))
            throw new ValidationException("gateway.active", "Unknown active Gateway");

        if (activeGateway.equalsIgnoreCase("asterisk"))
            validateAsteriskSettings();

    }

    /**
     * Validates the Asterisk settings. This should only be called by <code>validateGatewaySettings</code> once it has determined what gateway is being used.
     * Verifies that the given values are coherent and that a connection to Asterisk Manager Interface can be established.
     *
     * @throws ValidationException if a configuration error is detected
     */
    private void validateAsteriskSettings() throws ValidationException {
        int retries;
        try {
            //noinspection UnusedAssignment
            retries = configuration.getInt("gateway.asterisk.retries");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("gateway.asterisk.retries", "Key does not exist.");
        } catch (ConversionException ex) {
            throw new ValidationException("gateway.asterisk.retries", "Value cannot be converted to Integer : '" + configuration.getString("gateway.asterisk.retries") + "'");
        }

        int ringTimeout;
        try {
            //noinspection UnusedAssignment
            ringTimeout = configuration.getInt("gateway.asterisk.ring_timeout");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("gateway.asterisk.ring_timeout", "Key does not exist.");
        } catch (ConversionException ex) {
            throw new ValidationException("gateway.asterisk.ring_timeout", "Value cannot be converted to Integer : '" + configuration.getString("gateway.asterisk.ring_timeout") + "'");
        }

        String agiHost;
        try {
            //noinspection UnusedAssignment
            agiHost = configuration.getString("gateway.asterisk.agi_server_host");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("gateway.asterisk.agi_server_host", "Key does not exist.");
        }

        String[] params = {"ami_host", "ami_port", "ami_user", "ami_password"};
        Map<String, String> values = new HashMap<>();
        for (String paramName : params) {
            try {
                values.put(paramName, configuration.getString("gateway.asterisk.settings." + paramName));
            } catch (NoSuchElementException ex) {
                throw new ValidationException("gateway.asterisk.settings." + paramName, "Key does not exist.");
            }
        }

        int amiPort;
        try {
            amiPort = Integer.parseInt(values.get("ami_port"));
        } catch (NumberFormatException ex) {
            throw new ValidationException("gateway.asterisk.settings.ami_port", "Value cannot be converted to Integer: '" + configuration.getString("gateway.asterisk.settings.ami_port") + "'");
        }

        ManagerConnectionFactory factory = new ManagerConnectionFactory(values.get("ami_host"), amiPort, values.get("ami_user"), values.get("ami_password"));
        ManagerConnection connection = factory.createManagerConnection();
        try {
            connection.login();
        } catch (IOException e) {
            throw new ValidationException("gateway.asterisk.settings", "Asterisk Manager Interface is unreachable: " +
                    ((e.getMessage() != null && !e.getMessage().equalsIgnoreCase("")) ?
                            e.getMessage() : "(no info)"));
        } catch (AuthenticationFailedException e) {
            throw new ValidationException("gateway.asterisk.settings", "Asterisk Manager Interface credentials are incorrect.");
        } catch (TimeoutException e) {
            throw new ValidationException("gateway.asterisk.settings", "Login timed out.");
        }
        connection.logoff();
    }

    /**
     * Validates the <code>ContactServer</code> configuration, which means the <code>www-home</code>, the port to use and that the default contact list is defined and usable.
     *
     * @throws ValidationException if a configuration error is detected
     */
    private void validateContactsServer() throws ValidationException {
        String home;
        try {
            //noinspection UnusedAssignment
            home = configuration.getString("contacts.home");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("contacts.home", "Key does not exist.");
        }

        int port;
        try {
            //noinspection UnusedAssignment
            port = configuration.getInt("contacts.port");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("contacts.port", "Key does not exist.");
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
            throw new ValidationException("contacts.lists.default", "Key does not exist.");
        } catch (IOException e) {
            throw new ValidationException("contacts.lists.default", "File cannot be read or written.");
        }
    }

    /**
     * Returns all the configuration entries beginning with a prefix
     *
     * @param prefix the prefix to use
     * @return a list of <code>String</code>
     */
    @SuppressWarnings("unchecked")
    private List<String> getEntries(String prefix) {
        List<String> result = new ArrayList<>();
        for (Iterator<String> it = configuration.getKeys(prefix); it.hasNext(); )
            result.add(it.next());

        return result;
    }

    /**
     * Finds the number of occurrences of a character in a <code>String</code>
     *
     * @param s the <code>String</code> to search
     * @param c the character to count
     * @return the number of occurrences of <code>c</code> in <code>s</code>
     */
    private int occurrences(String s, char c) {
        int count = 0;
        for (char c2 : s.toCharArray())
            if (c2 == c)
                count++;
        return count;
    }

    /**
     * Finds the top level entries (which means the direct children of the prefix) in the configuration
     *
     * @param prefix the prefix to find entries for
     * @return the direct children of the node corresponding to <code>prefix</code>
     */
    private Set<String> getTopLevelEntries(String prefix) {
        List<String> all = getEntries(prefix);
        Set<String> result = new HashSet<>();
        for (String entry : all) {
            String[] split = entry.split("\\.");
            result.add(split[occurrences(prefix, '.') + 1]);
        }
        return result;
    }

    /**
     * Validates the sounds configuration. <br />
     * This method cannot cause a <code>ValidationException</code> but only warn if a gateway has incomplete mappings.
     */
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

    /**
     * Describes the action to take if an invalid configuration entry is detected.
     */
    public enum OnError {
        /**
         * Exit EarlyWarning (using <code>System.exit</code>
         */
        Exit,
        /**
         * Display a warning in the log.
         */
        Warn
    }

    /**
     * The Exception used to express configuration mistakes.
     * It has a <code>parameter</code> field that corresponds to the field whose validation caused the error and a <code>problem</code> field that summarizes what the problem was.
     */
    class ValidationException extends Throwable {
        /**
         * The configuration entry that caused the problem
         */
        final String parameter;

        /**
         * A summary of the problem
         */
        final String problem;

        /**
         * Constructor
         *
         * @param parameter the parameter for which the Exception is raised
         * @param problem   the cause of the exception
         */
        ValidationException(String parameter, String problem) {
            this.parameter = parameter;
            this.problem = problem;
        }

        @Override
        public String toString() {
            return this.getClass().getName() + " on parameter '" + parameter + "': " + problem;
        }
    }

}
