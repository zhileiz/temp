package edu.upenn.cis.cis455.crawler.crawler.ms2;

import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static edu.upenn.cis.cis455.crawler.crawler.ms2.StormConstants.Crawler.*;

public class SharedInfo {

    Logger logger = LogManager.getLogger(LinkFilterBolt.class);

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

    private Crawler master;
    private int maxContentLength;
    private Integer maxDocumentCount = 0;
    private Integer activeWorkersCount = 0;
    private Integer countDown = 1000;

    private SharedInfo() {
        frontierQueue = new ArrayDeque<>();
        robotsInfo = new HashMap<>();
        lastVisitedTimes = new HashMap<>();
    }

    public void setMaster(Crawler master) {
        this.master = master;
    }

    public void setMaxDocumentCount(int max) {
        this.maxDocumentCount = max;
    }

    public void setMaxContentLength(int max) {
        this.maxContentLength = max;
    }

    public void addUrl(String url) {
        synchronized (frontierQueue) {
            URLInfo info = new URLInfo(url);
            frontierQueue.add(info);
        }
    }

    public String pullUrl() {
        if (shouldExit()) {
            if (countDown > 0) {
                countDown --;
                return null;
            }
            master.shutDown();
            return null;
        } else {
            synchronized (frontierQueue) {
                if (frontierQueue.isEmpty()) {
                    return null;
                }
                URLInfo info = frontierQueue.poll();
                if (deferCrawl(info.getHostName())) {
                    frontierQueue.add(info);
                    return null;
                } else {
                    return info.getRawUrl();
                }
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

    private boolean shouldExit() {
        synchronized (frontierQueue) {
            synchronized (activeWorkersCount) {
                return (frontierQueue.isEmpty() && activeWorkersCount == 0) || maxDocumentCount < 1;
            }
        }
    }

    public void declareWorking(boolean isWorking, String worker, String uid) {
        synchronized (activeWorkersCount) {
            if (isWorking) {
                logger.debug("[👨‍💻 working: " + worker + "] " + activeWorkersCount + " | " + uid );
                activeWorkersCount++;
            } else {
                logger.debug("[🚽 rest: : " + worker + "] " + activeWorkersCount + " | " + uid);
                activeWorkersCount--;
            }
        }
    }

    public void declareDownloaded() {
        synchronized (maxDocumentCount) {
            maxDocumentCount--;
        }
    }







}
