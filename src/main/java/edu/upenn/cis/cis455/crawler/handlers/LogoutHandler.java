package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.crawler.Constants;
import spark.Request;
import spark.Response;
import spark.Route;

import static edu.upenn.cis.cis455.crawler.utils.RequestUtils.*;

public class LogoutHandler implements Route {
    @Override
    public Object handle(Request req, Response res) throws Exception {
        removeAuthenticatedUser(req);
        res.redirect(Constants.Paths.LOGIN);
        return null;
    }


}
