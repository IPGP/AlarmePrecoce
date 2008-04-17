/**
 * 
 */
package fr.ipgp.earlywarning.gateway;

import org.junit.*;
import fr.ipgp.earlywarning.gateway.MockGateway;
/**
 * @author patriceboissier
 *
 */
public class TestMockGateway {
	
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
	
	public static junit.framework.Test suite() {
		return new junit.framework.JUnit4TestAdapter(TestMockGateway.class);
	}
}
