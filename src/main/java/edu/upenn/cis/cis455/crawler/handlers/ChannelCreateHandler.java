package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.model.User;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import spark.Request;
import spark.Response;
import spark.Route;

import static edu.upenn.cis.cis455.crawler.utils.Constants.USER_SESSION_ID;

public class ChannelCreateHandler implements Route {

    @Override
    public Object handle(Request req, Response res) {
        String channel = req.params("name");
        String name = req.queryParams("name");
        String user = ((User) req.session().attribute(USER_SESSION_ID)).getUserName();
        System.out.println("[👨‍🍳 User:]" + req.session().attribute(USER_SESSION_ID));
        System.out.println("[👨‍🍳 User:]" + user);
        String path = req.queryParams("xpath");
        if (channel != null && channel.trim().length() > 0 && path != null && path.trim().length() > 0) {
            try {
                StorageInterface db = StorageFactory.getDatabaseInstance("");
                db.addChannel(channel, path, user);
                return "Successfully created channel";
            } catch (Exception e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }
        return "Failed to Register Route.";
    }
}
