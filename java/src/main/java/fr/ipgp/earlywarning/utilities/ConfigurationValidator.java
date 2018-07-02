package fr.ipgp.earlywarning.utilities;

import fr.ipgp.earlywarning.EarlyWarning;
import org.apache.commons.configuration.ConversionException;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.NoSuchElementException;

import static fr.ipgp.earlywarning.utilities.ConfigurationValidator.OnError.Exit;

/**
 * Configuration validator.
 * Reads a given <code>XMLConfiguration</code> and verifies everything it has to contain exists and is correct.
 * All the tests that can be made are made (file existence for adequate fields, etc.) and in case of doubt, warnings are emitted.
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

    public ConfigurationValidator(XMLConfiguration configuration, OnError behaviour)
    {
        onError = behaviour;
        this.configuration = configuration;
    }

    public ConfigurationValidator(XMLConfiguration configuration)
    {
        this(configuration, defaultBehaviour);
    }

    public void validate()
    {
        return;
//        validateContactsServer();
        // TODO: implement ConfigurationValidator::validate
    }

    private void validateContactsServer() {
        String home;
        int port;

        try {
            home = configuration.getString("contacts.home");
            port = configuration.getInt("contacts.port");

            File homeDir = new File(home);
            if (!homeDir.isDirectory())
                throw new FileNotFoundException("The ContactServer home doesn't exist in '" + home + "'");

            boolean foundIndex = false;
            boolean foundJS = false;
            for (File f : homeDir.listFiles()) {
                if (f.getName().equalsIgnoreCase("index.html"))
                    foundIndex = true;
                else if (f.getName().equals("sortable.js"))
                    foundJS = true;
            }
            if (!foundIndex || !foundJS)
                throw new FileNotFoundException("Couldn't find index.html or sortable.js in ContactServer home ('" + home + "')");
        }
        catch (FileNotFoundException | NoSuchElementException ex)
        {
            if (onError == Exit)
            {
                EarlyWarning.appLogger.fatal("Configuration is invalid.");
                EarlyWarning.appLogger.fatal(ex.getMessage());
                System.exit(-1);
            }
            else
            {
                EarlyWarning.appLogger.warn("Configuration is invalid.");
                EarlyWarning.appLogger.warn(ex.getMessage());
            }
        }
        catch (ConversionException ex)
        {
            if (onError == Exit)
            {
                EarlyWarning.appLogger.warn("Configuration is invalid.");
                EarlyWarning.appLogger.fatal("The ContactServer port is invalid :" + EarlyWarning.configuration.getString("contacts.port"));
                System.exit(-1);
            }
            else
            {
                EarlyWarning.appLogger.warn("Configuration is invalid.");
                EarlyWarning.appLogger.warn("The ContactServer port is invalid :" + EarlyWarning.configuration.getString("contacts.port"));
            }
        }
    }
}
