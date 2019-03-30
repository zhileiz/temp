package edu.upenn.cis.cis455.crawler.crawler.ms2;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.spout.IRichSpout;
import edu.upenn.cis.stormlite.spout.SpoutOutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Values;

import java.util.Map;
import java.util.UUID;

import static edu.upenn.cis.cis455.crawler.crawler.ms2.StormConstants.FieldNames.*;


public class CrawlerSpout implements IRichSpout {

    SpoutOutputCollector collector;

    String executorId = UUID.randomUUID().toString();

    @Override
    public void open(Map<String, String> config, TopologyContext topo, SpoutOutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void close() { }

    @Override
    public void nextTuple() {
        String url = SharedInfo.getInstance().pullUrl();
        if (url != null) {
            collector.emit(new Values<>(url));
        }
    }

    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);
    }

    @Override
    public String getExecutorId() {
        return executorId;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields(URL));
    }
}
