/**
 * Created Apr 26, 2008 08:21:54 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.earlywarning.telephones.*;
import fr.ipgp.earlywarning.view.*;
/**
 * The file call list controler (part of the Observer/MVC pattern)
 * @author Patrice Boissier
 */
public class FileCallListControler {
	public FileCallListView listView = null;
	private FileCallList fileCallList = null;
	
	public FileCallListControler(FileCallList fileCallList) {
		this.fileCallList = fileCallList;
		listView = new JComboBoxFileCallList(this, fileCallList.getFileName());
		this.fileCallList.addFileListener(listView);
	}
	
	/**
	 * Display the file call list view
	 */
	public void displayView() {
		listView.display();
	}
	
	/**
	 * Close the file call list view
	 */
	public void closeView() {
		listView.close();
	}
	
	/**
	 * Action to be made when file is changed
	 * @param file
	 */
	public void notifyFileChanged(String file) {
		fileCallList.setFileName(file);
	}
}
