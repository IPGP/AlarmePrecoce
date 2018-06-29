package fr.ipgp.earlywarning.messages;

public class NoSuchMessageException extends Exception {
    public NoSuchMessageException(String requested)
    {
        super(requested);
    }
}
