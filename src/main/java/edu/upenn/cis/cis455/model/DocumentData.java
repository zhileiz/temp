package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class DocumentData implements Serializable {
    String rawContent;
    String lastCheckedTime;

    public DocumentData(String rawContent, String time) {
        this.rawContent = rawContent;
        this.lastCheckedTime = time;
    }

    public String getLastCheckedTime() {
        return lastCheckedTime;
    }

    public String getRawContent() {
        return rawContent;
    }

    @Override
    public String toString() {
        return "[DocumentData: " + "lastCheckedTime=" + lastCheckedTime + " rawContent=" + rawContent + "]";
    }
}
