/**
 * Created Mar 5, 2008 8:24:15 PM
 * Copyright 2008 Observatoire volcanologique du Piton de La Fournaise / IPGP - Voicent Communucations, Inc
 */

package fr.ipgp.earlywarning.telephones;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * @author Patrice Boissier
 *
 */
public class FileCallList implements CallList{
	private File file;
    private int total = -1;
    private ArrayList<String> currentRecord = new ArrayList<String>();
    private BufferedReader bufferedReader = null;
    
	public FileCallList(File file) throws IOException {
		this.file = file;
		bufferedReader = new BufferedReader(new FileReader(file));
        getTotal();
        reset();
	}
	
	/**
	 * @return a String representing the object
	 */
	public String toString() {
		String result = file.toString();
		return result;
	}
	
	/**
	 * 
	 * @return the file to get
	 */
	public File getFile() {
		return this.file;
	}
	
    
    public boolean next() {
        currentRecord.clear();
        try {
            String line = null;
            while (true) {
                line = bufferedReader.readLine();
                if (line == null)
                    return false;
                if (line.length() == 0 || line.charAt(0) == '#')
                    continue;
                break;
            }
                
            StringTokenizer tkz = new StringTokenizer(line, ",");
            while (tkz.hasMoreTokens()) {
                String tk = tkz.nextToken();
                tk.trim();
                currentRecord.add(tk);
            }
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public String getValue(String name) {
        if (CallList.NAME.equals(name))
            return (String) currentRecord.get(0);
        if (CallList.PHONE.equals(name))
            return (String) currentRecord.get(1);
        return null;
    }
    
    public int getTotal() {
        if (total == -1) {
            try {
                total = 0;
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.length() == 0 || line.charAt(0) == '#')
                        continue;
                    total++;
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                total = -1;
            }
        }
        return total;
    }
    
    public void reset() throws IOException {
    	bufferedReader.close();
    	bufferedReader = new BufferedReader(new FileReader(file));
    }
}
