package edu.upenn.cis.cis455.crawler.handlers;

import com.sleepycat.collections.StoredSortedMap;
import edu.upenn.cis.cis455.model.DocumentData;
import edu.upenn.cis.cis455.storage.StorageInterface;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.List;

public class LookupHandler implements Route {

    private StorageInterface store;

    public LookupHandler(StorageInterface sb) {
        store = sb;
    }

    @Override
    public Object handle(Request request, Response response) {
        String url = request.queryParams("url");
        if (url == null) {
            return "<h1>Please provide a url</h1>";
        }
        DocumentData doc = (DocumentData) store.getDocument(url);
        if (doc != null) {
            String start = " <h1>This is the document we crawled:</h1>\n";
            StringBuilder sb = new StringBuilder();
            sb.append(start);
            sb.append(doc.getRawContent());
            return sb.toString();
        } else {
            String title = "<h1>Document not found. Do you mean one of these?</h1>";
            List<String> list = store.getAllDocumentURLs();
            StringBuilder sb = new StringBuilder();
            sb.append(title);
            for (String s : list) {
                sb.append("<li>" + s + "</li>\n");
            }
            return sb.toString();
        }
    }
}
