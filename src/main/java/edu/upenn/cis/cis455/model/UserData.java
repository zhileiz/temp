package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class UserData implements Serializable {
    String firstName;
    String lastName;
    String passwordHash;

    public UserData(String firstName, String lastName, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = encryptPassword(password);
    }

    private String encryptPassword(String password) {
        return password;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPassword_hash() {
        return passwordHash;
    }

    @Override
    public String toString() {
        return "[UserData: firstName=" + firstName +
                " lastName=" + lastName +
                " passwordHash" + passwordHash + "]";
    }
}
