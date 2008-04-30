/**
 * 
 */
package fr.ipgp.earlywarning.telephones;

import java.util.EventObject;
/**
 * @author patriceboissier
 *
 */
public class FileReferenceCallListChangedEvent extends EventObject {
	private String newFileReferenceCallList;
	
	public FileReferenceCallListChangedEvent (Object source, String newFileReferenceCallList) {
		super(source);
		this.newFileReferenceCallList = newFileReferenceCallList;
	}
	
	public String getNewFileReferenceCallList() {
		return newFileReferenceCallList;
	}
}
