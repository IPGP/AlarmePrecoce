package fr.ipgp.earlywarning.telephones;

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
     * @throws IOException if no corresponding {@link ContactList} implementation can be found or the file can't be read
     */
    public static ContactList build(String filename) throws IOException {
        String extension = getExtension(filename);

        if (extension.equalsIgnoreCase("json"))
            return new JSONContactList(filename);
        else {
            EarlyWarning.appLogger.error("Could not determine the ContactList implementation to use for file name '" + filename + "'");
            throw new IOException("No corresponding implementation found for '" + filename + "'");
        }
    }

    private static String getExtension(String filename) {
        String[] split = filename.split(".");
        return split[split.length - 1];
    }
}
