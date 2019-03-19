package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.crawler.utils.Constants;
import edu.upenn.cis.cis455.model.User;
import spark.Filter;
import spark.Request;
import spark.Response;

import static edu.upenn.cis.cis455.crawler.utils.RequestUtils.*;
import static spark.Spark.halt;

public class MainFilter implements Filter {
    @Override
    public void handle(Request req, Response res) throws Exception {
        if (!req.pathInfo().equals(Constants.Pages.LOGIN_PAGE) &&
                !req.pathInfo().equals(Constants.Paths.LOGIN) &&
                !req.pathInfo().equals(Constants.Paths.REGISTER) &&
                !req.pathInfo().equals(Constants.Pages.REGISTER_PAGE)) {
            User user = getAuthenticatedUser(req);
            if(user == null) {
                res.redirect(Constants.Pages.REGISTER_PAGE);
                halt();
            }
        }
    }
}
