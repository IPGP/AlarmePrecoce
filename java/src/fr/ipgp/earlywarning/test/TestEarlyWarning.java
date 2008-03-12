/**
 * Created Mar 12, 2008 2:42:18 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.test;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import fr.ipgp.earlywarning.EarlyWarning;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Patrice Boissier
 *
 */
public class TestEarlyWarning {
	
	@Before
	public void setUp() throws UnknownHostException, SocketException {
		
	}
	
	@After
	public void tearDown() {

	}
	
	@Test
	public void testReadConf() throws IOException {
		EarlyWarning earlyWarning = new EarlyWarning();
	}
	
	public static junit.framework.Test suite() {
        return new junit.framework.JUnit4TestAdapter(TestTrigger.class);
    }
}
