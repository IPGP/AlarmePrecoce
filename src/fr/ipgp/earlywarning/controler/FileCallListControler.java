/**
 * 
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.earlywarning.telephones.*;
import fr.ipgp.earlywarning.view.*;
/**
 * @author patriceboissier
 *
 */
public class FileCallListControler {
	public FileCallListView listView = null;
	private FileCallList fileCallList = null;
	
	public FileCallListControler(FileCallList fileCallList) {
		this.fileCallList = fileCallList;
		listView = new JComboBoxFileCallList(this, fileCallList.getFile());
		this.fileCallList.addFileListener(listView);
	}
	
	public void displayView() {
		listView.display();
	}
	
	public void closeView() {
		listView.close();
	}
	
	public void notifyFileChanged(String file) {
		fileCallList.setFile(file);
	}
}
