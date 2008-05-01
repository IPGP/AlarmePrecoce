/**
 * 
 */
package fr.ipgp.earlywarning.telephones;

import javax.swing.event.EventListenerList;
/**
 * @author patriceboissier
 *
 */
public class FileCallList implements CallList {
	private String file;
	private String type;
	private EventListenerList listeners;
	
	public FileCallList(String file) throws InvalidFileNameException {
		String [] fileElements = file.split("\\.");
		if (fileElements.length < 2)
			throw new InvalidFileNameException("Invalid File Name : " + file);
		if ((!fileElements[fileElements.length-1].equals("txt")) && (!fileElements[fileElements.length-1].equals("voc")))
			throw new InvalidFileNameException("Invalid File Name : " + file);
		this.file = file;
		this.type = fileElements[fileElements.length-1];
		listeners = new EventListenerList();
	}
	
	/**
	 * @return a String representing the object
	 */
	public String toString() {
		String result = file.toString();
		return result;
	}
	
	/**
	 * 
	 */
	public void setFile(String file) {
		this.file = file;
		fireFileChanged();
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
	
	public void addFileListener(FileCallListListener listener){
		listeners.add(FileCallListListener.class, listener);
	}
	
	public void removeFileListener(FileCallListListener listener){
		listeners.remove(FileCallListListener.class, listener);
	}
	
	public void fireFileChanged() {
		FileCallListListener[] listenerList = (FileCallListListener[])listeners.getListeners(FileCallListListener.class);
		
		for(FileCallListListener listener : listenerList) {
			listener.fileReferenceCallListChanged(new FileCallListChangedEvent(this, getFile()));
		}
	}
}
