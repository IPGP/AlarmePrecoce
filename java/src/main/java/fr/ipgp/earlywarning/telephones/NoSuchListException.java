package fr.ipgp.earlywarning.telephones;

public class NoSuchListException extends Exception {
    public NoSuchListException(String requested) {
        super(requested);
    }
}
