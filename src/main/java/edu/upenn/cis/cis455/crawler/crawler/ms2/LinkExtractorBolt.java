package edu.upenn.cis.cis455.crawler.crawler.ms2;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.utils.CrawlerUtils;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static edu.upenn.cis.cis455.crawler.crawler.ms2.StormConstants.FieldNames.*;

public class LinkExtractorBolt extends StorageAccessorBolt {
    private OutputCollector collector;

    Fields schema = new Fields(LINK);

    String executorId = UUID.randomUUID().toString();

    @Override
    public void cleanup() { }

    @Override
    public void execute(Tuple input) {
        SharedInfo.getInstance().declareWorking(true, this.getClass().getName(), executorId);
        String type = input.getStringByField(TYPE);
        String content = input.getStringByField(CONTENT);
        String date = input.getStringByField(DATE);
        String base = input.getStringByField(BASE);
        if (type.contains("html")) {
            List<String> urls = parseDoc(content, base);
            for (String url : urls) {
                collector.emit(new Values<>(url));
            }
            getDB().addDocument(base, content, date);
            SharedInfo.getInstance().declareDownloaded();
        }
        SharedInfo.getInstance().declareWorking(false, this.getClass().getName(), executorId);
    }

    private List<String> parseDoc(String doc, String base) {
        String baseUrl = CrawlerUtils.wrapURL(base);
        List<String> links = new ArrayList<>();
        Document tgt = Jsoup.parse(doc, baseUrl);
        Elements refs = tgt.select("a");
        for (Element e : refs) {
            if (e.attr("href").startsWith("#")) { continue; }
            String link = e.attr("abs:href");
            if (link != null && link.length() > 7) {
                links.add(link);
            }
        }
        return links;
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
