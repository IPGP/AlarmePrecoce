/**
 * Created June 6, 2016 12:39:01 PM
 * Copyright 2016 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.gateway;

import fr.ipgp.common.charon.CharonI;
import fr.ipgp.common.charon.InvalidLedStateException;
import fr.ipgp.earlywarning.triggers.Trigger;

import java.io.IOException;

/**
 *
 * @author Patrice Boissier
 */
public class CharonGateway implements Gateway {

    private static CharonGateway uniqueInstance;
    private CharonI charon;

    private CharonGateway(String moduleIP, int modulePort, int tcpTimeout) {
        charon = new CharonI(moduleIP, modulePort, tcpTimeout);
    }

    public static synchronized CharonGateway getInstance(String moduleIP, int modulePort, int tcpTimeout) {
        if (uniqueInstance == null) {
            uniqueInstance = new CharonGateway(moduleIP, modulePort, tcpTimeout);
        }
        return uniqueInstance;
    }

    public void callTillConfirm(Trigger trigger) {
        String phoneToCall = trigger.getContactList().getCallList().get(0).phone;
        callTillConfirm(phoneToCall);
    }

    public void callTillConfirm(String phone) {

    }

    @Override
    public void callTest(String number) {
        callTillConfirm(number);
    }

    @Override
    public String getSettingsQualifier() {
        return "charon";
    }

    public Boolean changeLed(int ledNumber) {
        try {
            // Recuperation de l'etat des leds
            charon.getEtatLeds();
            // changement d'etat a 0 (allume) de la led concernee
            charon.setLedsAt(ledNumber, 0);
            charon.setEtatLeds();

            // changement d'etat a 0 (allume) de la led concernee
            charon.setLedsAt(ledNumber, 1);
            charon.setEtatLeds();
            return true;
        } catch (InvalidLedStateException ex) {
            System.out.println("Erreur: " + ex.getMessage());
            return false;
        } catch (IOException ex) {
            System.out.println("Erreur: " + ex.getMessage());
            return false;
        }
    }
}
