package edu.upenn.cis.cis455.crawler.crawler.ms2;

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

public class LinkExtractorBolt implements IRichBolt {
    private OutputCollector collector;

    Fields schema = new Fields(LINK);

    String executorId = UUID.randomUUID().toString();

    @Override
    public void cleanup() { }

    @Override
    public void execute(Tuple input) {
        String type = input.getStringByField(TYPE);
        String content = input.getStringByField(CONTENT);
        System.out.println(type + ":" + content);
        for (int i = 0; i < 5; i++) {
            String aggregate = "LINK: + " + i + "{type:" + type + ", content:" + content + "}";
            collector.emit(new Values<Object>(aggregate));
        }
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
