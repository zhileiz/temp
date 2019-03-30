package edu.upenn.cis.cis455.crawler.handlers;

import com.sleepycat.collections.StoredSortedMap;
import edu.upenn.cis.cis455.model.DocumentData;
import edu.upenn.cis.cis455.storage.StorageInterface;
import org.apache.commons.lang3.StringEscapeUtils;
import spark.Request;
import spark.Response;
import spark.Route;

import javax.swing.text.View;
import java.util.List;

public class LookupHandler implements Route {

    private StorageInterface store;

    public LookupHandler(StorageInterface sb) {
        store = sb;
    }

    @Override
    public Object handle(Request request, Response response) {
        TemplateView view = new TemplateView();
        String url = request.queryParams("url");
        if (url == null) {
            view.insertElement(new ViewElement("h2", null, "Please provide a url"));
            return view.render();
        }
        DocumentData doc = (DocumentData) store.getDocument(url);
        if (doc != null) {
            view.insertElement(new ViewElement("h2", null, "Document Found:"));
            ViewElement document = new ViewElement(StringEscapeUtils.escapeHtml4(doc.getRawContent()));
            view.insertElement(new ViewElement("pre", null, document));
        } else {
            view.insertElement(new ViewElement("h2", null, "Document not found. Do you mean one of these?"));
            ViewElement list = new ViewElement("ul", "collection", "");
            List<String> docList = store.getAllDocumentURLs();
            for (String s : docList) {
                String pathInfo = request.pathInfo() + "?url=";
                ViewElement path = new ViewElement(pathInfo);
                ViewElement link = new ViewElement("\n<a href=\"" + pathInfo + s + "\">" + s + "</a>");
                list.insertElement(new ViewElement("li", "collection-item", path, link));
            }
            view.insertElement(list);
        }
        return view.render();
    }
}
