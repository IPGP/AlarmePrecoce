package commons;

import fr.ipgp.earlywarning.EarlyWarning;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;

import java.io.File;
import java.io.IOException;

import static fr.ipgp.earlywarning.utilities.FileSearch.searchForFile;

public class TestCommons {
    public static String setUpEnvironment() throws IOException, ConfigurationException {
        String root = "../";

        String workingDir = searchForFile(root,"earlywarning.xml").getParentFile().getCanonicalPath();
        System.setProperty("user.dir", workingDir);

        File configurationFile = searchForFile(new File(workingDir), "earlywarning.xml");

        EarlyWarning.configuration = new XMLConfiguration(configurationFile.getCanonicalPath());
        EarlyWarning.configuration.setThrowExceptionOnMissing(true);

        return workingDir;
    }
}
