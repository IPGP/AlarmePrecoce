package messages;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.messages.NoSuchMessageException;
import fr.ipgp.earlywarning.messages.WarningMessageMapper;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static commons.TestCommons.setUpEnvironment;
import static fr.ipgp.earlywarning.utilities.FileSearch.searchForFile;

/**
 * Tests for the {@link WarningMessageMapper}
 *
 * @author Thomas Kowalski
 */
public class TestMessageMapper {
    private static final String DEFAULT_SOUND_NAME = "hello-world";
    private static Random rand;

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestMessageMapper.class);
    }

    @BeforeClass
    public static void setUp() throws IOException, ConfigurationException {
        System.out.println("MessageMapper tests.");

        String workingDir = setUpEnvironment();

        File configurationFile = searchForFile(new File(workingDir), "earlywarning_test_messagemapper.xml");
        EarlyWarning.configuration = new XMLConfiguration(configurationFile.getCanonicalPath());
        EarlyWarning.configuration.setThrowExceptionOnMissing(true);

        rand = new Random();
    }

    @AfterClass
    public static void tearDown() {

    }

    private String randomGatewayName() {
        return "Gateway<" + rand.nextInt() + ">";
    }

    @Test(expected = NoSuchMessageException.class)
    public void testRandomNonExistentGateway() throws NoSuchMessageException {
        System.out.println("Testing Non-Existent Gateway");
        WarningMessageMapper.testDefaultMessage(randomGatewayName());
        WarningMessageMapper mapper = WarningMessageMapper.getInstance(randomGatewayName());
    }

    @Test(expected = NoSuchMessageException.class)
    public void testUnconfiguredGateway() throws NoSuchMessageException {
        System.out.println("Testing Unconfigured Gateway");
        WarningMessageMapper.testDefaultMessage("unconfigured");
    }

    @Test
    public void testCorrectGateway() throws NoSuchMessageException {
        System.out.println("Testing Configured Gateway");
        WarningMessageMapper.testDefaultMessage("configured");
        WarningMessageMapper mapper = WarningMessageMapper.getInstance("configured");

        mapper.getName("welcome");
        mapper.getNameIgnoreCase("wElCoME");

        Assert.assertEquals(mapper.getNameOrDefault("non-existent-sound"), DEFAULT_SOUND_NAME);
        Assert.assertEquals(mapper.getNameOrDefaultIgnoreCase("non-existent-souNd"), DEFAULT_SOUND_NAME);
    }
}
