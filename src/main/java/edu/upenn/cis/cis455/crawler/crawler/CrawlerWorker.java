package edu.upenn.cis.cis455.crawler.crawler;

import edu.upenn.cis.cis455.commonUtil.CommonUtil;
import edu.upenn.cis.cis455.crawler.CrawlMaster;
import edu.upenn.cis.cis455.crawler.info.RequestObj;
import edu.upenn.cis.cis455.crawler.info.ResponseObj;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.utils.Constants;
import edu.upenn.cis.cis455.crawler.utils.CrawlerUtils;
import edu.upenn.cis.cis455.model.DocumentData;
import edu.upenn.cis.cis455.storage.StorageInterface;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class CrawlerWorker extends Thread {

    private boolean shouldContinue;
    private CrawlMaster master;
    private StorageInterface db;
    private CrawlerUrlQueue queue;
    private long maxContentSize;

    public CrawlerWorker(CrawlMaster master, StorageInterface db, CrawlerUrlQueue queue, int maxContentSize) {
        this.master = master;
        this.db = db;
        this.queue = queue;
        this.shouldContinue = true;
        this.maxContentSize = maxContentSize * 1024 * 1024;
    }

    private class ShouldSleepException extends Exception {
        int time;
        public ShouldSleepException(int i) { time = i; }
    }

    private class ShouldSkipException extends Exception {
        String reason;
        public ShouldSkipException(String reason ) { this.reason = reason; }
    }

    private URLInfo getNextInfo() throws ShouldSleepException {
        synchronized (queue) {
            URLInfo peekInfo = queue.peek();
            if (peekInfo == null) {
//                System.out.println("[" + Thread.currentThread() + " Sleeping due to null peakInfo]");
                throw new ShouldSleepException(1);
            }
            if (!master.isOKtoCrawl(peekInfo)) {
                queue.nonBlockPoll();
                return null;
            } else {
                if (master.deferCrawl(peekInfo.getHostName())) {
//                    System.out.println("[" + Thread.currentThread() + " Sleeping due to defer..]");
                    throw new ShouldSleepException(1);
                } else {
                    return queue.nonBlockPoll();
                }
            }
        }
    }

    private void sleep(int i) {
        if (i > 0) {
            try {
                Thread.sleep(i);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while (shouldContinue) {
            if (master.isDone()) { shouldContinue = false; }
            URLInfo urlInfo;
            try { urlInfo = getNextInfo(); }
            catch (ShouldSleepException e) {
//                System.out.println("[" + Thread.currentThread() + " Sleeping..]");
                sleep(e.time); continue;
            }
            if (urlInfo != null) {
                master.setWorking(true);
                System.out.println("\n\n===============\n [CRAWLING By" + Thread.currentThread() + "...]" + urlInfo.getRawUrl());
                try {
                    /* Check and Crawl */
                    String[] newDoc = headOrGet(urlInfo);
                    /* Parse New Document and save */
                    parseAndSave(newDoc, urlInfo);
                } catch (ShouldSkipException e) {
                    System.out.println(e.reason);
                }
                master.setWorking(false);
            }
        }
        master.notifyThreadExited();
    }

    private boolean shouldParse(String contentType) {
        return contentType != null && contentType.startsWith("text/html");
    }

    private boolean shouldSave(String contentType, String contentLength) {
        long length = -1;
        try {
            length = Long.parseLong(contentLength);
        } catch (Exception e) {
            System.err.println("Failed to parse length");
        }
        System.out.println("Max length is: " + maxContentSize + ", but content size is " + length);
        return contentType != null && contentType.startsWith("text") && length < maxContentSize;
    }

    private String[] headOrGet(URLInfo urlInfo) throws ShouldSkipException {
        String url = urlInfo.getRawUrl();
        DocumentData doc = (DocumentData) db.getDocument(url);
        String newDoc = null;
        String newDate = null;
        String contentType = null;
        if (doc == null) {
            ResponseObj testRes = doHead(url, null);
            contentType = testRes.getOrDefault(Constants.HTTPHeaders.CONTENT_TYPE, "");
            String contentLength = testRes.getOrDefault(Constants.HTTPHeaders.CONTENT_LENGTH, "0");
            if (!shouldSave(contentType, contentLength)) {
                throw new ShouldSkipException("[Skipped] Content Type is: " + contentType +
                                            " and Content Length is: " + contentLength);
            }
            ResponseObj res = doGet(url);
            newDoc = res.getContent();
            newDate = res.getOrDefault(Constants.HTTPHeaders.DATE, CommonUtil.getCurrentTime());
            contentType = res.getOrDefault(Constants.HTTPHeaders.CONTENT_TYPE, "").trim().toLowerCase();
        } else {
            System.out.println("[" + Thread.currentThread() + "ALREADY EXIST] " + urlInfo.getRawUrl());
            ResponseObj res = doHead(url, doc.getLastCheckedTime());
            System.out.println(res);
            if (res.getResponseCode() < 300) {
                res = doGet(url);
                newDoc = res.getContent();
                newDate = res.getOrDefault(Constants.HTTPHeaders.DATE, CommonUtil.getCurrentTime());
            }
            contentType = res.getOrDefault(Constants.HTTPHeaders.CONTENT_TYPE, "").trim().toLowerCase();
        }
        return new String[]{newDoc, newDate, contentType};
    }

    private void parseAndSave(String[] newDoc, URLInfo urlInfo) {
        String doc = newDoc[0];
        String date = newDoc[1];
        String contentType = newDoc[2];
        if (doc != null) {
            if (shouldParse(contentType)) {
                List<URLInfo> links = parseDoc(doc, urlInfo.getRawUrl());
                if (links != null && !links.isEmpty()) {
                    for (URLInfo info : links) {
                        if (master.isOKtoParse(info)) {
                            queue.add(info);
                        }
                    }
                }
            } else {
                System.out.println("Content Type is: '" + contentType + "' so skipped.");
            }
            System.out.println("[" + Thread.currentThread() + "SAVING]" + urlInfo.getRawUrl());
            master.incCount();
            db.addDocument(urlInfo.getRawUrl(), doc, date);
        }
    }

    private ResponseObj doHead(String url, String date) {
        RequestObj req = new RequestObj(url, Constants.HTTPMethods.HEAD);
        if (date != null) {
            req.addProperty(Constants.HTTPHeaders.IF_MODIFIED_SINCE, date);
        }
        return CrawlerUtils.makeRequest(req);
    }

    private ResponseObj doGet(String url) {
        RequestObj req = new RequestObj(url, Constants.HTTPMethods.GET);
        return CrawlerUtils.makeRequest(req);
    }

    private List<URLInfo> parseDoc(String doc, String baseUrl) {
        String[] parts = baseUrl.split("/");
        String lastPart = parts[parts.length - 1];
        String[] pieces = lastPart.split("\\.");
        if (!lastPart.trim().equals("") && pieces.length < 2) {
            baseUrl = baseUrl + "/";
        }
        List<URLInfo> links = new ArrayList<>();
        Document tgt = Jsoup.parse(doc, baseUrl);
        Elements hrefs = tgt.select("a");
        for (Element e : hrefs) {
            if (e.attr("href").startsWith("#")) {
                continue;
            }
            String link = e.attr("abs:href");
            if (link != null && link.length() > 7) {
                URLInfo url = new URLInfo(link);
                if (url.getHostName() != null) {
                    links.add(new URLInfo(link));
                }
            }
        }
        return links;
    }

}
