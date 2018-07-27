package failover;

import fr.ipgp.earlywarning.heartbeat.AliveRequester;
import fr.ipgp.earlywarning.heartbeat.AliveState;
import fr.ipgp.earlywarning.heartbeat.HeartbeatServerThread;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.Random;

import static java.lang.Math.abs;

public class TestAliveRequests {
    private static int port;
    private static HeartbeatServerThread server;

    @BeforeClass
    public static void setUp() throws IOException {
        Random r = new Random();
        port = abs(r.nextInt()) % 50000 + 1000;
        server = HeartbeatServerThread.getInstance(port);
        server.start();
    }

    @AfterClass
    public static void tearDown() {

    }

    @Test
    public void testNotRunning() {
        server.disable();

        AliveRequester requester = AliveRequester.getInstance("localhost", port);
        AliveState state = requester.getState();

        Assert.assertEquals(state, AliveState.CantConnect);
    }

    @Test
    public void testAlive() {
        server.enable();

        AliveRequester requester = AliveRequester.getInstance("localhost", port);
        AliveState state = requester.getState();

        System.out.println(AliveState.values()[state.ordinal()]);

        Assert.assertEquals(state, AliveState.Alive);
    }

    @Test
    public void testUnreachable() {
        AliveRequester requester = AliveRequester.getInstance("1.1.1.1", port);
        AliveState state = requester.getState();

        Assert.assertEquals(state, AliveState.CantConnect);
    }
}
