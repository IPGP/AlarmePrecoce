/**
 * 
 */
package fr.ipgp.earlywarning.messages;

import java.io.*;
/**
 * @author patriceboissier
 *
 */
public class TextFileWarningMessage implements WarningMessage {
	private String file;
	private String fileContent;
	private final WarningMessageType type = WarningMessageType.TXT;
	
	public TextFileWarningMessage(String file) {
		this.file = file;
		this.fileContent = getContent();
	}

	/**
	 * @return a String representing the object
	 */
	public String toString() {
		String result = file.toString();
		return result;
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
			BufferedReader input =  new BufferedReader(new FileReader(new File(file)));
			try {
		        String line = null;
		        while (( line = input.readLine()) != null){
		        	contents.append(line);
		        	contents.append(System.getProperty("line.separator"));
		        }
			} finally {
				input.close();
			}
		} catch (IOException ex){
			ex.printStackTrace();
		}	    
		return contents.toString();
	}
}