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

        File configurationFile = searchForFile(root,"earlywarning.xml");
        File workingDirFile = configurationFile.getParentFile().getParentFile().getCanonicalFile();
        String workingDir = workingDirFile.getCanonicalPath();
        System.setProperty("user.dir", workingDir);

        System.out.println("Working directory used for this test: '" + System.getProperty("user.dir") + "'");

        configurationFile = searchForFile(new File(workingDir), "earlywarning.xml");

        EarlyWarning.configuration = new XMLConfiguration(configurationFile.getCanonicalPath());
        EarlyWarning.configuration.setThrowExceptionOnMissing(true);

        return workingDir;
    }
}
