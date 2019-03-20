package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.crawler.utils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;

import static edu.upenn.cis.cis455.crawler.utils.RequestUtils.*;

public class LogoutHandler implements Route {

    Logger logger = LogManager.getLogger(LogoutHandler.class);
    @Override
    public Object handle(Request req, Response res) throws Exception {
        removeAuthenticatedUser(req);
        res.redirect(Constants.Paths.LOGIN);
        return null;
    }


}
