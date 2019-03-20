package edu.upenn.cis.cis455.crawler.utils;


import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.info.RequestObj;
import edu.upenn.cis.cis455.crawler.info.ResponseObj;
import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class CrawlerUtils {

    private static Logger logger = LogManager.getLogger(CrawlerUtils.class);

    public static RobotsTxtInfo getRobotsInfo(URLInfo urlInfo) {
        String robotURL = urlInfo.getHostURL() + "/robots.txt";
        RequestObj req = new RequestObj(robotURL, Constants.HTTPMethods.GET);
        ResponseObj res = makeRequest(req);
        return parseRobotsTxt(res.getContent());
    }

    public static ResponseObj makeRequest(RequestObj req) {
        try {
            logger.debug(req.getUrl());
            HttpURLConnection con = (HttpURLConnection) req.getUrl().openConnection();
            con.setRequestMethod(req.getMethod());
            for (String[] property : req.getProperties()) {
                logger.debug("REQ PROP: " + property[0] + " : " + property[1]);
                con.setRequestProperty(property[0], property[1]);
            }
            return new ResponseObj(con);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static RobotsTxtInfo parseRobotsTxt(String content) {
        try {
            RobotsTxtInfo info = new RobotsTxtInfo();
            String[] lines = content.split("\n");
            String currAgent = null;
            for (String line : lines) {
                if (!line.trim().isEmpty() && !line.trim().startsWith("#")) {
                    String key = line.substring(0, line.indexOf(":")).trim().toLowerCase();
                    String value = line.substring(line.indexOf(":") + 1, line.length()).trim();
                    if (key.trim().isEmpty() || value.isEmpty()) {
                        continue;
                    }
                    String tempAgent = addKeyValuePair(info, key, value, currAgent);
                    currAgent = tempAgent == null ? currAgent : tempAgent;
                }
            }
            return info;
        } catch (Exception e) {
            logger.debug("Failed to parse Robots.TXT");
            return null;
        }
    }

    private static String addKeyValuePair(RobotsTxtInfo tgt, String key, String value, String agent) {
        switch (key) {
            case Constants.Robots.ALLOW:
                tgt.addAllowedLink(agent, value);
                return null;
            case Constants.Robots.DISALLOW:
                tgt.addDisallowedLink(agent, value);
                return null;
            case Constants.Robots.CRAWL_DELAY:
                try { tgt.addCrawlDelay(agent, Integer.parseInt(value)); }
                catch (Exception e) { e.printStackTrace(); }
                return null;
            case Constants.Robots.SITEMAP:
                tgt.addSitemapLink(value);
                return null;
            case Constants.Robots.USER_AGENT:
                tgt.addUserAgent(value);
                return value;
            default:
                return null;
        }
    }


}
