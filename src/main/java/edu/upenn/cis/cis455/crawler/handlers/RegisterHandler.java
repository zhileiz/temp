package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.model.User;
import edu.upenn.cis.cis455.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;

import static edu.upenn.cis.cis455.crawler.utils.Constants.Paths.*;
import static edu.upenn.cis.cis455.crawler.utils.RequestUtils.*;

public class RegisterHandler implements Route {

    Logger logger = LogManager.getLogger(RegisterHandler.class);

    StorageInterface db;

    public RegisterHandler(StorageInterface db) { this.db = db; }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        String firstName = req.queryParams("firstName");
        String lastName = req.queryParams("lastName");
        String username = req.queryParams("username");
        String password = req.queryParams("password");
        logger.debug("REGISTER: received " + firstName + " " + lastName + " " + username + " " + password);
        if (firstName == null || lastName == null || username == null || password == null) {
            res.redirect(REGISTER);
        } else {
            int result = db.addUser(username, firstName, lastName, password);
            if (result > 0) {
                User u = new User(219, username, password);
                addAuthenticatedUser(req, u);
                res.redirect(MAIN_PAGE);
            } else {
                res.redirect(REGISTER);
            }
        }
        return null;
    }
}
