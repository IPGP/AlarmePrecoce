package gateway;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.asterisk.Tester;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static commons.TestCommons.setUpEnvironment;
import static fr.ipgp.earlywarning.utilities.FileSearch.searchForFile;

public class ValidationTestAsterisk {
    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(ValidationTestAsterisk.class);
    }

    @BeforeClass
    public static void setUp() throws IOException, ConfigurationException {
        String workingDir = setUpEnvironment();

        File configurationFile = searchForFile(new File(workingDir), "earlywarning.xml");

        EarlyWarning.configuration = new XMLConfiguration(configurationFile.getCanonicalPath());
        EarlyWarning.configuration.setThrowExceptionOnMissing(true);
    }

    @Test
    public void validationTest() throws Exception {
        List<String> callList = new ArrayList<>();
        callList.add("0692877305");
        String code = "1256";

        Tester.run(callList, code);
    }

}
