/**
 * 
 */
package fr.ipgp.earlywarning.view;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import fr.ipgp.earlywarning.telephones.*;
import fr.ipgp.earlywarning.controler.*;
/**
 * @author patriceboissier
 *
 */
public class JComboBoxFileReferenceCallList extends FileReferenceCallListView implements ActionListener{
	private JFrame frame = null;
	private JPanel panel = null;
	private JComboBox callListList = null;
	private JLabel labelSelectedCallList = null;
	private JLabel labelTitle = null;
	private String [] files;
	private JButton choseCallListButton = null;
	
	public JComboBoxFileReferenceCallList(FileReferenceCallListControler controler) {
		this(controler, "");
	}
	
	public JComboBoxFileReferenceCallList(FileReferenceCallListControler controler, String file) {
		super(controler); 
		buildFrame(file);
	}
	
	private void buildFrame(String file) {
		frame = new JFrame();
		frame.setTitle("EarlyWarning");
		frame.setSize(400,200);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel = new JPanel();
		panel.setLayout(new FlowLayout());
		labelTitle = new JLabel("Earlywarning System");
		panel.add(labelTitle);
		
		files = new String[]{"callList1.voc", "callList2.voc", "callList3.voc"};		
		callListList = new JComboBox(files);
		callListList.setSelectedItem(file);
		panel.add(callListList);
		
		choseCallListButton = new JButton("Choisir");
		choseCallListButton.addActionListener(this);
		panel.add(choseCallListButton);
		
		labelSelectedCallList = new JLabel("Liste d'appel selectionnee : " + file);
		panel.add(labelSelectedCallList);
		
		frame.setContentPane(panel);
		
	}
	
	@Override
	public void close() {
		frame.dispose();
	}

	@Override
	public void display() {
		System.out.println("Display!!!");
		frame.setVisible(true);
	}
	
	public void fileReferenceCallListChanged(FileReferenceCallListChangedEvent event) {
		labelSelectedCallList.setText("Liste d'appel par defaut : " + event.getNewFileReferenceCallList());
		callListList.setSelectedItem(event.getNewFileReferenceCallList());
	}

	public void actionPerformed(ActionEvent arg0) {
		getControler().notifyFileChanged((String)callListList.getSelectedItem());
	}

}
