package edu.upenn.cis.cis455.crawler.crawler.ms2;

import edu.upenn.cis.cis455.commonUtil.CommonUtil;
import edu.upenn.cis.cis455.crawler.info.RequestObj;
import edu.upenn.cis.cis455.crawler.info.ResponseObj;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.utils.CrawlerUtils;
import edu.upenn.cis.cis455.model.DocumentData;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;

import java.util.Map;
import java.util.UUID;

import static edu.upenn.cis.cis455.crawler.crawler.ms2.StormConstants.FieldNames.*;
import static edu.upenn.cis.cis455.crawler.crawler.ms2.StormConstants.HTTPHeaders.*;
import static edu.upenn.cis.cis455.crawler.crawler.ms2.StormConstants.HTTPMethods.*;

public class DocumentFetcherBolt extends StorageAccessorBolt {

    private OutputCollector collector;

    Fields schema = new Fields(TYPE, CONTENT, DATE, BASE);

    String executorId = UUID.randomUUID().toString();

    private class RawDocument {
        public String date, type, content;
        public RawDocument() {date = null; type = null; content = null;};
        public RawDocument(String type, String content, String date) {
            this.type = type; this.content = content; this.date = date;
        }
    }

    @Override
    public void cleanup() { }

    @Override
    public void execute(Tuple input) {
        String url = input.getStringByField(URL);
        System.out.println("Crawling " + url);
        if (shouldGet(url)) {
            RawDocument result = doGet(url);
            collector.emit(new Values<>(result.type, result.content, result.date, url));
        }
    }

    private boolean shouldGet(String url) {
        ResponseObj res = doHead(url, getSavedDate(url));
        if (res.getResponseCode() >= 300) {
            System.out.println("[🗓 NO CHANGE: ] document wasn't modified.");
            return false;
        }
        String contentType = res.getOrDefault(HTTP_CONTENT_TYPE, "");
        String contentLength = res.getOrDefault(HTTP_CONTENT_LENGTH, "0");
        return shouldDownload(contentType, contentLength);
    }

    private ResponseObj doHead(String url, String date) {
        RequestObj req = new RequestObj(url, HEAD);
        if (date != null) {
            req.addProperty(HTTP_IF_MODIFIED_SINCE, date);
        }
        return CrawlerUtils.makeRequest(req);
    }

    private RawDocument doGet(String url) {
        RequestObj req = new RequestObj(url, GET);
        ResponseObj res = CrawlerUtils.makeRequest(req);
        String content = res.getContent();
        String type = res.getOrDefault(HTTP_CONTENT_TYPE, "");
        String date = res.getOrDefault(HTTP_DATE, CommonUtil.getCurrentTime());
        if (content == null || type == null || date == null) { return null; }
        return new RawDocument(type, content, date);
    }

    private boolean shouldDownload(String contentType, String contentLength) {
        long length = -1;
        try {
            length = Long.parseLong(contentLength);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(contentType);
        return contentType != null && (contentType.startsWith("text") || contentType.contains("xml"));
    }

    private String getSavedDate(String url) {
        StorageInterface db = getDB();
        if (db == null) { return null; }
        DocumentData doc = (DocumentData) db.getDocument(url);
        if (doc == null) { return null; }
        return doc.getLastCheckedTime();
    }

    @Override
    public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);
    }

    @Override
    public Fields getSchema() { return schema; }

    @Override
    public String getExecutorId() { return executorId; }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(schema);
    }
}
