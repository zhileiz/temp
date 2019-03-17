package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.model.User;
import spark.Request;
import spark.Route;
import spark.Response;
import spark.HaltException;
import spark.Session;
import edu.upenn.cis.cis455.storage.StorageInterface;

import static edu.upenn.cis.cis455.crawler.Constants.USER_SESSION_ID;

public class LoginHandler implements Route {

    StorageInterface db;
    
    public LoginHandler(StorageInterface db) {
        this.db = db;
    }

    @Override
    public String handle(Request req, Response res) throws HaltException {
        String username = req.queryParams("username");
        String password = req.queryParams("password");
        System.out.println("LOGIN: received " + username + " " + password);
        if (username == null || password == null) {
            res.redirect("/register");
        } else if (db.getSessionForUser(username, password)) {
            User u = new User(219, username, password);
            addAuthenticatedUser(req, u);
            res.redirect("/welcome");
        } else {
            System.err.println("Invalid credentials");
            res.redirect("/login");
        }
        return null;
    }

    private void addAuthenticatedUser(Request request, User u) {
        request.session().attribute(USER_SESSION_ID, u);
    }
}
