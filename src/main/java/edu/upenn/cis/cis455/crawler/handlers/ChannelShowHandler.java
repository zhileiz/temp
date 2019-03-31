package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.model.Channel;
import edu.upenn.cis.cis455.storage.StorageFactory;
import spark.Request;
import spark.Response;
import spark.Route;

public class ChannelShowHandler implements Route {

    @Override
    public Object handle(Request req, Response res) {
        String name = req.queryParams("channel");
        try {
            Channel channel = StorageFactory.getDatabaseInstance(name).getChannelByName(name);
            if (channel == null) {
                return "No Such Channel";
            } else {
                TemplateView view = new TemplateView();
                view.insertElement(new ViewElement("h1", null, channel.getName()));
                ViewElement collection = new ViewElement("div", "collection", "");
                for (String s : channel.getDocuments()) {
                    String link = "<a href=\"/lookup?url=" + s + "\" class=\"collection-item\">"+ s + "</a>";
                    collection.insertElement(new ViewElement(link));
                }
                view.insertElement(collection);
                return view.render();
            }
        } catch (Exception e) {
            return "ERRROR: " + e.getMessage();
        }
    }
}
