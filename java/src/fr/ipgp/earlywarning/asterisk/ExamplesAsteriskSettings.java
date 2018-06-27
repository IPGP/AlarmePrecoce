package fr.ipgp .earlywarning.asterisk;

import org.asteriskjava.pbx.DefaultAsteriskSettings;

public class ExamplesAsteriskSettings extends DefaultAsteriskSettings {

    @Override
    public String getManagerPassword() {
        // this password MUST match the password (secret=) in manager.conf
        return "ipgp";
    }

    @Override
    public String getManagerUsername() {
        // this MUST match the section header '[myconnection]' in manager.conf
        return "manager";
    }

    @Override
    public String getAsteriskIP() {
        // The IP address or FQDN of your Asterisk server.
        return "195.83.188.41";
    }

    @Override
    public String getAgiHost() {
        // The IP Address or FQDN of you asterisk-java application.
        return "195.83.188.229";
        // return "localhost";
        // return "127.0.0.1";
    }

    @Override
    public int getManagerPortNo() {
        return 5038;
    }

}