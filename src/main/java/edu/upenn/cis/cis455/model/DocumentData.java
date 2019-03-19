package edu.upenn.cis.cis455.model;

import java.io.Serializable;

public class DocumentData implements Serializable {
    String md5Hash;
    String rawContent;
    String lastUpdatedTime;

    public DocumentData(String md5Hash, String rawContent, String time) {
        this.md5Hash = md5Hash;
        this.rawContent = rawContent;
        this.lastUpdatedTime = time;
    }

    public String getMd5Hash() {
        return md5Hash;
    }

    public String getLastCheckedTime() {
        return lastUpdatedTime;
    }

    public String getRawContent() {
        return rawContent;
    }

    @Override
    public String toString() {
        return "[DocumentData: " +
                "md5Hash=" + md5Hash +
                " lastCheckedTime=" + lastUpdatedTime +
                " rawContent=" + rawContent + "]";
    }
}
