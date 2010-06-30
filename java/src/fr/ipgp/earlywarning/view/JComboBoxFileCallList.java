/**
 * Created Apr 30, 2008 11:01:05 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.view;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import fr.ipgp.earlywarning.telephones.*;
import fr.ipgp.earlywarning.controler.*;
/**
 * File call list JComboBox representation.
 * @author Patrice Boissier
 */
public class JComboBoxFileCallList extends FileCallListView implements ActionListener{
	private JFrame frame = null;
	private JPanel panelTitle = null;
	private JPanel panelCallList = null;
	private JPanel panelSelectedCallList = null;
	private JComboBox callListList = null;
	private JLabel labelSelectedCallList = null;
	private JLabel labelTitle = null;
	private String [] files;
	private JButton choseCallListButton = null;
	private FlowLayout layout = new FlowLayout();
	
	public JComboBoxFileCallList(FileCallListControler controler) {
		this(controler, null, null);
	}
	
	public JComboBoxFileCallList(FileCallListControler controler, FileCallList fileCallList, FileCallLists fileCallLists) {
		super(controler); 
		buildFrame(fileCallList, fileCallLists);
	}
	
	private void buildFrame(FileCallList fileCallList, FileCallLists fileCallLists) {		
		frame = new JFrame();
		frame.setTitle("EarlyWarning");
		frame.setSize(400,200);
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		layout.setAlignment(FlowLayout.CENTER);
		
		panelTitle = new JPanel();
		panelTitle.setLayout(layout);
		labelTitle = new JLabel("Earlywarning System");
		panelTitle.add(labelTitle);
		
//		panelCallList = new JPanel();
//		panelCallList.setLayout(layout);
//		files = new String[fileCallLists.getFileCallLists().size()];
//		for (int i = 0; i < fileCallLists.getFileCallLists().size(); i++) {
//			files[i] = fileCallLists.getFileCallLists().get(i).getFileName();
//		};
//		callListList = new JComboBox(files);
//		callListList.setSelectedItem(fileCallList.getFileName());
//		panelCallList.add(callListList);
//		
//		choseCallListButton = new JButton("Choisir");
//		choseCallListButton.addActionListener(this);
//		panelCallList.add(choseCallListButton);
		
		panelSelectedCallList = new JPanel();
		panelSelectedCallList.setLayout(layout);
		labelSelectedCallList = new JLabel("Liste d'appel selectionnee : " + fileCallList.getFileName());
		panelSelectedCallList.add(labelSelectedCallList);

		panelCallList.add(panelSelectedCallList);
		
		frame.getContentPane().add(panelTitle, BorderLayout.NORTH);
		frame.getContentPane().add(panelCallList, BorderLayout.CENTER);
	}
	
	@Override
	public void close() {
		frame.dispose();
	}

	@Override
	public void display() {
		frame.setVisible(true);
	}
	
	public void fileReferenceCallListChanged(FileCallListChangedEvent event) {
		labelSelectedCallList.setText("Liste d'appel par defaut : " + event.getNewFileReferenceCallList());
		callListList.setSelectedItem(event.getNewFileReferenceCallList());
	}

	public void actionPerformed(ActionEvent arg0) {
		getControler().notifyFileChanged((String)callListList.getSelectedItem());
	}

}
