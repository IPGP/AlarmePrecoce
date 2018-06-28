package fr.ipgp.earlywarning.asterisk;

/**
 * A listener to listen to hang up events (on the AGI script side and on callee's side)
 *
 * @author Thomas Kowalski
 */
interface OnHangupListener {
    void onHangup();
}
