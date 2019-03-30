package edu.upenn.cis.cis455.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserData implements Serializable {
    String firstName;
    String lastName;
    String passwordHash;
    List<String> channels;

    public UserData(String firstName, String lastName, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = encryptPassword(password);
        this.channels = new ArrayList<>();
    }

    private String encryptPassword(String password) { return password; }

    public String getFirstName() { return firstName; }

    public String getLastName() { return lastName; }

    public String getPassword_hash() { return passwordHash; }

    public List<String> getChannels() { return channels; }

    public void addChannel(String channel) { channels.add(channel); }

    @Override
    public String toString() {
        return "[UserData: firstName=" + firstName +
                " lastName=" + lastName +
                " passwordHash" + passwordHash + "]";
    }
}
