package edu.upenn.cis.cis455.crawler.info;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class RequestObj {

    String url;
    String method;

    Logger logger = LogManager.getLogger(RequestObj.class);

    public class Property{
        String key, value;
        public Property(String key, String value) {
            this.key = key; this.value = value;
        }
        public String[] toPair() {
            String[] pair = {this.key, this.value};
            return pair;
        }
    }

    private List<Property> properties;

    public RequestObj(String url, String method) {
        this.url = url;
        this.method = method;
        properties = new ArrayList<Property>();
    }

    public RequestObj(URLInfo urlInfo, String method) {
        this.url = urlInfo.getRawUrl();
        this.method = method;
    }

    public URL getUrl() {
        try {
            return new URL(this.url);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void addProperty(String key, String value) {
        properties.add(new Property(key, value));
    }

    public List<String[]> getProperties() {
        List<String[]> list = new ArrayList<>();
        for (Property p : properties) {
            list.add(p.toPair());
        }
        return list;
    }

    public String getMethod() {
        return method;
    }
}
