package fr.ipgp.earlywarning.utilities;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.contacts.ContactList;
import fr.ipgp.earlywarning.contacts.ContactListBuilder;
import fr.ipgp.earlywarning.gateway.CharonGateway;
import fr.ipgp.earlywarning.heartbeat.AliveRequester;
import fr.ipgp.earlywarning.messages.NoSuchMessageException;
import fr.ipgp.earlywarning.messages.WarningMessageMapper;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Configuration validator.
 * Reads a given {@link XMLConfiguration} and verifies everything it has to contain exists and is correct.
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
     * A reference to the main {@link XMLConfiguration}
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
     * Returns all the configuration entries beginning with a prefix
     *
     * @param prefix the prefix to use
     * @return a {@link List} of {@link String}
     */
    public static List<String> getEntries(XMLConfiguration configuration, String prefix) {
        List<String> result = new ArrayList<>();
        for (Iterator<String> it = configuration.getKeys(prefix); it.hasNext(); )
            result.add(it.next());

        return result;
    }

    /**
     * Finds the number of occurrences of a character in a {@link String}
     *
     * @param s the {@link String} to search
     * @param c the character to count
     * @return the number of occurrences of <code>c</code> in <code>s</code>
     */
    @SuppressWarnings("SameParameterValue")
    public static int occurrences(String s, char c) {
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
    public static Set<String> getTopLevelEntries(XMLConfiguration configuration, String prefix) {
        List<String> all = getEntries(configuration, prefix);
        Set<String> result = new HashSet<>();
        for (String entry : all) {
            String[] split = entry.split("\\.");
            result.add(split[occurrences(prefix, '.') + 1]);
        }
        return result;
    }

    public static List<Map<String, String>> getItems(String at) {
        List<Map<String, String>> items = new ArrayList<>();

        List<HierarchicalConfiguration> entries = EarlyWarning.configuration.configurationsAt(at);
        for (HierarchicalConfiguration entry : entries) {
            Map<String, String> m = new HashMap<>();

            for (Iterator<String> it = entry.getKeys(); it.hasNext(); ) {
                String key = it.next();
                m.put(key, entry.getString(key));
            }

            items.add(m);
        }

        return items;
    }

    /**
     * Validates the whole {@link XMLConfiguration}, taking measures depending on the <code>onError</code> behaviour.
     */
    public void validate() {
        EarlyWarning.appLogger.info("------------------------------------");
        EarlyWarning.appLogger.info("Verifying configuration.");
        try {
            validateContactsServer();
            validateSounds();
            validateGatewaySettings();
            validateMail();
            validateTriggers();
            validateFailover();
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

    @SuppressWarnings("UnusedAssignment")
    private void validateTriggers() throws ValidationException {
        boolean triggersForErrors;
        try {
            triggersForErrors = configuration.getBoolean("triggers.create_trigger_on_errors");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("triggers.create_trigger_on_errors", "Key not found.");
        } catch (ConversionException ex) {
            throw new ValidationException("triggers.create_trigger_on_errors", "Value '" + configuration.getString("triggers.create_trigger_on_errors") + "' cannot be converted to a boolean.");
        }

        int defaultPriority;
        try {
            defaultPriority = configuration.getInt("triggers.defaults.priority");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("triggers.defaults.priority", "Key not found.");
        } catch (ConversionException ex) {
            throw new ValidationException("triggers.defaults.priority", "Value '" + configuration.getString("triggers.defaults.priority") + "' cannot be converted to an integer.");
        }

        if (defaultPriority < 1 || defaultPriority > 10)
            throw new ValidationException("triggers.defaults.priority", "Priority should be between 1 and 10");

        int defaultConfirmCode;
        String _defaultConfirmCode = null;
        try {
            _defaultConfirmCode = configuration.getString("triggers.defaults.confirm_code");
            defaultConfirmCode = configuration.getInt("triggers.defaults.confirm_code");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("triggers.defaults.confirm_code", "Key not found.");
        } catch (ConversionException ex) {
            throw new ValidationException("triggers.defaults.confirm_code", "Value '" + _defaultConfirmCode + "' cannot be converted to an integer.");
        }

        if (_defaultConfirmCode.length() > 4)
            EarlyWarning.appLogger.warn("Default confirm code '" + _defaultConfirmCode + "' is longer thaan four digits.");

        boolean defaultRepeat;
        try {
            defaultRepeat = configuration.getBoolean("triggers.defaults.repeat");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("triggers.defaults.repeat", "Key not found.");
        } catch (ConversionException ex) {
            throw new ValidationException("triggers.defaults.repeat", "Value '" + configuration.getString("triggers.defaults.repeat") + "' cannot be converted to a boolean.");
        }
    }

    /**
     * Validates the e-mailing settings.
     *
     * @throws ValidationException if a configuration error is detected
     */
    @SuppressWarnings("UnusedAssignment")
    private void validateMail() throws ValidationException {
        String _useMail = configuration.getString("mail.use_mail");
        boolean useMail;
        if (_useMail.equalsIgnoreCase("true"))
            useMail = true;
        else if (_useMail.equalsIgnoreCase("false"))
            useMail = false;
        else {
            useMail = false;
            EarlyWarning.appLogger.warn("E-mail status (active / inactive) is not a boolean: '" + _useMail + "', defaulting to false.");
        }

        // Do not validate e-mail settings if they are not in used
        if (!useMail)
            return;

        boolean useSsl;
        try {
            useSsl = configuration.getBoolean("mail.smtp.use_ssl");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("mail.smtp.use_ssl", "Key does not exist.");
        } catch (ConversionException ex) {
            throw new ValidationException("mail.smtp.use_ssl", "Value cannot be converted to a boolean.");
        }

        String[] fields = new String[]{"host", "username", "password"};

        for (String field : fields) {
            String value;
            try {
                value = configuration.getString("mail.smtp." + field);
            } catch (NoSuchElementException ex) {
                throw new ValidationException("mail.smtp." + field, "Key does not exist.");
            }
        }

        String host = configuration.getString("mail.smtp.host");
        String username = configuration.getString("mail.smtp.username");
        String password = configuration.getString("mail.smtp.password");

        String _from = "";
        try {
            _from = configuration.getString("mail.smtp.from");
            InternetAddress from = new InternetAddress(_from);
            from.validate();
        } catch (NoSuchElementException ex) {
            throw new ValidationException("mail.smtp.from", "Key does not exist.");
        } catch (AddressException e) {
            throw new ValidationException("mail.smtp.from", "Value '" + _from + "' cannot be converted to an e-mail address.");
        }

        int port;
        try {
            port = configuration.getInt("mail.smtp.port");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("mail.smtp.port", "Key does not exist.");
        } catch (ConversionException ex) {
            throw new ValidationException("mail.smtp.port", "Value cannot be converted to an Integer.");
        }

        List<HierarchicalConfiguration> emailEntries = EarlyWarning.configuration.configurationsAt("mail.mailinglist.contact");

        if (emailEntries.size() == 0)
            throw new ValidationException("mail.smtp.mailinglist", "E-mailing is enabled but the mailing list is empty.");

        for (HierarchicalConfiguration sub : emailEntries) {
            String mail = sub.getString("email");
            try {
                InternetAddress internetAddress = new InternetAddress(mail);
                internetAddress.validate();
            } catch (AddressException ex) {
                throw new ValidationException("mail.smtp.mailinglist", "Value '" + mail + "' cannot be converted to an e-mail address.");
            }
        }

        try {
            Mailer.getInstance(host, _from, username, password, String.valueOf(port), useSsl).testAuthentication();
        } catch (MessagingException e) {
            e.printStackTrace();
            throw new ValidationException("mail.smtp", "SMTP credentials are incorrect or another SMTP-related authentication exception occurred. Please check stacktrace for details..");
        }
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

        if (!activeGateway.equalsIgnoreCase("charon")) {
            // If the active Gateway is not the Charon module, verify whether or not the failover system is enabled.
            boolean failoverEnabled;
            try {
                failoverEnabled = configuration.getBoolean("gateway.failover_enabled");
            } catch (NoSuchElementException ex) {
                throw new ValidationException("gateway.failover_enabled", "Key does not exist.");
            } catch (ConversionException ex) {
                throw new ValidationException("gateway.failover_enabled", "Value '" + configuration.getString("gateway.failover_enabled") + "' cannot be converted to a boolean.");
            }

            if (!failoverEnabled)
                EarlyWarning.appLogger.warn("Failover system is currently DISABLED.");
            else {
                try {
                    WarningMessageMapper.testDefaultMessage("charon");
                } catch (NoSuchMessageException e) {
                    throw new ValidationException("sounds.default", "No default LED given for failover system 'charon'");
                }
            }
        }

        /* All the gateways that can be used */
        Set<String> availableGateways = new HashSet<>();
        availableGateways.add("asterisk");
        availableGateways.add("charon");

        /* Verify that the active Gateway is a known one */
        if (!availableGateways.contains(activeGateway.toLowerCase()))
            throw new ValidationException("gateway.active", "Unknown active Gateway");

        /* Select what Gateway configuration should be validated */
        if (activeGateway.equalsIgnoreCase("asterisk"))
            validateAsteriskSettings();

        /* Verify that the default sound is configured for the selected gateway */
        try {
            WarningMessageMapper.testDefaultMessage(activeGateway);
            WarningMessageMapper.getInstance(activeGateway);
            EarlyWarning.appLogger.debug("Default sound is configured.");
        } catch (NoSuchMessageException e) {
            throw new ValidationException("gateway.active", "No default sound configured for gateway '" + activeGateway + "'");
        }

        /* Always validate Charon settings, since it is used for the failover system. */
        validateCharonSettings();
    }

    private void validateCharonSettings() throws ValidationException {
        int port;
        try {
            //noinspection UnusedAssignment
            port = configuration.getInt("gateway.charon.port");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("gateway.charon.port", "Key does not exist.");
        } catch (ConversionException ex) {
            throw new ValidationException("gateway.charon.port", "Value cannot be converted to Integer: '" + configuration.getString("gateway.charon.port") + "'");
        }

        String host;
        try {
            //noinspection UnusedAssignment
            host = configuration.getString("gateway.charon.host");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("gateway.charon.host", "Key does not exist.");
        }

        int timeout;
        try {
            //noinspection UnusedAssignment
            timeout = configuration.getInt("gateway.charon.timeout");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("gateway.charon.timeout", "Key does not exist.");
        } catch (ConversionException ex) {
            throw new ValidationException("gateway.charon.timeout", "Value cannot be converted to Integer: '" + configuration.getString("gateway.charon.timeout") + "'");
        }

        CharonGateway gateway = CharonGateway.getInstance(host, port, timeout);
        if (!gateway.checkConnected())
            throw new ValidationException("gateway.charon", "Charon I module is unreachable at with address " + host + ":" + String.valueOf(port));
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
            throw new ValidationException("gateway.asterisk.retries", "Value cannot be converted to Integer: '" + configuration.getString("gateway.asterisk.retries") + "'");
        }

        int ringTimeout;
        try {
            //noinspection UnusedAssignment
            ringTimeout = configuration.getInt("gateway.asterisk.ring_timeout");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("gateway.asterisk.ring_timeout", "Key does not exist.");
        } catch (ConversionException ex) {
            throw new ValidationException("gateway.asterisk.ring_timeout", "Value cannot be converted to Integer: '" + configuration.getString("gateway.asterisk.ring_timeout") + "'");
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
        } catch (IOException ex) {
            throw new ValidationException("gateway.asterisk.settings", "Asterisk Manager Interface is unreachable: " +
                    ((ex.getMessage() != null && !ex.getMessage().equalsIgnoreCase("")) ?
                            ex.getMessage() : "(no info)"));
        } catch (AuthenticationFailedException ex) {
            throw new ValidationException("gateway.asterisk.settings", "Asterisk Manager Interface credentials are incorrect.");
        } catch (TimeoutException ex) {
            throw new ValidationException("gateway.asterisk.settings", "Login timed out.");
        }
        connection.logoff();
    }

    /**
     * Validates the {@link fr.ipgp.earlywarning.contacts.ContactListManagerServer}, which means the <code>www-home</code> and its contents, the port to use and that the default contact list is defined and usable.
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


        try {
            File root = new File(home).getAbsoluteFile();
            if (!root.isDirectory())
                throw new ValidationException("contacts.home", "ContactServer root directory does not exist or is not a directory ('" + root.getCanonicalPath() + "'");

            String[] requiredFiles = new String[]{"index.html", "Sortable.min.js"};

            for (String file : requiredFiles) {
                File f = new File(root.getCanonicalPath() + "/" + file);
                if (!f.isFile())
                    throw new ValidationException("contacts.home", "File '" + file + "' does not exist in ContactServer root ('" + root.getCanonicalPath() + "'");
            }
        } catch (IOException e) {
            throw new ValidationException("contacts.home", "ContactServer root directory is not accessible.");
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

        String defaultPath = null;
        try {
            List<Map<String, String>> contactLists = getItems("contacts.lists.list");
            for (Map<String, String> contactList : contactLists) {
                if (contactList.get("id").equalsIgnoreCase("default")) {
                    defaultPath = contactList.get("path");
                } else {
                    ContactList list = ContactListBuilder.build(contactList.get("path"));
                    if (list.getEnabledContacts().isEmpty())
                        EarlyWarning.appLogger.warn("Your list '" + contactList.get("id") + "' has no enabled contact.");
                }
            }

            if (defaultPath == null)
                throw new ValidationException("contacts.lists.list.default", "No default contact list configured.");

            ContactList list = ContactListBuilder.build(defaultPath);
            if (list.getEnabledContacts().isEmpty())
                EarlyWarning.appLogger.warn("Your default contact list ('" + defaultPath + "') currently does not have anyone on the call list. You should fix this or no call will be emitted.");

        } catch (NoSuchElementException ex) {
            throw new ValidationException("contacts.lists.list", "No call list configured.");
        } catch (ContactListBuilder.UnimplementedContactListTypeException ex) {
            throw new ValidationException("contacts.lists.list.default", "Unsupported file format for default list.");
        } catch (IOException ex) {
            throw new ValidationException("contacts.lists.list.default", "File cannot be read or written.");
        }
    }

    private List<String> getEntries(String prefix) {
        return getEntries(configuration, prefix);
    }

    private Set<String> getTopLevelEntries(String prefix) {
        return getTopLevelEntries(configuration, prefix);
    }

    /**
     * Validates the sounds configuration. <br />
     * This method cannot cause a {@link ValidationException} but only warn if a gateway has incomplete mappings.
     */
    private void validateSounds() {
        Set<String> gateways = new HashSet<>();
        Set<String> soundNames = new HashSet<>();
        Map<String, Set<String>> availableGateways = new HashMap<>();
        List<Map<String, String>> sounds = getItems("sounds.sound");

        for (Map<String, String> soundEntry : sounds) {
            String sound = soundEntry.get("id");
            soundNames.add(sound);
            availableGateways.put(sound, new HashSet<String>());

            for (String gateway : soundEntry.keySet()) {
                gateways.add(gateway);
                availableGateways.get(sound).add(gateway);
            }
        }

        for (String gateway : gateways)
            for (String sound : soundNames)
                if (!availableGateways.get(sound).contains(gateway))
                    EarlyWarning.appLogger.warn("Sound '" + sound + "' has no mapping for gateway '" + gateway + "'");
    }

    public void validateFailover() throws ValidationException {
        boolean isFailover;
        try {
            isFailover = configuration.getBoolean("failover.is_failover");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("failover.is_failover", "Key does not exist.");
        } catch (ConversionException ex) {
            throw new ValidationException("failover.is_failover", "Value '" + configuration.getString("failover.is_failover") + "' cannot be converted to a boolean.");
        }

        int port;
        try {
            port = configuration.getInt("failover.heartbeat_port");
        } catch (NoSuchElementException ex) {
            throw new ValidationException("failover.heartbeat_port", "Key does not exist.");
        } catch (ConversionException ex) {
            throw new ValidationException("failover.heartbeat_port", "Value '" + configuration.getString("failover.heartbeat_port") + "' cannot be converted to an integer.");
        }

        if (isFailover) {
            String host;
            try {
                host = configuration.getString("failover.main");
            } catch (NoSuchElementException ex) {
                throw new ValidationException("failover.main", "Key does not exist.");
            }

            AliveRequester requester = AliveRequester.getInstance(host, port);
            boolean currentlyRunning = requester.getOnline();
            if (!currentlyRunning)
                EarlyWarning.appLogger.warn("Current instance is configured to be a failover but the main instance that should be running at " + host + " IS NOT RESPONDING. Is the configuration wrong or is there an error on the other side?");
        }
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
     *
     * @author Thomas Kowalski
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
