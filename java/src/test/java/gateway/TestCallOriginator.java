package gateway;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.asterisk.CallOriginator;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.TimeoutException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static commons.TestCommons.setUpEnvironment;
import static fr.ipgp.earlywarning.utilities.FileSearch.searchForFile;

/**
 * Tests for the {@link CallOriginator}
 *
 * @author Thomas Kowalski
 */
public class TestCallOriginator {
    private static String correctUsername, correctHost, correctPassword;
    private static int correctPort;

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestCallOriginator.class);
    }

    @BeforeClass
    public static void setUp() throws ConfigurationException, IOException {
        String workingDir = setUpEnvironment();
        File configurationFile = searchForFile(new File(workingDir), "earlywarning_test_calloriginator.xml");

        EarlyWarning.configuration = new XMLConfiguration(configurationFile.getCanonicalPath());
        EarlyWarning.configuration.setThrowExceptionOnMissing(true);

        correctHost = EarlyWarning.configuration.getString("gateway.asterisk.settings.ami_host");
        correctPort = EarlyWarning.configuration.getInt("gateway.asterisk.settings.ami_port");
        correctUsername = EarlyWarning.configuration.getString("gateway.asterisk.settings.ami_user");
        correctPassword = EarlyWarning.configuration.getString("gateway.asterisk.settings.ami_password");
    }

    @AfterClass
    public static void tearDown() {

    }

    @Test(expected = AuthenticationFailedException.class)
    public void testWrongCredentials() throws AuthenticationFailedException, IOException, TimeoutException {
        CallOriginator originator = new CallOriginator(correctHost, correctPort, "wrong", "user", "1234", "default");
        originator.testCredentials();
    }

    @Test(expected = IOException.class)
    public void testWrongInetAddress() throws AuthenticationFailedException, IOException, TimeoutException {
        CallOriginator originator = new CallOriginator("localhost", 5338, correctUsername, correctPassword, "1234", "default");
        originator.testCredentials();
    }
}
