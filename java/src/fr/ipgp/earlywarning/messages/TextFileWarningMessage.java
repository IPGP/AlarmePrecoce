/*

 */
package fr.ipgp.earlywarning.messages;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * @author patriceboissier
 */
public class TextFileWarningMessage implements WarningMessage {
    private final WarningMessageType type = WarningMessageType.TXT;
    private String file;
    private String fileContent;

    public TextFileWarningMessage(String file) {
        this.file = file;
        this.fileContent = getContent();
    }

    /**
     * @return a String representing the object
     */
    public String toString() {
        return file;
    }

    /**
     * @return the file to get
     */
    public String getFile() {
        return file;
    }

    /**
     * @return the fileContent to get
     */
    public String getFileContent() {
        return fileContent;
    }

    /**
     * @return the type
     */
    public WarningMessageType getType() {
        return type;
    }

    private String getContent() {
        StringBuilder contents = new StringBuilder();
        try {
            try (BufferedReader input = new BufferedReader(new FileReader(new File(file)))) {
                String line;
                while ((line = input.readLine()) != null) {
                    contents.append(line);
                    contents.append(System.getProperty("line.separator"));
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return contents.toString();
    }
}
