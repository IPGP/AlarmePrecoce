/*
  Created Mar 21, 2008 11:20:01 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.gateway;

import fr.ipgp.earlywarning.EarlyWarning;
import fr.ipgp.earlywarning.messages.AudioWarningMessage;
import fr.ipgp.earlywarning.telephones.ContactList;
import fr.ipgp.earlywarning.triggers.Trigger;

import java.util.Random;

/**
 * Mock phone gateway for testing purpose.
 *
 * @author Patrice Boissier
 */
public class MockGateway implements Gateway {

    private static MockGateway uniqueInstance;

    private MockGateway() {

    }

    public static synchronized MockGateway getInstance() {
        if (uniqueInstance == null) {
            uniqueInstance = new MockGateway();
        }
        return uniqueInstance;
    }

    @Override
    public void callTillConfirm(Trigger trigger) {
        EarlyWarning.appLogger.info("MockGateway calling till confirm for trigger " + trigger.toString());
    }

    @Override
    public void callTest(String number) {
        EarlyWarning.appLogger.info("MockGateway emitting test call to " + number);
    }

    @Override
    public String getSettingsQualifier() {
        return "mock";
    }
}
