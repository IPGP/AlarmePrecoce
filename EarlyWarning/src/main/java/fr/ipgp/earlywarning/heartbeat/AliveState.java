package fr.ipgp.earlywarning.heartbeat;

/**
 * An enumeration representing the main instance state, as determined by the {@link AliveRequester}.
 *
 * @author Thomas Kowalski
 */
public enum AliveState {
    /**
     * The server received the {@link AliveRequest} and answered it with an {@link AliveResponse}.
     */
    Alive,
    /**
     * The server does not accept TCP connections.
     */
    CantConnect,
    /**
     * The server accepted the connection, but binding the IO streams failed.
     */
    CantWrite,
    /**
     * The server accepted the connections, received the {@link AliveRequest} but did not answer.
     */
    NoResponse,
    /**
     * Another (generic) error.
     */
    Error
}
