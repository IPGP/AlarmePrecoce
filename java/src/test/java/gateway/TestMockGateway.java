package gateway;

import fr.ipgp.earlywarning.gateway.MockGateway;
import org.apache.commons.configuration.ConfigurationException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static commons.TestCommons.setUpEnvironment;

/**
 * @author patriceboissier
 */
public class TestMockGateway {

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestMockGateway.class);
    }

    @Before
    public void setUp() throws IOException, ConfigurationException {
        setUpEnvironment();
    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreateMockGateway() {
        MockGateway mockGateway = MockGateway.getInstance();
    }
}
