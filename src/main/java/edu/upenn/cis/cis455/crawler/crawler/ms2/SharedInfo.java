package edu.upenn.cis.cis455.crawler.crawler.ms2;

import edu.upenn.cis.cis455.crawler.CrawlerMS2;
import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.utils.Constants;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static edu.upenn.cis.cis455.crawler.crawler.ms2.StormConstants.Crawler.*;

public class SharedInfo {

    private static SharedInfo instance;

    public static SharedInfo getInstance() {
        if (instance == null) {
            instance = new SharedInfo();
        }
        return instance;
    }

    private Queue<URLInfo> frontierQueue;
    private Map<String, RobotsTxtInfo> robotsInfo;
    private Map<String, LocalDateTime> lastVisitedTimes;

    private CrawlerMS2 master;
    private int maxContentLength;
    private int maxDocumentCount;

    private SharedInfo() {
        frontierQueue = new ArrayDeque<>();
        robotsInfo = new HashMap<>();
        lastVisitedTimes = new HashMap<>();
    }

    public void setMaster(CrawlerMS2 master) {
        this.master = master;
    }

    public void addUrl(String url) {
        synchronized (frontierQueue) {
            URLInfo info = new URLInfo(url);
            frontierQueue.add(info);
        }
    }

    public String pullUrl() {
        synchronized (frontierQueue) {
            if (frontierQueue.isEmpty()) { return null; }
            URLInfo info = frontierQueue.poll();
            if (deferCrawl(info.getHostName())) {
                frontierQueue.add(info);
                return null;
            } else {
                return info.getRawUrl();
            }
        }
    }

    public synchronized boolean deferCrawl(String site) {
        /* No delay if no record */
        LocalDateTime last = null;
        try {
            last = lastVisitedTimes.get(site);
        } catch (Exception e) {
            e.printStackTrace();
        }
        LocalDateTime now = LocalDateTime.now();
        if (last == null || robotsInfo.get(site) == null) {
            lastVisitedTimes.put(site, now);
            return false;
        }
        /* No delay if no requirement */
        RobotsTxtInfo robot = robotsInfo.get(site);
        long delay = robot.getCrawlDelay(USER_AGENT);
        delay = delay / 2;
        if (delay < 0) {
            delay = robot.getCrawlDelay(USER_AGENT_ALL);
        }
        /* Check Time*/
        LocalDateTime temp = LocalDateTime.from(last);
        now = LocalDateTime.now();
        long timeElapsed = temp.until(now, ChronoUnit.SECONDS);
        if (timeElapsed < delay) { return true; }
        /* record time */
        lastVisitedTimes.put(site, now);
        return false;
    }

    public RobotsTxtInfo getRobotsInfo(String url) {
        String host = (new URLInfo(url)).getHostName();
        synchronized (robotsInfo) {
            return robotsInfo.get(host);
        }
    }

    public boolean containsRobot(String url) {
        String host = (new URLInfo(url)).getHostName();
        synchronized (robotsInfo) {
            return robotsInfo.containsKey(host);
        }
    }

    public void addRobotInfo(String host, RobotsTxtInfo info) {
        synchronized (robotsInfo) {
            robotsInfo.put(host, info);
        }
    }







}
