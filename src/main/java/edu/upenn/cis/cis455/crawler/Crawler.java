package edu.upenn.cis.cis455.crawler;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.HttpsURLConnection;

import edu.upenn.cis.cis455.commonUtil.CommonUtil;
import edu.upenn.cis.cis455.crawler.crawler.CrawlerUrlQueue;
import edu.upenn.cis.cis455.crawler.crawler.CrawlerWorker;
import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.utils.CrawlerUtils;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;


public class Crawler implements CrawlMaster {
    static final int NUM_WORKERS = 10;
    static final String USER_AGENT = "cis455crawler";
    static final String USER_AGENT_ALL = "*";

    Logger logger = LogManager.getLogger(Crawler.class);

    CrawlerUrlQueue urlQueue;
    HashMap<String, RobotsTxtInfo> robotsInfo;
    HashMap<String, LocalDateTime> lastVisitedTimes;
    CrawlerWorker[] workers;
    StorageInterface db;

    int maxContentLength;
    int maxDocumentCount;
    int activeWorkerCount;
    int threadCount;
    boolean hasStarted;

    public Crawler(String startUrl, StorageInterface db, int size, int count) {
        this.db = db;
        this.maxContentLength = size;
        this.maxDocumentCount = count;
        this.lastVisitedTimes = new HashMap<>();
        URLInfo initialUrl = new URLInfo(startUrl);
        initializeRobotsInfo(initialUrl);
        initializeThreadPool(initialUrl, NUM_WORKERS);
    }

    private void initializeThreadPool(URLInfo initialUrl, int numWorkers) {
        urlQueue = new CrawlerUrlQueue();
        urlQueue.add(initialUrl);
        workers = new CrawlerWorker[numWorkers];
        for (int i = 0; i < numWorkers; i++) {
            workers[i] = new CrawlerWorker(this, db, urlQueue, maxContentLength);
        }
    }

    private void initializeRobotsInfo(URLInfo initialUrl) {
        robotsInfo = new HashMap<>();
        RobotsTxtInfo initialInfo = CrawlerUtils.getRobotsInfo(initialUrl);
        robotsInfo.put(initialUrl.getHostName(), initialInfo);
    }

    ///// TODO: you'll need to flesh all of this out.  You'll need to build a thread
    // pool of CrawlerWorkers etc. and to implement the functions below which are
    // stubs to compile
    
    /**
     * Main thread
     */
    public void start() {
        for (int i = 0; i < workers.length; i++) {
            workers[i].start();
        }
        this.threadCount = workers.length;
        this.hasStarted = true;
    }
    
    /**
     * Returns true if it's permissible to access the site right now
     * eg due to robots, etc.
     */
    @Override
    public boolean isOKtoCrawl(URLInfo info) {
        synchronized (robotsInfo) {
            if (!robotsInfo.containsKey(info.getHostName())) {
                robotsInfo.put(info.getHostName(), CrawlerUtils.getRobotsInfo(info));
            }
            RobotsTxtInfo robot = robotsInfo.get(info.getHostName());
            if (robot == null) { return true; }
            ArrayList<String> disallowed = robot.getDisallowedLinks(USER_AGENT);
            if (disallowed == null) { disallowed = robot.getDisallowedLinks(USER_AGENT_ALL); }
            if (disallowed != null) {
                for (String path : disallowed) {
                    if (info.getFilePath().startsWith(cleanPath(path))) {
                        logger.debug("DISALLOWED '" + path + "' by '" + info.getFilePath() + "'");
                        return false;
                    }
                }
            } else {
                logger.debug("NO USER AGENT FOUND!");
            }
            return true;
        }
    }

    private String cleanPath(String path) {
        if (path.length() < 2)  { return path; }
        if (path.substring(path.length() - 1, path.length()).equals("/")) {
            return path.substring(0, path.length()-1);
        } else {
            return path;
        }
    }

