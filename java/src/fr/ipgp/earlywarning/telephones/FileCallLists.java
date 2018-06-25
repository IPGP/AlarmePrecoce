/**
 * Created May 01, 2008 10:22:50 AM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP.
 */
package fr.ipgp.earlywarning.telephones;
import java.io.*;
import java.util.*;
/**
 * Class representing the available file call lists.
 * @author Patrice Boissier
 */
public class FileCallLists implements CallLists {
	private List<FileCallList> fileCallLists = new ArrayList<FileCallList>();
	
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
	        for (int i = 0; i<files.length; i++) {
	        	try {
	        		fileCallLists.add(new FileCallList(files[i]));
	        	} catch (FileNotFoundException fnfe) {
	        		
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
