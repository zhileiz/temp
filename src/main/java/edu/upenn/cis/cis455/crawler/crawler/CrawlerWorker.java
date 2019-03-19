package edu.upenn.cis.cis455.crawler.crawler;

import edu.upenn.cis.cis455.commonUtil.CommonUtil;
import edu.upenn.cis.cis455.crawler.CrawlMaster;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.info.RequestObj;
import edu.upenn.cis.cis455.crawler.info.ResponseObj;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.utils.Constants;
import edu.upenn.cis.cis455.crawler.utils.CrawlerUtils;
import edu.upenn.cis.cis455.model.DocumentData;
import edu.upenn.cis.cis455.storage.StorageInterface;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.Date;
import java.util.List;

import static edu.upenn.cis.cis455.commonUtil.CommonUtil.getDateFromString;
import edu.upenn.cis.cis455.crawler.utils.Constants;

public class CrawlerWorker extends Thread {

    private boolean shouldContinue;
    private CrawlMaster master;
    private StorageInterface db;
    private CrawlerUrlQueue queue;


    public CrawlerWorker(CrawlMaster master, StorageInterface db, CrawlerUrlQueue queue) {
        this.master = master;
        this.db = db;
        this.queue = queue;
        this.shouldContinue = true;
    }

    @Override
    public void run() {
        while (shouldContinue) {
            URLInfo urlInfo = queue.poll();
            if (urlInfo != null && master.isOKtoCrawl(urlInfo)) {
                master.setWorking(true);
                if (master.deferCrawl(urlInfo.getHostName())) {
                    queue.add(urlInfo);
                } else {
                    /* Check and Crawl */
                    String[] newDoc = headOrGet(urlInfo);
                    /* Parse New Document and save */
                    System.out.println("[ACTIVE:]" + ((Crawler) master).getActiveWorkerCount());
                    parseAndSave(newDoc, urlInfo);
                }
                master.setWorking(false);
            }
            if (master.isDone()) {
                shouldContinue = false;
            }
        }
        master.notifyThreadExited();
    }

    private String[] headOrGet(URLInfo urlInfo) {
        String url = urlInfo.getRawUrl();
        DocumentData doc = (DocumentData) db.getDocument(url);
        String newDoc = null;
        String newDate = null;
        if (doc == null) {
            ResponseObj res = doGet(url);
            newDoc = res.getContent();
            newDate = res.getOrDefault(Constants.HTTPHeaders.DATE, CommonUtil.getCurrentTime());
        } else {
            System.out.println("[ALREADY EXIST] " + urlInfo.getRawUrl());
            ResponseObj res = doHead(url, doc.getLastCheckedTime());
            System.out.println(res);
            if (res.getResponseCode() < 300) {
                res = doGet(url);
                newDoc = res.getContent();
                newDate = res.getOrDefault(Constants.HTTPHeaders.DATE, CommonUtil.getCurrentTime());
            }
        }
        return new String[]{newDoc, newDate};
    }

    private void parseAndSave(String[] newDoc, URLInfo urlInfo) {
        String doc = newDoc[0];
        String date = newDoc[1];
        if (doc != null) {
            List<URLInfo> links = parseDoc(doc);
            if (links != null && links.isEmpty()) {
                for (URLInfo info : links) {
                    if (master.isOKtoParse(info)) {
                        queue.add(urlInfo);
                    }
                }
            }
            System.out.println("[SAVING]" + urlInfo.getRawUrl());
            db.addDocument(urlInfo.getRawUrl(), doc, date);
        }
    }

    private ResponseObj doHead(String url, String date) {
        RequestObj req = new RequestObj(url, Constants.HTTPMethods.HEAD);
        req.addProperty(Constants.HTTPHeaders.IF_MODIFIED_SINCE, date);
        return CrawlerUtils.makeRequest(req);
    }

    private ResponseObj doGet(String url) {
        RequestObj req = new RequestObj(url, Constants.HTTPMethods.GET);
        return CrawlerUtils.makeRequest(req);
    }

    private List<URLInfo> parseDoc(String doc) {
        //TODO
        return null;
    }

}
