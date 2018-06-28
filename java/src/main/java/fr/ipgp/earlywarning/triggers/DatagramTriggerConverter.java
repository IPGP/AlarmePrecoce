/*
  Created Mar 13, 2008 11:07:36 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.triggers;

import fr.ipgp.earlywarning.messages.AudioWarningMessage;
import fr.ipgp.earlywarning.messages.TextWarningMessage;
import fr.ipgp.earlywarning.messages.WarningMessage;
import fr.ipgp.earlywarning.telephones.FileCallList;
import fr.ipgp.earlywarning.telephones.InvalidFileNameException;
import fr.ipgp.earlywarning.telephones.TextCallList;
import fr.ipgp.earlywarning.utilities.CommonUtilities;

import java.io.IOException;
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
    protected FileCallList defaultCallList;
    protected WarningMessage defaultWarningMessage;
    protected boolean defaultRepeat;
    protected String defaultConfirmCode;
    protected int defaultPriority;

    public DatagramTriggerConverter(DatagramPacket packet, FileCallList defaultCallList, WarningMessage defaultWarningMessage, boolean defaultRepeat, String defaultConfirmCode, int defaultPriority) {
        this.packet = packet;
        this.senderAddress = packet.getAddress();
        this.senderPort = packet.getPort();
        this.packetContent = new String(packet.getData(), 0, packet.getLength());
        this.defaultCallList = defaultCallList;
        this.defaultWarningMessage = defaultWarningMessage;
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
     * Set the Trigger attributes.
     *
     * @throws UnknownTriggerFormatException, InvalidTriggerFieldException, MissingTriggerFieldException
     */
    public void decode() throws UnknownTriggerFormatException, InvalidTriggerFieldException, MissingTriggerFieldException, IOException, InvalidFileNameException {
        String[] packetContentSplit = this.packetContent.split(" ");
        int version;
        if (packetContentSplit[0].matches("\\d\\d"))
            version = Integer.parseInt(packetContentSplit[0]);
        else {
            if (packetContentSplit[0].equals("Sismo"))
                version = 1;
            else {
                throw new UnknownTriggerFormatException("Unknown version : " + packetContentSplit[0]);
            }
        }

        switch (version) {
            case 1:
                decodeV1(packetContentSplit);
                break;
            case 2:
                decodeV2(packetContentSplit);
                break;
            default:
                throw new UnknownTriggerFormatException("Unknown version : " + packetContentSplit[0]);
        }
    }

    /**
     * Decode the old OVPF format : type 01
     * Sismo dd/MM/yyyy HH:mm:ss Declenchement
     *
     * @param packetContentElements the elements of the received message
     * @throws InvalidTriggerFieldException if a datagram field is invalid
     */
    private void decodeV1(String[] packetContentElements) throws InvalidTriggerFieldException, MissingTriggerFieldException {
        if (!CommonUtilities.isDate(packetContentElements[1] + " " + packetContentElements[2], "dd/MM/yyyy HH:mm:ss"))
            throw new InvalidTriggerFieldException("Invalid V1 trigger field(s) : invalid date " + packetContentElements[1] + " " + packetContentElements[2]);
        if (packetContentElements.length < 4)
            throw new MissingTriggerFieldException("Not enough fields for a V1 trigger : " + this.packetContent);
        trigger.setApplication(packetContentElements[0]);
        trigger.setCallList(defaultCallList);
        trigger.setMessage(defaultWarningMessage);
        trigger.setType("01");
        trigger.setDate(packetContentElements[1] + " " + packetContentElements[2]);
        trigger.setRepeat(defaultRepeat);
        trigger.setConfirmCode(defaultConfirmCode);
    }

    /**
     * Decode version 2 messages.<br/>
     * <b>Format : </b><br/>
     * vv p yyyy/MM/dd HH:mm:ss application calllist repeat confirmcode message<br/>
     * vv : version, two digit<br/>
     * p : priority, one digit<br/>
     * yyyy/MM/dd HH:mm:ss : date, ISO format<br/>
     * application : application name, [a-zA-Z_0-9]*<br/>
     * calllist : call list, either a comma separated list of digits or a .csv file name ([a-zA-Z_0-9]*\.csv)<br/>
     * repeat : true or false<br/>
     * confirmcode : confirmation code, a digit sequence (1 to 6 digits)
     * message : warning message, either text message encapsulated between two "pipes" (|) or a .wav file ([a-zA-Z_0-9]*\.wav)
     *
     * @param packetContentElements the elements of the received message
     */
    private void decodeV2(String[] packetContentElements) throws InvalidTriggerFieldException, MissingTriggerFieldException, IOException, InvalidFileNameException {
        StringBuilder warningMessage = new StringBuilder();
        boolean first = true;
        if (packetContentElements.length > 8) {
            for (int j = 8; j < packetContentElements.length; j++) {
                if (first) {
                    warningMessage = new StringBuilder(packetContentElements[j]);
                    first = false;
                } else
                    warningMessage.append(" ").append(packetContentElements[j]);
            }
        } else
            throw new MissingTriggerFieldException("Not enough fields for a V2 trigger : " + this.packetContent);

        if (!packetContentElements[1].matches("\\d"))
            throw new InvalidTriggerFieldException("Invalid V2 trigger field(s) : invalid priority " + packetContentElements[1]);
        if (!CommonUtilities.isDate(packetContentElements[2] + " " + packetContentElements[3], "yyyy/MM/dd HH:mm:ss"))
            throw new InvalidTriggerFieldException("Invalid V2 trigger field(s) : invalid date format " + packetContentElements[2] + " " + packetContentElements[3]);
        if (!packetContentElements[4].matches("\\w*"))
            throw new InvalidTriggerFieldException("Invalid V2 trigger field(s) : invalid application name " + packetContentElements[4]);
        if (!(packetContentElements[6].equals("true") || packetContentElements[6].equals("false")))
            throw new InvalidTriggerFieldException("Invalid V2 trigger field(s) : invalid repeat " + packetContentElements[6]);
        if (!packetContentElements[7].matches("\\d+") || packetContentElements[7].length() > 7)
            throw new InvalidTriggerFieldException("Invalid V2 trigger field(s) : invalid confirm code " + packetContentElements[7]);
        if (packetContentElements[5].matches("\\w+\\.txt")) {
            trigger.setCallList(new FileCallList(defaultCallList.getFilePath() + "/" + packetContentElements[5]));
        } else {
            if (packetContentElements[5].matches("\\w+\\.voc")) {
                trigger.setCallList(new FileCallList(defaultCallList.getFilePath() + "/" + packetContentElements[5]));
            } else {
                if (packetContentElements[5].matches("(\\d*)(,\\d*)*")) {
                    String phoneNumbers = packetContentElements[5].replaceAll(",", " ");
                    trigger.setCallList(new TextCallList(phoneNumbers));
                } else
                    throw new InvalidTriggerFieldException("Invalid V2 trigger field(s) : invalid call list " + packetContentElements[5]);
            }
        }
        if (warningMessage.toString().matches("\\w+\\.wav"))
            trigger.setMessage(new AudioWarningMessage(warningMessage.toString()));
        else {
            if (warningMessage.toString().matches("\\|[\\w\\s!\\?,\\.':\\(\\)\\u00C0-\\u00FF]*\\|"))
                trigger.setMessage(new TextWarningMessage(warningMessage.toString()));
            else
                throw new InvalidTriggerFieldException("Invalid V2 trigger field(s) : invalid warning message " + warningMessage);
        }
        trigger.setApplication(packetContentElements[4]);
        trigger.setType(packetContentElements[0]);
        trigger.setPriority(Integer.parseInt(packetContentElements[1]));
        trigger.setDate(packetContentElements[2] + " " + packetContentElements[3]);
        trigger.setRepeat(Boolean.parseBoolean(packetContentElements[6]));
        trigger.setConfirmCode(packetContentElements[7]);
    }
}
