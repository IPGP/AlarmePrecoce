package fr.ipgp.earlywarning.utilities;

import org.apache.commons.configuration.XMLConfiguration;

/**
 * Configuration validator.
 * Reads a given <code>XMLConfiguration</code> and verifies everything it has to contain exists and is correct.
 * All the tests that can be made are made (file existence for adequate fields, etc.) and in case of doubt, warnings are emitted.
 * @author Thomas Kowalski
 */
public class ConfigurationValidator {
    XMLConfiguration configuration;

    public ConfigurationValidator(XMLConfiguration configuration)
    {
        this.configuration = configuration;
    }

    public void validate()
    {
        // TODO: implement ConfigurationValidator::validate
    }
}
