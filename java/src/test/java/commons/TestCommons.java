package commons;

import fr.ipgp.earlywarning.EarlyWarning;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.File;
import java.io.IOException;

import static fr.ipgp.earlywarning.utilities.FileSearch.searchForFile;

/**
 * General utilities for the tests.
 *
 * @author Thomas Kowalski
 */
public class TestCommons {
    /**
     * Finds the right working directory (the one containing <code>resources</code>) and loads the default configuration
     * @return the working directory
     * @throws IOException if the default configuration file cannot be found
     * @throws ConfigurationException if the configuration is invalid
     */
    public static String setUpEnvironment() throws IOException, ConfigurationException {
        String root = "../";

        File configurationFile = searchForFile(root,"earlywarning.xml");
        File workingDirFile = configurationFile.getParentFile().getParentFile().getCanonicalFile();
        String workingDir = workingDirFile.getCanonicalPath();
        System.setProperty("user.dir", workingDir);

        System.out.println("Setting working directory to: '" + System.getProperty("user.dir") + "'");

        configurationFile = searchForFile(new File(workingDir), "earlywarning.xml");

        EarlyWarning.configuration = new XMLConfiguration(configurationFile.getCanonicalPath());
        EarlyWarning.configuration.setThrowExceptionOnMissing(true);

        return workingDir;
    }
}
