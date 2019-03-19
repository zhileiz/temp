package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class ContentHashData implements Serializable {
    String url;

    public ContentHashData(String url) {
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
