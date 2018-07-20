/*
  Created Mar 25, 2008 09:20:21 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.gateway;

import fr.ipgp.earlywarning.triggers.Trigger;

/**
 * The phone gateway interface
 *
 * @author Thomas Kowalski
 */
public interface Gateway {
    /**
     * Calls the contacts from the trigger's contact list until one confirms the message.
     *
     * @param trigger that triggered the call
     */
    CallLoopResult callTillConfirm(Trigger trigger);

    void callTest(String number);

    /**
     * Returns the name that should be used to configure the sounds in the Settings.
     * For Asterisk, this method should return a descriptive name, like <code>myGateway</code>, and the configuration
     * should provide a configuration should contain mappings for each sound:<br />
     * <code>
     *     <sounds>
     *         <sound>
     *             <id>SOUNDID</id>
     *             <...>mapping_other_gateway</...>
     *             <myGateway>mapping_for_myGateway</myGateway>
     *         </sound>
     *     </sounds>
     * </code>
     *
     * @return the qualifier used for configuration
     */
    String getSettingsQualifier();
}
