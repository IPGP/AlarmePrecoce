/**
 * Created Mar 25, 2008 09:20:21 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.gateway;

import fr.ipgp.earlywarning.messages.FileWarningMessage;
import fr.ipgp.earlywarning.telephones.FileCallList;
import fr.ipgp.earlywarning.triggers.Trigger;

/**
 * The phone gateway interface
 *
 * @author Patrice Boissier
 */
public interface Gateway {
    String callText(String phoneNumber, String text, boolean selfDelete);

    String callAudio(String phoneNumber, String audioFile, boolean selfDelete);

    String callStatus(String requestID);

    String callRemove(String requestID);

    String callTillConfirm(String logFile, String messageFile, String confirmCode, String[] phoneNumbers);

    String callTillConfirm(String logFile, String messageFile, String confirmCode, FileCallList callList);

    String callTillConfirm(String callList, String messageFile, String confirmCode);

    String callTillConfirm(Trigger trigger, FileWarningMessage defaultWarningMessage);
}
