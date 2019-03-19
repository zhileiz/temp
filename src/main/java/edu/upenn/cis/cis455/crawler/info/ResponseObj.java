package edu.upenn.cis.cis455.crawler.info;

import edu.upenn.cis.cis455.crawler.utils.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public class ResponseObj {

    Map<String, List<String>> headers;
    private int responseCode;
    private String responseMessage;
    private String content;

    public ResponseObj(HttpURLConnection conn) {
        this.headers = conn.getHeaderFields();
        try {
            this.responseCode = conn.getResponseCode();
            this.responseMessage = conn.getResponseMessage();
            if (!conn.getRequestMethod().equals(Constants.HTTPMethods.HEAD)) {
                setContent(conn);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setContent(HttpURLConnection conn) throws IOException {
        InputStreamReader reader = new InputStreamReader(conn.getInputStream());
        BufferedReader bufferedReader = new BufferedReader(reader);
        StringBuilder response = new StringBuilder();
        String responseSingle = null;
        while ((responseSingle = bufferedReader.readLine()) != null) {
            response.append(responseSingle + "\n");
        }
        this.content = response.toString();
    }

    public String getContent() {
        return content;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public int getContentLength() {
        try {
            return Integer.parseInt(headers.get("Content-Length").get(0));
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public List<String> getHeaderField(String key) {
        return headers.get(key);
    }

    public String getOrDefault(String key, String defaultVal) {
        List<String> entries = this.getHeaderField(key);
        if (entries != null && entries.size() > 0) {
            return entries.get(0);
        } else {
            return defaultVal;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Status:" + getResponseCode() + "\n");
        sb.append("Message:" + getResponseMessage() + "\n");
        sb.append("Headers:\n");
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            sb.append(entry.getKey() + " : " + entry.getValue() + "\n");
        }
        sb.append("Content:\n");
        sb.append(getContent());
        return sb.toString();
    }
}
