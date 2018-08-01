package fr.ipgp.earlywarning.contacts;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A class used to do complex comparisons on {@link ContactList}s. It compares the enabled contacts (and their call order) and the available contacts.
 *
 * @author Thomas Kowalski
 */
public abstract class ContactListComparer {

    /**
     * Compares two {@link ContactList}s by comparing their enabled contacts and their available contacts.
     * @param a the first {@link ContactList}
     * @param b the second {@link ContactList}
     * @return <code>a equals b</code>
     */
    public static boolean equals(ContactList a, ContactList b)
    {
        if (a == null && b == null)
             return true;

        if (a == null)
            return false;

        if (b == null)
            return false;

        Iterator<Contact> itA = a.getEnabledContacts().iterator();
        Iterator<Contact> itB = b.getEnabledContacts().iterator();

        for(; itA.hasNext() && itB.hasNext(); )
        {
            Contact ca = itA.next();
            Contact cb = itB.next();

            if (!ca.equals(cb))
                return false;
        }

        if (itA.hasNext() != itB.hasNext())
            return false;

        Set<Contact> sa = new HashSet<>(a.getAvailableContacts());
        Set<Contact> sb = new HashSet<>(b.getAvailableContacts());

        if (!sa.equals(sb))
            return false;

        return true;
    }

}
