/*
  Created Apr 26, 2008 08:20:34 PM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.controler;

/**
 * The file call list view (part of the Observer/MVC pattern)
 *
 * @author Patrice Boissier
 */
public abstract class FileCallListView {
    private FileCallListControler controler;

    public FileCallListView(FileCallListControler controler) {
        super();
        this.controler = controler;
    }

    /**
     * @return the file call list controler
     */
    public final FileCallListControler getControler() {
        return controler;
    }

    /**
     * Display the view
     */
    public abstract void display();

    /**
     * Close the view
     */
    public abstract void close();
}