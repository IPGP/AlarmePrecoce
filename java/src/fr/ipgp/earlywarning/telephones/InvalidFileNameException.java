/**
 * 
 */
package fr.ipgp.earlywarning.telephones;

/**
 * @author patriceboissier
 *
 */
public class InvalidFileNameException extends Exception {
	private static final long serialVersionUID = 633692405543453454L;
	InvalidFileNameException() {
    }
	InvalidFileNameException(String msg) {
        super(msg);
    }
}
