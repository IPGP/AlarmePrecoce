/**
 * Created May 01, 2008 11:01:05 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.telephones;

import java.util.EventObject;
/**
 * @author Patrice Boissier
 * File call list event, part of the observer pattern.
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
