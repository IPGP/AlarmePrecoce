/**
 * 
 */
package fr.ipgp.earlywarning.controler.actions;

import fr.ipgp.earlywarning.view.*;
import javax.swing.*;
import java.awt.event.*;
/**
 * @author patriceboissier
 *
 */
public class ChoseCallListAction extends AbstractAction {
	private EarlyWarningWindow window;
	
	public ChoseCallListAction (EarlyWarningWindow window, String texte){
		super(texte);
		this.window = window;
	}
	
	public void actionPerformed(ActionEvent e) { 
		Object selected = window.getCallListList().getSelectedItem();
		window.getLabelSelectedCallList().setText("Liste d'appel selectionnee : " + (String)selected);
	} 
}
