/**
 * 
 */
package fr.ipgp.earlywarning.utilities;

import java.io.*;
/**
 * This class represents a Voc file.<br/>
 * the voc file structure is the following (hex format)<br/>
 * <br/>
 * file header : 03 00 00 00 03 00 00 00 04 4E 61 6D 65 05 50 68 6F 6E 65 06 53 74 61 74 75 73 12 00 00 00<br/> 
 * Line entry : [name size in hex format (1 byte)] [name (name size bytes)] 0A [telephone number (n bytes)] 00<br/>
 * (...)<br/>
 * file footer : 00 50 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00<br/>
 * <br/>
 * Example :<br/>
 * <br/>
 * A Voc File containing :<br/>
 * <table border=1>
 *   <tr><td>Patrice GSM</td><td>0692703856</td></tr>
 *   <tr><td>Patrice Domicile</td><td>0262248610</td></tr>
 * </table>
 * is coded : <br/>
 * 03 00 00 00 03 00 00 00 04 4E 61 6D 65 05 50 68 6F 6E 65 06 53 74 61 74 75 73 12 00 00 00<br/> 
 * 0B 50 61 74 72 69 63 65 20 47 53 4D 0A 30 36 39 32 37 30 33 38 35 36 00<br/>
 * 10 50 61 74 72 69 63 65 20 44 6F 6D 69 63 69 6C 65 0A 30 32 36 32 32 34 38 36 31 30 00<br/> 
 * 00 50 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 <br/>
 * 
 * @author Patrice Boissier
 *
 */
public class VocFile {
	private File file;
	
	public void addEntry(String name, String phoneNumber) {
		
	}
`
	public void addEntry(String name, String phoneNumber, int atLine) {
		
	}
}
