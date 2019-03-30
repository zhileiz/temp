package edu.upenn.cis.cis455.crawler.crawler.ms1;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.Queue;

public class CrawlerUrlQueue {

    Logger logger = LogManager.getLogger(CrawlerUrlQueue.class);

    Queue<URLInfo> queue;
    boolean shouldWait;

    public CrawlerUrlQueue() {
        queue = new ArrayDeque<URLInfo>();
        shouldWait = true;
    }

    public synchronized void add(URLInfo urlInfo) {
        if (urlInfo != null) {
            logger.debug("[" + Thread.currentThread() + "ADDED:]" + urlInfo.getRawUrl());
            queue.add(urlInfo);
            this.notifyAll();
        }
    }

    public synchronized URLInfo peek() {
        return queue.peek();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void exit() {
        this.shouldWait = false;
        this.notifyAll();
    }

    public URLInfo nonBlockPoll() {
        return queue.poll();
    }

    public synchronized URLInfo poll() {
        while (shouldWait) {
            if (!queue.isEmpty()) {
                return queue.poll();
            } else {
                try {
                    wait();
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }

}
