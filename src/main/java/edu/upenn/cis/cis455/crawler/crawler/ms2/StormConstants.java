package edu.upenn.cis.cis455.crawler.crawler.ms2;

public class StormConstants {
    public class FieldNames {
        public static final String URL = "URL";
        public static final String TYPE = "TYPE";
        public static final String CONTENT = "CONTENT";
        public static final String LINK = "LINK";
        public static final String DATE = "DATE";
        public static final String BASE = "BASE";
        public static final String FRONTIER = "FRONTIER";
        public static final String EVENT = "EVENT";
    }

    public class Crawler {
        public static final String USER_AGENT = "cis455crawler";
        public static final String USER_AGENT_ALL = "*";
    }

    public class HTTPMethods {
        public static final String GET = "GET";
        public static final String HEAD = "HEAD";
    }

    public class HTTPHeaders {
        public static final String HTTP_IF_MODIFIED_SINCE = "If-Modified-Since";
        public static final String HTTP_DATE = "Date";
        public static final String HTTP_CONTENT_TYPE = "Content-Type";
        public static final String HTTP_CONTENT_LENGTH = "Content-Length";
    }
}
