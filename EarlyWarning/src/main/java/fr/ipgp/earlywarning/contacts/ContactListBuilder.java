package fr.ipgp.earlywarning.contacts;

import fr.ipgp.earlywarning.EarlyWarning;

import java.io.IOException;

/**
 * Provides an easy way to construct a {@link ContactList} based on the name of the file given.
 *
 * @author Thomas Kowalski
 */
@SuppressWarnings("WeakerAccess")
public class ContactListBuilder {
    /**
     * The {@link ContactList} builder itself.
     *
     * @param filename the file from which we want to instantiate a {@link ContactList}
     * @return the corresponding {@link ContactList}
     * @throws IOException if no corresponding {@link ContactList} implementation can be found or the file cannot be read
     */
    public static ContactList build(String filename) throws IOException, UnimplementedContactListTypeException {
        String extension = getExtension(filename);

        if (extension.equalsIgnoreCase("json"))
            return new JSONContactList(filename);
        else {
            EarlyWarning.appLogger.error("Could not determine the ContactList implementation to use for file name '" + filename + "'");
            throw new UnimplementedContactListTypeException("No corresponding implementation found for '" + filename + "'");
        }
    }

    /**
     * Extracts the extension of a given file
     *
     * @param filename the name of the file
     * @return its extension
     */
    private static String getExtension(String filename) {
        String[] split = filename.split("\\.");
        return split[split.length - 1];
    }

    /**
     * The {@link Exception} to throw when the given file name does not have an extension compatible with any kind of {@link ContactList}
     *
     * @author Thomas Kowalski
     */
    public static class UnimplementedContactListTypeException extends Throwable {
        public UnimplementedContactListTypeException(String s) {
            super(s);
        }
    }
}
