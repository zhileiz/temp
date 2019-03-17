package edu.upenn.cis.cis455.crawler.utils;

import edu.upenn.cis.cis455.crawler.Constants;
import edu.upenn.cis.cis455.model.User;
import spark.Request;

public class RequestUtils {
    public static User getAuthenticatedUser(Request request) {
        return request.session().attribute(Constants.USER_SESSION_ID);
    }

    public static void removeAuthenticatedUser(Request request) {
        request.session().removeAttribute(Constants.USER_SESSION_ID);
    }

    public static void addAuthenticatedUser(Request request, User u) {
        request.session().attribute(Constants.USER_SESSION_ID, u);
    }
}
