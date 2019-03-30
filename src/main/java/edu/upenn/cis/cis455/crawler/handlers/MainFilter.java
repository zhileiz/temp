package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.crawler.utils.Constants;
import edu.upenn.cis.cis455.model.User;
import spark.Filter;
import spark.Request;
import spark.Response;

import static edu.upenn.cis.cis455.crawler.utils.Constants.Pages.*;
import static edu.upenn.cis.cis455.crawler.utils.Constants.Paths.*;
import static edu.upenn.cis.cis455.crawler.utils.RequestUtils.*;
import static spark.Spark.halt;

public class MainFilter implements Filter {
    @Override
    public void handle(Request req, Response res) throws Exception {
        if (!req.pathInfo().equals(LOGIN_PAGE) &&
                !req.pathInfo().equals(LOGIN) &&
                !req.pathInfo().equals(REGISTER) &&
                !req.pathInfo().equals(REGISTER_PAGE)) {
            User user = getAuthenticatedUser(req);
            if(user == null) {
                res.redirect(REGISTER_PAGE);
                halt();
            }
        }
    }
}
