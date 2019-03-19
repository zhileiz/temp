package edu.upenn.cis.cis455.crawler.crawler;

import edu.upenn.cis.cis455.crawler.info.URLInfo;

import java.util.ArrayDeque;
import java.util.Queue;

public class CrawlerUrlQueue {
    Queue<URLInfo> queue;
    boolean shouldWait;

    public CrawlerUrlQueue() {
        queue = new ArrayDeque<URLInfo>();
        shouldWait = true;
    }

    public synchronized void add(URLInfo urlInfo) {
        queue.add(urlInfo);
        this.notifyAll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public void exit() {
        this.shouldWait = false;
        this.notifyAll();
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
