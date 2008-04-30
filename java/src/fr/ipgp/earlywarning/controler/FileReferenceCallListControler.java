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
public class FileReferenceCallListControler {
	public FileReferenceCallListView listView = null;
	
	private FileReferenceCallList fileReferenceCallList = null;
	
	public FileReferenceCallListControler(FileReferenceCallList fileReferenceCallList) {
		this.fileReferenceCallList = fileReferenceCallList;
		listView = new JComboBoxFileReferenceCallList(this, fileReferenceCallList.getFile());
		this.fileReferenceCallList.addFileListener(listView);
	}
	
	public void displayView() {
		listView.display();
	}
	
	public void closeView() {
		listView.close();
	}
	
	public void notifyFileChanged(String file) {
		fileReferenceCallList.setFile(file);
	}
}
