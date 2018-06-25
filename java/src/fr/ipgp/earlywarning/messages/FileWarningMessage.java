/**
 * Created Mar 5, 2008 8:24:15 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.messages;

/**
 * This class represents the file warning message to be delivered by phone call
 *
 * @author Patrice Boissier
 */
public class FileWarningMessage implements WarningMessage {
    private final WarningMessageType type = WarningMessageType.WAV;
    private String file;

    public FileWarningMessage(String file) {
        this.file = file;
    }

    /**
     * @return a String representing the object
     */
    public String toString() {
        String result = file;
        return result;
    }

    /**
     * @return the file to get
     */
    public String getFile() {
        return file;
    }

    /**
     * @return the type
     */
    public WarningMessageType getType() {
        return type;
    }
}
