package fr.ipgp.earlywarning.asterisk;

/**
 * A listener for DTMF code received events emitted by the {@link AlertCallScript}
 *
 * @author Thomas Kowalski
 */
interface OnCodeReceivedListener {
    CallOriginator.CallAction onCodeReceived(String code);
}
