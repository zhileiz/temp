package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.model.Channel;
import edu.upenn.cis.cis455.storage.StorageFactory;
import spark.Request;
import spark.Response;
import spark.Route;

import java.util.ArrayList;
import java.util.List;

import static edu.upenn.cis.cis455.crawler.utils.RequestUtils.*;

public class MainHandler implements Route {
    @Override
    public Object handle(Request req, Response res) throws Exception {
        TemplateView view = new TemplateView();
        List<Channel> channels = StorageFactory.getDatabaseInstance("").getAllChannels();
        String title = "Welcome, " + getAuthenticatedUser(req).getUserName() + ", you are logged in";
        view.insertElement(new ViewElement("h2", null, title));
        view.insertElement(new ViewElement("h4", null, "All Available Channels:"));
        view.insertElement(getChannels(channels));
        view.insertElement(new ViewElement("h4", null, "Create New Channel:"));
        view.insertElement(getForm());
        return view.render();
    }

    private ViewElement getChannels(List<Channel> channels) {
        SimpleTable table = new SimpleTable("Channel Name", "XPath", "Document Count", "Creator");
        for (Channel ch : channels) {
            String link = "<a href=\"/show?channel=" + ch.getName() + "\">" + ch.getName() + "</a>";
            table.addRow(link, ch.getXpath(), String.valueOf(ch.getDocuments().size()), ch.getCreator());
        }
        return new ViewElement(table.render());
    }

    private ViewElement getForm() {
        SimpleForm form = new SimpleForm("/create", "GET");
        form.addTextInputField("Channel Name", "name");
        form.addTextInputField("Channel XPath", "xpath");
        return new ViewElement(form.render());
    }
}
