package fr.ipgp.earlywarning.asterisk;

import org.asteriskjava.fastagi.*;

class LocalAgiServer {
    /**
     * The base AgiServer to run
     */
    @SuppressWarnings("FieldCanBeLocal")
    private final DefaultAgiServer server;

    /**
     * The (unique) AGI script available
     */
    private final AlertCallScript script;

    /**
     * Valued constructor.
     *
     * @param script an instance of AlertCallScript to use to serve AGI service requests.
     */
    private LocalAgiServer(AlertCallScript script) {
        this.script = script;

        server = new DefaultAgiServer(new AlertCallMappingStrategy());

        AgiServerThread thread = new AgiServerThread(server);
        thread.startup();
    }

    /**
     * Default constructor: constructs an AlertCallScript and calls the valued constructor.
     */
    public LocalAgiServer() {
        this(new AlertCallScript());
    }

    /**
     * A hardcoded MappingStragy.
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
