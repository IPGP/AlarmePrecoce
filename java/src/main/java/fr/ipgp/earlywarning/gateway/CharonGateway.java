/**
 * Created June 6, 2016 12:39:01 PM
 * Copyright 2016 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.gateway;

import fr.ipgp.charon.CharonI;
import fr.ipgp.charon.InvalidLedStateException;
import fr.ipgp.charon.InvalidResponseException;
import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.triggers.Trigger;

import java.io.IOException;

/**
 * A Gateway using the Charon I module in case the Asterisk Server is out of service.
 *
 * @author Patrice Boissier
 * @author Thomas Kowalski
 */
public class CharonGateway implements Gateway {

    private static CharonGateway uniqueInstance;
    private CharonI charon;

    private final static int triggeringLed = 7;

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
        EarlyWarning.appLogger.info("CharonGateway: calling.");
        String phoneToCall = trigger.getContactList().getCallList().get(0).phone;
        callTillConfirm(phoneToCall);
    }

    public void callTillConfirm(String phone) {
        call(triggeringLed);
    }

    @Override
    public void callTest(String number) {
        callTillConfirm(number);
    }

    @Override
    public String getSettingsQualifier() {
        return "charon";
    }

    public boolean call(int triggeringLed) {
        try {
            // Get latest LED state (for debugging purposes)
            charon.refreshLedState();
            System.out.println(charon.toString());

            // Power the LED off
            charon.setLedAt(triggeringLed, 0);
            charon.applyLedState();

            charon.refreshLedState();
            System.out.println(charon.toString());

            // Power the LED on (forces the call to go through)
            charon.setLedAt(triggeringLed, 1);
            charon.applyLedState();

            return true;
        } catch (InvalidLedStateException ignored) {
            // This can't happen: we haven't given a custom LED state String
            return false;
        } catch (IOException ex) {
            System.out.println("Error: " + ex.getMessage());
            return false;
        } catch (InvalidResponseException e) {
            EarlyWarning.appLogger.error("Received invalid response from Charon Gateway: " + e.getResponse());
            EarlyWarning.appLogger.error(e.getMessage());
            return false;
        }
    }
}
