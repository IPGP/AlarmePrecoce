package fr.ipgp.earlywarning.telephones;

import java.io.IOException;

public class ContactListBuilder {
    public static ContactList build(String filename) throws IOException {
        // TODO: fix this
        return new JSONContactList(filename);
    }
}
