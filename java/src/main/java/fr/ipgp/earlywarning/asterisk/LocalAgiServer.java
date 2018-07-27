package fr.ipgp.earlywarning.asterisk;

import org.asteriskjava.fastagi.*;

/**
 * The server that serves our AGI script.<br />
 * It extends Asterisk-Java's {@link DefaultAgiServer} and runs it in a separate {@link AgiServerThread}.
 * In order to avoid using a <code>fastagi-mapping.properties</code> file, that is the source of frequent issues,
 * we use an {@link AlertCallMappingStrategy}, a hardcoded {@link MappingStrategy} that always returns a local instance of the {@link AlertCallScript}.
 *
 * @author Thomas Kowalski
 */
public class LocalAgiServer {
    /**
     * The base {@link AgiServer} to run
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final DefaultAgiServer server;

    /**
     * The (unique) {@link AgiScript} available
     */
    private final AlertCallScript script;

    /**
     * Valued constructor.
     *
     * @param script an instance of {@link AlertCallScript} to use to serve AGI service requests.
     */
    private LocalAgiServer(AlertCallScript script) {
        this.script = script;

        server = new DefaultAgiServer(new AlertCallMappingStrategy());

        AgiServerThread thread = new AgiServerThread(server);
        thread.startup();
    }

    /**
     * Default constructor: constructs an {@link AlertCallScript} and passes it to the valued constructor.
     */
    public LocalAgiServer() {
        this(new AlertCallScript());
    }

    /**
     * A hardcoded {@link MappingStrategy}<br />
     * The main advantage is that we don't rely on an external mapping files (since it often causes problems).
     */
    class AlertCallMappingStrategy implements MappingStrategy {
        @Override
        public AgiScript determineScript(AgiRequest agiRequest, AgiChannel agiChannel) {
            if (agiRequest.getRequestURL().contains("alertcall"))
                return script;
            else {
                System.err.println("No corresponding file for URL " + agiRequest.getRequestURL());
                return null;
            }
        }
    }
}
