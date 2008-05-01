/**
 * 
 */
package fr.ipgp.earlywarning.telephones;

import java.util.EventObject;
/**
 * @author patriceboissier
 *
 */
public class FileCallListChangedEvent extends EventObject {
	private static final long serialVersionUID = 6337562405653453454L;
	private String newFileReferenceCallList;
	
	public FileCallListChangedEvent (Object source, String newFileReferenceCallList) {
		super(source);
		this.newFileReferenceCallList = newFileReferenceCallList;
	}
	
	public String getNewFileReferenceCallList() {
		return newFileReferenceCallList;
	}
}
