/*

 */
package fr.ipgp.earlywarning.gateway;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author patriceboissier
 */
public class TestMockGateway {

    public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestMockGateway.class);
    }

    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }

    @Test
    public void testCreateMockGateway() {
        MockGateway mockGateway = MockGateway.getInstance();
        System.out.println(mockGateway.callStatus("bidon"));
        System.out.println(mockGateway.callStatus("bidon"));
        System.out.println(mockGateway.callStatus("bidon"));
    }
}
