/**
 * 
 */
package fr.ipgp.earlywarning.telephones;

/**
 * @author patriceboissier
 *
 */
public class FileReferenceCallList implements CallList {
	private String file;
	private String type;
	
	public FileReferenceCallList(String file) throws InvalidFileNameException {
		String [] fileElements = file.split("\\.");
		if (fileElements.length < 2)
			throw new InvalidFileNameException("Invalid File Name : " + file);
		if ((!fileElements[fileElements.length-1].equals("txt")) && (!fileElements[fileElements.length-1].equals("voc")))
			throw new InvalidFileNameException("Invalid File Name : " + file);
		this.file = file;
		this.type = fileElements[fileElements.length-1];
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
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
}
