package edu.upenn.cis.cis455.crawler.utils;

public class Constants {

    public static final String USER_SESSION_ID = "user";

    public class Paths {
        public static final String REGISTER = "/register";
        public static final String MAIN_PAGE = "/welcome";
        public static final String LOGIN = "/login";
        public static final String LOGOUT = "/logout";
    }

    public class Pages {
        public static final String LOGIN_PAGE = "/login-form.html";
        public static final String REGISTER_PAGE = "/register.html";
    }

    public class HTTPMethods {
        public static final String GET = "GET";
        public static final String HEAD = "HEAD";
    }

    public class Robots {
        public static final String USER_AGENT = "user-agent";
        public static final String DISALLOW = "disallow";
        public static final String CRAWL_DELAY = "crawl-delay";
        public static final String ALLOW = "allow";
        public static final String SITEMAP = "sitemap";
    }

    public class HTTPHeaders {
        public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
        public static final String DATE = "Date";
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String CONTENT_LENGTH = "Content-Length";
    }
}
