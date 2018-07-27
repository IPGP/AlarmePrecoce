/*
  Created Mar 13, 2008 11:07:36 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.triggers;

import fr.ipgp.earlywarning.contacts.ContactListMapper;
import fr.ipgp.earlywarning.utilities.CommonUtilities;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * Create a trigger object from a datagram packet
 *
 * @author Patrice Boissier
 */
public class DatagramTriggerConverter implements TriggerConverter {
    protected DatagramPacket packet;
    protected InetAddress senderAddress;
    protected int senderPort;
    protected Trigger trigger;
    protected String packetContent;
    protected boolean defaultRepeat;
    protected String defaultConfirmCode;
    protected int defaultPriority;

    public DatagramTriggerConverter(DatagramPacket packet, boolean defaultRepeat, String defaultConfirmCode, int defaultPriority) {
        this.packet = packet;
        this.senderAddress = packet.getAddress();
        this.senderPort = packet.getPort();
        this.packetContent = new String(packet.getData(), 0, packet.getLength());
        this.defaultRepeat = defaultRepeat;
        this.defaultConfirmCode = defaultConfirmCode;
        this.defaultPriority = defaultPriority;
        this.trigger = new Trigger(CommonUtilities.getUniqueId(), defaultPriority);
        this.trigger.setInetAddress(senderAddress);
    }

    /**
     * @return trigger the trigger to get
     */
    public Trigger getTrigger() {
        return trigger;
    }

    /**
     * Decode the properties of the received message from the DatagramPacket.
     * Sets the {@link Trigger}'s attributes.
     *
     * @throws UnknownTriggerFormatException, InvalidTriggerFieldException, MissingTriggerFieldException
     */
    public void decode() throws UnknownTriggerFormatException, InvalidTriggerFieldException, MissingTriggerFieldException {
        System.out.println(packetContent);
        String[] packetContentSplit = this.packetContent.split(" ");
        int version;
        if (packetContentSplit[0].matches("\\d\\d"))
            version = Integer.parseInt(packetContentSplit[0]);
        else if (packetContentSplit[0].equals("Sismo"))
            version = 1;
        else
            throw new UnknownTriggerFormatException("Unknown version: " + packetContentSplit[0]);

        switch (version) {
            case 1:
                decodeV1(packetContentSplit);
                break;
            case 2:
                decodeV2(packetContentSplit);
                break;
            default:
                throw new UnknownTriggerFormatException("Unknown version: " + packetContentSplit[0]);
        }

    }

    /**
     * Decode the old OVPF format: type 01
     * <code>Sismo dd/MM/yyyy HH:mm:ss Declenchement</code>
     *
     * @param packetContentElements the elements of the received message
     * @throws InvalidTriggerFieldException if a datagram field is invalid
     */
    private void decodeV1(String[] packetContentElements) throws InvalidTriggerFieldException, MissingTriggerFieldException {
        if (!CommonUtilities.isDate(packetContentElements[1] + " " + packetContentElements[2], "dd/MM/yyyy HH:mm:ss"))
            throw new InvalidTriggerFieldException("Invalid V1 trigger field(s): invalid date " + packetContentElements[1] + " " + packetContentElements[2]);
        if (packetContentElements.length < 4)
            throw new MissingTriggerFieldException("Not enough fields for a V1 trigger: " + this.packetContent);
        trigger.setApplication(packetContentElements[0]);
        trigger.setMessage("default");
        trigger.setType("01");
        trigger.setDate(packetContentElements[1] + " " + packetContentElements[2]);
        trigger.setRepeat(defaultRepeat);
        trigger.setConfirmCode(defaultConfirmCode);
    }

    /**
     * Decode version 2 messages.<br/>
     * <b>Format: </b><br/>
     * <code>vv p yyyy/MM/dd HH:mm:ss application calllist repeat confirmcode message</code><br/>
     * <code>vv</code>: version, two digits<br/>
     * <code>p</code>: priority, one digit<br/>
     * <code>yyyy/MM/dd HH:mm:ss</code>: date, in ISO format<br/>
     * <code>application</code>: application name, [a-zA-Z_0-9]*<br/>
     * <code>list</code>: ID in the configuration of the call list to use<br/>
     * <code>repeat</code>: <code>true</code> or <code>false</code><br/>
     * <code>confirmcode</code>: confirmation code, a digit sequence (1 to 6 digits)
     * <code>message</code>: ID in the configuration of the warning message file to play
     *
     * @param packetContentElements the elements of the received message
     */
    private void decodeV2(String[] packetContentElements) throws InvalidTriggerFieldException, MissingTriggerFieldException {
        StringBuilder warningMessageBuilder = new StringBuilder();
        String warningMessage;
        if (packetContentElements.length > 8) {
            for (int j = 8; j < packetContentElements.length; j++)
                warningMessageBuilder.append(" ").append(packetContentElements[j]);
            warningMessage = warningMessageBuilder.toString().replace("|", "").trim();
        } else
            throw new MissingTriggerFieldException("Not enough fields for a V2 trigger: " + this.packetContent);

        // Priority sanity check
        if (!packetContentElements[1].matches("\\d"))
            throw new InvalidTriggerFieldException("Invalid V2 trigger field(s): invalid priority: '" + packetContentElements[1] + "'");

        // Date sanity check
        if (!CommonUtilities.isDate(packetContentElements[2] + " " + packetContentElements[3], "yyyy/MM/dd HH:mm:ss"))
            throw new InvalidTriggerFieldException("Invalid V2 trigger field(s): invalid date format: '" + packetContentElements[2] + " " + packetContentElements[3] + "'");

        // Application name sanity check
        if (!packetContentElements[4].matches("\\w*"))
            throw new InvalidTriggerFieldException("Invalid V2 trigger field(s): invalid application name: '" + packetContentElements[4] + "'");

        // Call list ID sanity check
        if (!packetContentElements[5].matches("\\w+"))
            throw new InvalidTriggerFieldException("Invalid V2 trigger field(s): invalid call list ID: '" + packetContentElements[5] + "'");

        // Repeat mode sanity check
        if (!(packetContentElements[6].equals("true") || packetContentElements[6].equals("false")))
            throw new InvalidTriggerFieldException("Invalid V2 trigger field(s): invalid repeat: '" + packetContentElements[6] + "'");

        // Confirmation code sanity check
        if (!packetContentElements[7].matches("\\d+") || packetContentElements[7].length() > 7)
            throw new InvalidTriggerFieldException("Invalid V2 trigger field(s): invalid confirmation code: '" + packetContentElements[7] + "'");

        trigger.setContactList(ContactListMapper.getInstance().getListOrDefault(packetContentElements[5]));
        trigger.setMessage(warningMessage);
        trigger.setApplication(packetContentElements[4]);
        trigger.setType(packetContentElements[0]);
        trigger.setPriority(Integer.parseInt(packetContentElements[1]));
        trigger.setDate(packetContentElements[2] + " " + packetContentElements[3]);
        trigger.setRepeat(Boolean.parseBoolean(packetContentElements[6]));
        trigger.setConfirmCode(packetContentElements[7]);
    }
}
