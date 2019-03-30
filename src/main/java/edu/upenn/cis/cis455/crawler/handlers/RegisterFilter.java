package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.crawler.utils.Constants;
import edu.upenn.cis.cis455.model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Filter;
import spark.Request;
import spark.Response;

import static edu.upenn.cis.cis455.crawler.utils.Constants.HTTPMethods.*;
import static edu.upenn.cis.cis455.crawler.utils.Constants.Pages.*;
import static edu.upenn.cis.cis455.crawler.utils.Constants.Paths.*;
import static edu.upenn.cis.cis455.crawler.utils.RequestUtils.getAuthenticatedUser;
import static spark.Spark.halt;

public class RegisterFilter implements Filter {
    Logger logger = LogManager.getLogger(RegisterFilter.class);

    @Override
    public void handle(Request req, Response res) throws Exception {
        User authUser = getAuthenticatedUser(req);
        if(authUser != null) {
            res.redirect(MAIN_PAGE);
            halt();
        } else if (req.requestMethod().equals(GET)) {
            res.redirect(REGISTER_PAGE);
            halt();
        }
    }
}
