package edu.upenn.cis.cis455.storage;

import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;

public class BerkeleyDatabase {
    private Environment env;
    private static final String CLASS_CATALOG = "java_class_catalog";
    private StoredClassCatalog javaCatalog;

    Logger logger = LogManager.getLogger(BerkeleyDatabase.class);

    private static final String USER_STORE = "user_store";
    private static final String DOCUMENT_STORE = "document_store";
    private static final String CONTENT_SEEN_STORE = "content_seen_store";
    private static final String CHANNEL_STORE = "channel_store";

    private Database userDb;
    private Database documentDb;
    private Database contentSeenDb;
    private Database channelDb;

    public BerkeleyDatabase(String homeDirectory) throws DatabaseException, FileNotFoundException {
        logger.debug("Opening environment in: " + homeDirectory);

        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(true);
        envConfig.setAllowCreate(true);

        env = new Environment(new File(homeDirectory), envConfig);

        // java class catalog
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(true);
        dbConfig.setAllowCreate(true);

        Database catalogDb = env.openDatabase(null, CLASS_CATALOG, dbConfig);
        javaCatalog = new StoredClassCatalog(catalogDb);

        // databases
        userDb = env.openDatabase(null, USER_STORE, dbConfig);
        documentDb = env.openDatabase(null, DOCUMENT_STORE, dbConfig);
        contentSeenDb = env.openDatabase(null, CONTENT_SEEN_STORE, dbConfig);
        channelDb = env.openDatabase(null, CHANNEL_STORE, dbConfig);
    }

    public final StoredClassCatalog getClassCatalog() {
        return javaCatalog;
    }

    public final Database getUserDatabase() {
        return userDb;
    }

    public final Database getDocumentDatabase() {
        return documentDb;
    }

    public Database getContentSeenDb() {
        return contentSeenDb;
    }

    public final Database getChannelDb() { return channelDb; }

    public void close() throws DatabaseException {
        userDb.close();
        documentDb.close();
        contentSeenDb.close();
        channelDb.close();
        javaCatalog.close();
        env.close();
    }

    public final Environment getEnvironment() {
        return env;
    }
}
