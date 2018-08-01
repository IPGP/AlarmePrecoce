package fr.ipgp.earlywarning.contacts;

import org.json.JSONObject;

/**
 * Simple "structure-like" class representing a Contact, which is a name, a phone number and whether or not it should always be on the top of the call list.
 *
 * @author Thomas Kowalski
 */
public class Contact {
    /**
     * Contact's name
     */
    public final String name;
    /**
     * Contact's phone number
     */
    public final String phone;
    /**
     * Whether or not it's the prioritized contact
     */
    boolean priority;

    /**
     * Normal constructor
     *
     * @param name     the name
     * @param phone    the phone number
     * @param priority whether or not it should be prioritized in the call list
     */
    public Contact(String name, String phone, boolean priority) {
        this.name = name;
        this.phone = phone;
        this.priority = priority;
    }

    public Contact(String name, String phone)
    {
        this(name, phone, false);
    }

    /**
     * JSON Deserializer
     *
     * @param jsonContact the JSONObject to deserialize
     */
    public Contact(JSONObject jsonContact) {
        name = jsonContact.getString("name");
        phone = jsonContact.getString("phone");
        priority = jsonContact.getBoolean("priority");
    }

    @Override
    public String toString() {
        return (priority ? "P " : "") + name + " (" + phone + ")";
    }

    /**
     * Serializes a contact in JSON
     *
     * @return the contact, serialized
     */
    public JSONObject jsonSerialize() {
        JSONObject object = new JSONObject();
        object.put("name", name);
        object.put("phone", phone);
        object.put("priority", priority);
        return object;
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() == this.getClass())
            return equals((Contact) o);
        else
            return false;
    }

    @SuppressWarnings("WeakerAccess")
    public boolean equals(Contact c) {
        return c.priority == this.priority
                && c.name.equals(this.name)
                && c.phone.equals(this.phone);
    }
}
