package edu.upenn.cis.cis455.crawler.handlers;

import spark.Request;
import spark.Response;
import spark.Route;

import static edu.upenn.cis.cis455.crawler.utils.RequestUtils.*;

public class MainHandler implements Route {
    @Override
    public Object handle(Request req, Response res) throws Exception {
        return "<h1>Welcome, " + getAuthenticatedUser(req).getUserName() + ", you are logged in</h1>";
    }
}
