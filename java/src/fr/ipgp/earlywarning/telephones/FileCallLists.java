/*
  Created May 01, 2008 10:22:50 AM
  Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.telephones;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Class representing the available file call lists.
 *
 * @author Patrice Boissier
 */
public class FileCallLists implements CallLists {
    private List<FileCallList> fileCallLists = new ArrayList<>();

    public FileCallLists(File directory) throws InvalidFileNameException, FileNotFoundException {
        File[] files = directory.listFiles(
                new FilenameFilter() {
                    public boolean accept(File dir, String name) {
                        return name.endsWith(".txt") || name.endsWith(".voc");
                    }
                });
        if (files == null) {
            throw new FileNotFoundException("No call lists or " + directory.getName() + " not a directory.");
        } else {
            for (File file : files) {
                try {
                    fileCallLists.add(new FileCallList(file));
                } catch (FileNotFoundException ignored) {

                }
            }
        }
    }

    /**
     * @return the fileCallLists
     */
    public List<FileCallList> getFileCallLists() {
        return fileCallLists;
    }

}
