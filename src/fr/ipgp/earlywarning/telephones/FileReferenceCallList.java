/**
 * 
 */
package fr.ipgp.earlywarning.telephones;

import javax.swing.event.EventListenerList;
/**
 * @author patriceboissier
 *
 */
public class FileReferenceCallList implements CallList {
	private String file;
	private String type;
	private EventListenerList listeners;
	
	public FileReferenceCallList(String file) throws InvalidFileNameException {
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
	
	public void addFileListener(FileReferenceCallListListener listener){
		listeners.add(FileReferenceCallListListener.class, listener);
	}
	
	public void removeFileListener(FileReferenceCallListListener listener){
		listeners.remove(FileReferenceCallListListener.class, listener);
	}
	
	public void fireFileChanged() {
		FileReferenceCallListListener[] listenerList = (FileReferenceCallListListener[])listeners.getListeners(FileReferenceCallListListener.class);
		
		for(FileReferenceCallListListener listener : listenerList) {
			listener.fileReferenceCallListChanged(new FileReferenceCallListChangedEvent(this, getFile()));
		}
	}
}
