package edu.upenn.cis.cis455.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ChannelData implements Serializable {

    private String xpath;
    private String creator;
    private List<String> files;

    public ChannelData(String xpath, String creator) {
        this.xpath = xpath.trim();
        this.creator = creator;
        this.files = new ArrayList<>();
    }

    public String getXpath() {
        return xpath;
    }

    public String getCreator() { return creator; }

    public List<String> getFiles() {
        return files;
    }

    public void addFile(String url) {
        files.add(url);
    }

    @Override
    public String toString() {
        return "[ChannelData: xpath=" + xpath + "]";
    }


}
