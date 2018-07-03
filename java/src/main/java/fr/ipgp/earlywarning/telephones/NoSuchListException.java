package fr.ipgp.earlywarning.telephones;

/**
 * The exception to throw when no list can be matched to a requested name in the {@link ContactListMapper}
 *
 * @author Thomas Kowalski
 */
public class NoSuchListException extends Exception {
    public NoSuchListException(String requested) {
        super(requested);
    }
}
