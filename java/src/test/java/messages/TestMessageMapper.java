package messages;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.messages.NoSuchMessageException;
import fr.ipgp.earlywarning.messages.WarningMessageMapper;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static commons.TestCommons.setUpEnvironment;
import static fr.ipgp.earlywarning.utilities.FileSearch.searchForFile;

public class TestMessageMapper {
    private static Random rand;

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestMessageMapper.class);
    }

    @BeforeClass
    public static void setUp() throws IOException, ConfigurationException {
        String workingDir = setUpEnvironment();

        File configurationFile = searchForFile(new File(workingDir), "earlywarning_test_messagemapper.xml");
        EarlyWarning.configuration = new XMLConfiguration(configurationFile.getCanonicalPath());
        EarlyWarning.configuration.setThrowExceptionOnMissing(true);

        rand = new Random();
    }

    @AfterClass
    public static void tearDown()
    {

    }

    private String randomGatewayName()
    {
        return "Gateway<" + rand.nextInt() + ">";
    }

    @Test(expected = NoSuchMessageException.class)
    public void testRandomNonExistentGateway() throws NoSuchMessageException {
        WarningMessageMapper.testDefaultMessage(randomGatewayName());
        WarningMessageMapper mapper = WarningMessageMapper.getInstance(randomGatewayName());
    }

    @Test(expected = NoSuchMessageException.class)
    public void testUnconfiguredGateway() throws NoSuchMessageException {
        WarningMessageMapper.testDefaultMessage("unconfigured");
    }

    @Test
    public void testCorrectGateway() throws NoSuchMessageException {
        WarningMessageMapper.testDefaultMessage("configured");
        WarningMessageMapper mapper = WarningMessageMapper.getInstance("configured");

        mapper.getName("welcome");
    }
}
