package fr.ipgp .earlywarning.asterisk;

/**
 * // TODO: fix doc
 * A Listener used to listen to call events: onConnected (the user has acknowledged the Welcome message) and onDisconnected (the user hung up or the AGI script hung up)
 *
 * @author Thomas Kowalski
 */
interface OnConnectedListener {
    void onConnected();
}
