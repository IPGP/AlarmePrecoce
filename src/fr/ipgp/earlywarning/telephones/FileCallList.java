/**
 * 
 */
package fr.ipgp.earlywarning.telephones;

import javax.swing.event.EventListenerList;
import java.io.*;
/**
 * @author patriceboissier
 *
 */
public class FileCallList implements CallList {
	private String fileName;
	private File file;
	private String type;
	private EventListenerList listeners;
	
	public FileCallList(String fileName) throws InvalidFileNameException {
		this.fileName = fileName;
		this.file = new File(fileName);
		this.type = extractExtension(fileName);
		listeners = new EventListenerList();
	}

	public FileCallList(File file) throws InvalidFileNameException {
		this.file = file;
		this.fileName = file.getName();
		this.type = extractExtension(fileName);
		listeners = new EventListenerList();
	}
	
	private String extractExtension(String fileName) throws InvalidFileNameException {
		String [] fileElements = fileName.split("\\.");
		if (fileElements.length < 2)
			throw new InvalidFileNameException("Invalid File Name : " + fileName);
		if ((!fileElements[fileElements.length-1].equals("txt")) && (!fileElements[fileElements.length-1].equals("voc")))
			throw new InvalidFileNameException("Invalid File Name : " + fileName);
		return fileElements[fileElements.length-1];
	}
	
	/**
	 * @return a String representing the object
	 */
//	public String toString() {
//		String result = file.toString();
//		return result;
//	}
	
	/**
	 * 
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
		this.file = new File(fileName);
		//this.type = extractExtension(fileName);
		fireFileChanged();
	}
	
	/**
	 * @return the file to get
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
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
	
	public void addFileListener(FileCallListListener listener){
		listeners.add(FileCallListListener.class, listener);
	}
	
	public void removeFileListener(FileCallListListener listener){
		listeners.remove(FileCallListListener.class, listener);
	}
	
	public void fireFileChanged() {
		FileCallListListener[] listenerList = (FileCallListListener[])listeners.getListeners(FileCallListListener.class);
		
		for(FileCallListListener listener : listenerList) {
			listener.fileReferenceCallListChanged(new FileCallListChangedEvent(this, getFileName()));
		}
	}
}
