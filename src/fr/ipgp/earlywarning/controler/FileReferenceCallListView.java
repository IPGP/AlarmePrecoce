/**
 * 
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.earlywarning.telephones.*;
/**
 * @author patriceboissier
 *
 */
public abstract class FileReferenceCallListView implements FileReferenceCallListListener{
	private FileReferenceCallListControler controler = null;
	
	public FileReferenceCallListView(FileReferenceCallListControler controler) {
		super();
		this.controler = controler;
	}
	
	public final FileReferenceCallListControler getControler() {
		return controler;
	}
	
	public abstract void display();
	public abstract void close();
}
