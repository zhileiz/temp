package edu.upenn.cis.cis455.crawler.crawler.ms2;

import edu.upenn.cis.cis455.crawler.info.RequestObj;
import edu.upenn.cis.cis455.crawler.info.ResponseObj;
import edu.upenn.cis.cis455.crawler.info.RobotsTxtInfo;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.utils.Constants;
import edu.upenn.cis.cis455.crawler.utils.CrawlerUtils;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static edu.upenn.cis.cis455.crawler.crawler.ms2.StormConstants.Crawler.*;
import static edu.upenn.cis.cis455.crawler.crawler.ms2.StormConstants.FieldNames.*;

public class LinkFilterBolt implements IRichBolt {
    private OutputCollector collector;

    Fields schema = new Fields(FRONTIER);

    String executorId = UUID.randomUUID().toString();

    @Override
    public void cleanup() { }

    @Override
    public void execute(Tuple input) {
        String url = input.getStringByField(LINK);
        if (isOKtoCrawl(url)) {
            System.out.println("[✅ YES: ]" + url);
            SharedInfo.getInstance().addUrl(url);
        } else {
            System.out.println("[❌ DONT: ]" + url);
        }
    }

    public boolean isOKtoCrawl(String url) {
        String host = (new URLInfo(url)).getHostURL();
        SharedInfo sharedInfo = SharedInfo.getInstance();
        /* add robots info if host is found for the first time */
        if (!sharedInfo.containsRobot(host)) {
            RobotsTxtInfo initialInfo = getRobotsInfo(host);
            sharedInfo.addRobotInfo((new URLInfo(host)).getHostName(), initialInfo);
        }
        /* get disallowed links from list */
        RobotsTxtInfo info = SharedInfo.getInstance().getRobotsInfo(url);
        if (info == null) { return true; }
        ArrayList<String> disallowed = info.getDisallowedLinks(USER_AGENT);
        if (disallowed == null) {
            disallowed = info.getDisallowedLinks(USER_AGENT_ALL);
        }
        /* see if file matches */
        if (disallowed != null) {
            for (String path : disallowed) {
                if ((new URLInfo(url)).getFilePath().startsWith(cleanPath(path))) {
                    return false;
                }
            }
        }
        return true;
    }

    private String cleanPath(String path) {
        if (path.length() < 2)  { return path; }
        if (path.substring(path.length() - 1, path.length()).equals("/")) {
            return path.substring(0, path.length() - 1);
        }
        return path;
    }

    private RobotsTxtInfo getRobotsInfo(String host) {
        String robotURL = host + "/robots.txt";
        RequestObj req = new RequestObj(robotURL, Constants.HTTPMethods.GET);
        ResponseObj res = CrawlerUtils.makeRequest(req);
        if (res.getResponseCode() >= 400) {
            System.out.println("[⚠️ NO ROBOT: ] Failed to fetch ROBOT.txt for " + host);
            return new RobotsTxtInfo();
        }
        return CrawlerUtils.parseRobotsTxt(res.getContent());
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
