package edu.upenn.cis.cis455.crawler.crawler.ms2;

import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.stormlite.bolt.IRichBolt;

public abstract class StorageAccessorBolt implements IRichBolt {
    protected StorageInterface getDB() {
        try {
            return StorageFactory.getDatabaseInstance("");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
