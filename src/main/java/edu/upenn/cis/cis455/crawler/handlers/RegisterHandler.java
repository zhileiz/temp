package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.model.User;
import edu.upenn.cis.cis455.storage.StorageInterface;
import spark.Request;
import spark.Response;
import spark.Route;
import static edu.upenn.cis.cis455.crawler.utils.RequestUtils.*;

public class RegisterHandler implements Route {

    StorageInterface db;

    public RegisterHandler(StorageInterface db) { this.db = db; }

    @Override
    public Object handle(Request req, Response res) throws Exception {
        String firstName = req.queryParams("firstName");
        String lastName = req.queryParams("lastName");
        String username = req.queryParams("username");
        String password = req.queryParams("password");
        System.out.println("REGISTER: received " + firstName + " " + lastName + " " + username + " " + password);
        if (firstName == null || lastName == null || username == null || password == null) {
            res.redirect("/register");
        } else {
            int result = db.addUser(username, firstName, lastName, password);
            if (result > 0) {
                User u = new User(219, username, password);
                addAuthenticatedUser(req, u);
                res.redirect("/welcome");
            } else {
                res.redirect("/register.html");
            }
        }
        return null;
    }
}
