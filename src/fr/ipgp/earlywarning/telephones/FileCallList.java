/**
 * Created Mar 5, 2008 8:24:15 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP
 */
package fr.ipgp.earlywarning.telephones;

import javax.swing.event.EventListenerList;
import java.io.*;
/**
 * Class representing file call lists. File call lists must be txt (comma separated values) or voc (Voicent call list).<br/>
 * Implements the observer pattern (subject)
 * @author Patrice Boissier
 */
public class FileCallList implements CallList {
	private File file;
	private String type;
	private EventListenerList listeners;
	
	public FileCallList(String fileName) throws InvalidFileNameException, FileNotFoundException {
		this.file = new File(fileName);
		if (!this.file.exists())
			throw new FileNotFoundException("File call list does not exists");
		this.type = extractExtension(file);
		listeners = new EventListenerList();
	}

	public FileCallList(File file) throws InvalidFileNameException, FileNotFoundException {
		this.file = file;
		if (!this.file.exists())
			throw new FileNotFoundException("File call list does not exists");
		this.type = extractExtension(file);
		listeners = new EventListenerList();
	}
	
	/**
	 * Extracts and returns the file extension
	 * @param file the file to extract the extension from
	 * @return the file extension (String)
	 * @throws InvalidFileNameException if the extension is not "txt" or "voc"
	 */
	private String extractExtension(File file) throws InvalidFileNameException {
		String [] fileElements = file.getName().split("\\.");
		if (fileElements.length < 2)
			throw new InvalidFileNameException("Invalid File Name : " + file.getName());
		if ((!fileElements[fileElements.length-1].equals("txt")) && (!fileElements[fileElements.length-1].equals("voc")))
			throw new InvalidFileNameException("Invalid File Name : " + file.getName());
		return fileElements[fileElements.length-1];
	}
		
	/**
	 * @param fileName the file name to set
	 */
	public void setFileName(String fileName) {
		this.file = new File(fileName);
		//this.type = extractExtension(fileName);
		fireFileChanged();
	}
	
	/**
	 * @return the file to get
	 */
	public String getFileName() {
		return file.getName();
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
	
	/**
	 * @param listener the listener to add
	 */
	public void addFileListener(FileCallListListener listener){
		listeners.add(FileCallListListener.class, listener);
	}
	
	/**
	 * @param listener the listener to remove
	 */
	public void removeFileListener(FileCallListListener listener){
		listeners.remove(FileCallListListener.class, listener);
	}
	
	/**
	 * Actions to be made upon file change
	 */
	public void fireFileChanged() {
		FileCallListListener[] listenerList = (FileCallListListener[])listeners.getListeners(FileCallListListener.class);
		
		for(FileCallListListener listener : listenerList) {
			listener.fileReferenceCallListChanged(new FileCallListChangedEvent(this, getFileName()));
		}
	}
}
