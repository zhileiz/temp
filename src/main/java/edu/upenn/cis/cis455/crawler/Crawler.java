package edu.upenn.cis.cis455.crawler;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.net.ssl.HttpsURLConnection;

import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;


public class Crawler implements CrawlMaster {
    static final int NUM_WORKERS = 10;

    public Crawler(String startUrl, StorageInterface db, int size, int count) {
        // TODO: initialize
    }

    ///// TODO: you'll need to flesh all of this out.  You'll need to build a thread
    // pool of CrawlerWorkers etc. and to implement the functions below which are
    // stubs to compile
    
    /**
     * Main thread
     */
    public void start() {}
    
    /**
     * Returns true if it's permissible to access the site right now
     * eg due to robots, etc.
     */
    public boolean isOKtoCrawl(String site, int port, boolean isSecure) { return true; }

    /**
     * Returns true if the crawl delay says we should wait
     */
    public boolean deferCrawl(String site) { return false; }
    
    /**
     * Returns true if it's permissible to fetch the content,
     * eg that it satisfies the path restrictions from robots.txt
     */
    public boolean isOKtoParse(URLInfo url) { return true; }
    
    /**
     * Returns true if the document content looks worthy of indexing,
     * eg that it doesn't have a known signature
     */
    public boolean isIndexable(String content) { return true; }
    
    /**
     * We've indexed another document
     */
    public void incCount() { }
    
    /**
     * Workers can poll this to see if they should exit, ie the
     * crawl is done
     */
    public boolean isDone() { return true; }
    
    /**
     * Workers should notify when they are processing an URL
     */
    public void setWorking(boolean working) {}
    
    /**
     * Workers should call this when they exit, so the master
     * knows when it can shut down
     */
    public void notifyThreadExited() {}
    
    /**
     * Main program:  init database, start crawler, wait
     * for it to notify that it is done, then close.
     */
    public static void main(String args[]) {
        if (args.length < 3 || args.length > 5) {
            System.out.println("Usage: Crawler {start URL} {database environment path} {max doc size in MB} {number of files to index}");
            System.exit(1);
        }
        
        System.out.println("Crawler starting");
        String startUrl = args[0];
        String envPath = args[1];
        Integer size = Integer.valueOf(args[2]);
        Integer count = args.length == 4 ? Integer.valueOf(args[3]) : 100;

        StorageInterface db = null;
        try {
            db = StorageFactory.getDatabaseInstance(envPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        Crawler crawler = new Crawler(startUrl, db, size, count);
        
        System.out.println("Starting crawl of " + count + " documents, starting at " + startUrl);
        crawler.start();
        
        while (!crawler.isDone())
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        // TODO: final shutdown
            
        System.out.println("Done crawling!");
    }

}
