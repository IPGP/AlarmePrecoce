package fr.ipgp.earlywarning.asterisk;

/**
 * A Listener used to listen to the onConnected call event (which meaning is implementation-dependant: first packet received, first DTMF digit read...)
 *
 * @author Thomas Kowalski
 */
interface OnConnectedListener {
    void onConnected();
}
