package fr.ipgp.earlywarning.asterisk;

/**
 * A listener for DTMF code received events.
 *
 * @author Thomas Kowalski
 */
interface OnCodeReceivedListener {
    CallOriginator.CallAction onCodeReceived(String code);
}
