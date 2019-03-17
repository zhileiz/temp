package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.crawler.Constants;
import edu.upenn.cis.cis455.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import edu.upenn.cis.cis455.storage.StorageInterface;
import spark.Request;
import spark.Filter;
import spark.Response;

import static edu.upenn.cis.cis455.crawler.utils.RequestUtils.getAuthenticatedUser;
import static spark.Spark.halt;


public class LoginFilter implements Filter {
    Logger logger = LogManager.getLogger(LoginFilter.class);

    @Override
    public void handle(Request req, Response res) throws Exception {
        User authUser = getAuthenticatedUser(req);
        if(authUser != null) {
            res.redirect(Constants.Paths.MAIN_PAGE);
            halt();
        } else if (req.requestMethod().equals("GET")) {
            res.redirect(Constants.Pages.LOGIN_PAGE);
            halt();
        }
    }
}
