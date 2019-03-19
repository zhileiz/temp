package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class ContentHashKey implements Serializable {
    String hashKey;

    public ContentHashKey(String hashKey) {
        this.hashKey = hashKey;
    }

    public String getUrl() {
        return hashKey;
    }

    @Override
    public String toString() {
        return "[DocumentKey: " + "hashKey=" + hashKey + "]";
    }
}

