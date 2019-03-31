package edu.upenn.cis.cis455.crawler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

import edu.upenn.cis.cis455.crawler.crawler.ms2.*;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.stormlite.Config;
import edu.upenn.cis.stormlite.LocalCluster;
import edu.upenn.cis.stormlite.Topology;
import edu.upenn.cis.stormlite.TopologyBuilder;
import edu.upenn.cis.stormlite.tuple.Fields;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class CrawlerMS2 implements CrawlMaster {

    private static final String SPOUT = "SPOUT";
    private static final String FETCHER_BOLT = "FETCHER_BOLT";
    private static final String EXTRACTOR_BOLT = "EXTRACTOR_BOLT";
    private static final String PARSER_BOLT = "PARSER_BOLT";
    private static final String FILTER_BOLT = "FILTER_BOLT";

    Topology topology;
    LocalCluster cluster;
    int maxContentLength;
    int maxDocumentCount;

    public CrawlerMS2(String url, int size, int count) {
        configureTopology(4, 4, 4);
        this.maxContentLength = size;
        this.maxDocumentCount = count;
        SharedInfo.getInstance().setMaster(this);
        SharedInfo.getInstance().addUrl(url);
    }

    public void configureTopology(int numFetcher, int numExtractor, int numFilter) {
        CrawlerSpout spout = new CrawlerSpout();
        DocumentFetcherBolt fetcherBolt = new DocumentFetcherBolt();
        LinkExtractorBolt extractorBolt = new LinkExtractorBolt();
        LinkFilterBolt filterBolt = new LinkFilterBolt();
        DomParserBolt parserBolt = new DomParserBolt();

        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout(SPOUT, spout, 1);
        builder.setBolt(FETCHER_BOLT, fetcherBolt, numFetcher).shuffleGrouping(SPOUT);
        builder.setBolt(EXTRACTOR_BOLT, extractorBolt, numExtractor).shuffleGrouping(FETCHER_BOLT);
        builder.setBolt(PARSER_BOLT,parserBolt, numExtractor).shuffleGrouping(FETCHER_BOLT);
        builder.setBolt(FILTER_BOLT, filterBolt, numFilter).shuffleGrouping(EXTRACTOR_BOLT);
        topology = builder.createTopology();
    }

    public void start() {
        Config config = new Config();
        cluster = new LocalCluster();
        cluster.submitTopology("test", config, topology);
    }

    @Override
    public boolean isOKtoCrawl(URLInfo urlInfo) {
        return false;
    }

    @Override
    public boolean deferCrawl(String site) {
        return false;
    }

    @Override
    public boolean isOKtoParse(URLInfo url) {
        return false;
    }

    @Override
    public boolean isIndexable(String content) {
        return false;
    }

    @Override
    public void incCount() {

    }

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public void setWorking(boolean working) {

    }

    @Override
    public void notifyThreadExited() {

    }

    @Override
    public void shutDown() {
        cluster.killTopology("test");
        cluster.shutdown();
        System.out.println("[⛔️ ShutDOWN: ]");
        System.exit(0);
    }

    /**
     * Main program:  init database, start crawler, wait
     * for it to notify that it is done, then close.
     */
    public static void main(String args[]) {
        Logger logger = LogManager.getLogger(Crawler.class);

        args = checkArgs(args);
        /* get DB*/
        String envPath = args[1];
        createDBDirectory(envPath);
        StorageInterface db = getDB(envPath);
        if (db == null) {
            logger.debug("Cannot Instantiate Database");
            System.exit(1);
        }
        /* Other Params*/
        String startUrl = args[0];
        Integer size = Integer.valueOf(args[2]);
        Integer count = args.length == 4 ? Integer.valueOf(args[3]) : 100;

        CrawlerMS2 crawler = new CrawlerMS2(startUrl, size, count);
        crawler.start();
        try {
            Thread.sleep(3000000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        crawler.shutDown();
        System.exit(0);
    }

    private static StorageInterface getDB(String dbName) {
        try {
            return StorageFactory.getDatabaseInstance(dbName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static void createDBDirectory(String dirName) {
        if (!Files.exists(Paths.get(dirName))) {
            try {
                Files.createDirectory(Paths.get(dirName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String[] checkArgs(String[] args) {
        if (args.length < 3 || args.length > 5) {
            args = new String[4];
            args[0] = "https://dbappserv.cis.upenn.edu/crawltest.html";
            args[1] = "dbs";
            args[2] = "1";
            args[3] = "3000";
        }
        return args;
    }
}