    /**
     * Returns true if the crawl delay says we should wait
     */
    @Override
    public boolean deferCrawl(String site) {
        synchronized (lastVisitedTimes) {
            LocalDateTime last = lastVisitedTimes.get(site);
            LocalDateTime now = LocalDateTime.now();
            if (last == null) {
                lastVisitedTimes.put(site, now);
                return false;
            } else {
                RobotsTxtInfo robot = robotsInfo.get(site);
                if (robot == null) { return false; }
                long delay = robot.getCrawlDelay(USER_AGENT);
                if (delay < 0) { delay = robot.getCrawlDelay(USER_AGENT_ALL); }
                LocalDateTime temp = LocalDateTime.from(last);
                now = LocalDateTime.now();
                long timeElapsed = temp.until(now, ChronoUnit.SECONDS);
                if (timeElapsed < delay) { return true; }
                else {
                    logger.debug("[ ALLOWING ] " + site);
                    lastVisitedTimes.put(site, now);
                    return false;
                }
            }
        }
    }
    
    /**
     * Returns true if it's permissible to fetch the content,
     * eg that it satisfies the path restrictions from robots.txt
     */
    @Override
    public boolean isOKtoParse(URLInfo url) { return true; }
    
    /**
     * Returns true if the document content looks worthy of indexing,
     * eg that it doesn't have a known signature
     */
    @Override
    public boolean isIndexable(String content) { return true; }
    
    /**
     * We've indexed another document
     */
    @Override
    public void incCount() {
        synchronized (this) {
            maxDocumentCount --;
        }
    }
    
    /**
     * Workers can poll this to see if they should exit, ie the
     * crawl is done
     */
    @Override
    public boolean isDone() {
        if (hasStarted) {
            return (urlQueue.isEmpty() && activeWorkerCount == 0) || maxDocumentCount == 0;
        }
        return false;
    }
    
    /**
     * Workers should notify when they are processing an URL
     */
    @Override
    public void setWorking(boolean working) {
        synchronized (this) {
            if (working) {
                activeWorkerCount++;
            }
            else {
                activeWorkerCount--;
            }
        }
    }

    public int getActiveWorkerCount() {
        return activeWorkerCount;
    }

    /**
     * Workers should call this when they exit, so the master
     * knows when it can shut down
     */
    @Override
    public void notifyThreadExited() {
        synchronized (this) {
            threadCount --;
        }
    }

    @Override
    public void shutDown() {
        logger.debug("[ShutDOWN!] - shutting down");
        synchronized (urlQueue) {
            urlQueue.exit();
        }
        while (threadCount != 0) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        db.close();
    }

    /**
     * Main program:  init database, start crawler, wait
     * for it to notify that it is done, then close.
     */
    public static void main(String args[]) {
        args = checkArgs(args);
        String startUrl = args[0];
        String envPath = args[1];
        Integer size = Integer.valueOf(args[2]);
        Integer count = args.length == 4 ? Integer.valueOf(args[3]) : 100;

        createDBDirectory(envPath);
        StorageInterface db = getDB(envPath);

        args = checkArgs(args);
        createDBDirectory(args[1]);
        StorageInterface database = getDB(args[1]);

        Logger logger = LogManager.getLogger(Crawler.class);
        if (database == null) {
            logger.debug("Cannot Instantiate Database");
            System.exit(1);
        }
        
        Crawler crawler = new Crawler(startUrl, db, size, count);
        crawler.start();

        while (!crawler.isDone()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        crawler.shutDown();
            
        logger.debug("Done crawling!");
    }

    private static StorageInterface getDB(String dbName) {
        try {
            return StorageFactory.getDatabaseInstance(dbName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void createDBDirectory(String dirName) {
        if (!Files.exists(Paths.get(dirName))) {
            try {
                Files.createDirectory(Paths.get(dirName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String[] checkArgs(String[] args) {
        if (args.length < 3 || args.length > 5) {
            args = new String[4];
            args[0] = "https://dbappserv.cis.upenn.edu/crawltest.html";
            args[1] = "./berkeleyDB";
            args[2] = "11";
            args[3] = "1000";
        }
        return args;
    }

}
