package edu.upenn.cis.cis455.crawler.handlers;

import java.util.ArrayList;
import java.util.List;

public class SimpleTable {

    String[] headers;
    List<String[]> rows;

    public SimpleTable(String...headers) {
        this.headers = headers;
        this.rows = new ArrayList<>();
    }

    public void addRow(String...row) {
        rows.add(row);
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table><tr>");
        for (String s : headers) {
            sb.append("<th>" + s + "</th>");
        }
        sb.append("</tr>");
        for (String[] row : rows) {
            sb.append("<tr>");
            for (String s : row) {
                sb.append("<td>" + s + "</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb.toString();
    }
}
