package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class UserKey implements Serializable {

    private String username;

    public UserKey(String username) {
        this.username = username.trim();
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "[UserKey: username=" + username + "]";
    }


}
