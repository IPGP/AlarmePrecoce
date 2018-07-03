package fr.ipgp.earlywarning.contacts;

/**
 * The exception to throw if a {@link Contact} cannot be found in a {@link ContactList},for example with methods such as <code>findContactWithName</code>
 *
 * @author Thomas Kowalski
 */
public class NoSuchContactException extends Throwable {
    public NoSuchContactException(String s) {
        super(s);
    }
}
