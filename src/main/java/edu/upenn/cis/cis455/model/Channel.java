package edu.upenn.cis.cis455.model;

import java.util.ArrayList;
import java.util.List;

public class Channel {

    private String name;
    private String xpath;
    private List<String> documents;

    public Channel(ChannelKey channel, ChannelData data) {
        this.name = channel.getChannelName();
        this.xpath = data.getXpath();
        this.documents = new ArrayList<>();
        for (String s : data.getFiles()) {
            documents.add(s);
        }
    }

    public String getName() {
        return name;
    }

    public String getXpath() {
        return xpath;
    }

    public List<String> getDocuments() {
        return documents;
    }
}
