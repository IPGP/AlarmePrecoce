/**
 * 
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.earlywarning.telephones.*;
/**
 * @author patriceboissier
 *
 */
public abstract class FileCallListView implements FileCallListListener{
	private FileCallListControler controler = null;
	
	public FileCallListView(FileCallListControler controler) {
		super();
		this.controler = controler;
	}
	
	public final FileCallListControler getControler() {
		return controler;
	}
	
	public abstract void display();
	public abstract void close();
}
