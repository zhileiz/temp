package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class DocumentKey implements Serializable {
    String url;

    public DocumentKey(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "[DocumentKey: " + "url=" + url + "]";
    }
}
