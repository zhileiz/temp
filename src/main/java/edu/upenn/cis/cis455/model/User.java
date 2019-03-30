package edu.upenn.cis.cis455.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable {
    Integer userId;
    String userName;
    String password;

    List<String> channels;
    
    
    public User(Integer userId, String userName, String password) {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
        this.channels = new ArrayList<String>();
    }
    
    public Integer getUserId() {
        return userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getPassword() {
        return password;
    }
}
