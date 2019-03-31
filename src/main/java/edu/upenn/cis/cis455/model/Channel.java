package edu.upenn.cis.cis455.model;

import java.util.ArrayList;
import java.util.List;

public class Channel {

    private String name;
    private String xpath;
    private String creator;
    private List<String> documents;

    public Channel(ChannelKey channel, ChannelData data) {
        this.name = channel.getChannelName();
        this.xpath = data.getXpath();
        this.creator = data.getCreator();
        this.documents = new ArrayList<>();
        for (String s : data.getFiles()) {
            documents.add(s);
        }
    }

    public String getName() {
        return name;
    }

    public String getCreator() {
        return creator;
    }

    public String getXpath() {
        return xpath;
    }

    public List<String> getDocuments() {
        return documents;
    }
}
