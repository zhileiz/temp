package edu.upenn.cis.cis455.crawler.crawler.ms2;

import edu.upenn.cis.cis455.model.Channel;
import edu.upenn.cis.cis455.xpathengine.*;
import edu.upenn.cis.cis455.xpathengine.occurrence.DomToOccurrence;
import edu.upenn.cis.cis455.xpathengine.parser.XPath;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;
import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static edu.upenn.cis.cis455.crawler.crawler.ms2.StormConstants.FieldNames.*;

public class DomParserBolt extends StorageAccessorBolt {
    private OutputCollector collector;

    Fields schema = new Fields(EVENT);

    String executorId = UUID.randomUUID().toString();

    Map<String ,String> xpathToChannel  = new HashMap<>();

    @Override
    public void cleanup() { }

    @Override
    public void execute(Tuple input) {
        XPathEngine engine = XPathEngineFactory.getXPathEngine();
        String type = input.getStringByField(TYPE);
        String content = input.getStringByField(CONTENT);
        String date = input.getStringByField(DATE);
        String base = input.getStringByField(BASE);
        System.out.println("got " + base + " of " + type);
        if (type.contains("xml")) {
            System.out.println("[🖨 XML: ] Received XML");
            Document doc = null;
            try {
                doc = parseDoc(content);
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean[] results = engine.evaluate(doc);
            String[] paths = engine.getPaths();
            for (int i = 0; i < results.length; i++) {
                if (results[i]) {
                    getDB().addDocumentToChannel(xpathToChannel.get(paths[i]), base);
                }
            }
            getDB().addDocument(base, content, date);
        }
    }

    private Document parseDoc(String rawDoc) throws Exception {
        return DomToOccurrence.getDomNode(rawDoc);
    }

    private void populateChannels() {
        List<Channel> channels = getDB().getAllChannels();
        for (Channel c : channels) {
            xpathToChannel.put(c.getXpath(), c.getName());
        }
    }

    @Override
    public void prepare(Map<String, String> stormConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        populateChannels();
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
