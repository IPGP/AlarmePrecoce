package fr.ipgp.earlywarning.asterisk;

/**
 * A listener to listen to hang up events that can be emitted by the {@link AlertCallScript} or the {@link CallOriginator}
 *
 * @author Thomas Kowalski
 */
interface OnHangupListener {
    void onHangup();
}
