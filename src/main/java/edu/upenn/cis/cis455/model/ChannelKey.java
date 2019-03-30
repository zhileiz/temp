package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class ChannelKey implements Serializable {

    private String channelName;

    public ChannelKey(String channelName) {
        this.channelName = channelName.trim();
    }

    public String getChannelName() {
        return channelName;
    }

    @Override
    public String toString() {
        return "[ChannelKey: channelName=" + channelName + "]";
    }


}

