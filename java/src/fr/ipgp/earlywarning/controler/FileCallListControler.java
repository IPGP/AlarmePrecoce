/*
  Created Apr 26, 2008 08:21:54 PM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.controler;

import fr.ipgp.earlywarning.telephones.FileCallList;
import fr.ipgp.earlywarning.telephones.FileCallLists;
import fr.ipgp.earlywarning.view.JComboBoxFileCallList;

/**
 * The file call list controler (part of the Observer/MVC pattern)
 *
 * @author Patrice Boissier
 */
public class FileCallListControler {
    public FileCallListView listView;
    private FileCallList fileCallList;

    public FileCallListControler(FileCallList fileCallList, FileCallLists fileCallLists) {
        this.fileCallList = fileCallList;
        listView = new JComboBoxFileCallList(this, fileCallList, fileCallLists);
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
     *
     * @param file the file that has changed
     */
    public void notifyFileChanged(String file) {
        fileCallList.setFileName(file);
    }
}
