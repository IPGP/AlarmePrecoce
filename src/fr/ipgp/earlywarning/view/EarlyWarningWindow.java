/**
 * 
 */
package fr.ipgp.earlywarning.view;

import javax.swing.*;
import java.awt.*;
import fr.ipgp.earlywarning.controler.actions.*;
/**
 * @author patriceboissier
 *
 */
public class EarlyWarningWindow extends JFrame {
	private JComboBox callListList;
	private JLabel labelSelectedCallList;

	public EarlyWarningWindow() {
		super();
		
		build();
	}
	
	private void build() {
		setTitle("EarlyWarning");
		setSize(400,200);
		setLocationRelativeTo(null);
		setResizable(false);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setContentPane(buildContentPane());
	}
	
	private JPanel buildContentPane(){
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout());
		JLabel labelTitle = new JLabel("Earlywarning System");
		panel.add(labelTitle);
		
		Object[] elements = new Object[]{"Element 1", "Element 2", "Element 3", "Element 4", "Element 5"};		
		callListList = new JComboBox(elements);
		panel.add(callListList);
		
		JButton choseCallListButton = new JButton(new ChoseCallListAction(this, "Choisir"));
		panel.add(choseCallListButton);
		
		labelSelectedCallList = new JLabel("Liste d'appel selectionnee : ");
		panel.add(labelSelectedCallList);
		
		return panel;
	}
	
	public JComboBox getCallListList() {
		return callListList;
	}
	
	public JLabel getLabelSelectedCallList() {
		return labelSelectedCallList;
	}
}
