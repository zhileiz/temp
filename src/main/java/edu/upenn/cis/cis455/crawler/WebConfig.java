package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.crawler.handlers.*;
import edu.upenn.cis.cis455.crawler.utils.Constants;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static spark.Spark.*;
import static spark.Spark.awaitInitialization;
import static spark.Spark.post;

public class WebConfig {

    Logger logger = LogManager.getLogger(WebConfig.class);

    public WebConfig(String[] args) {
        args = checkArgs(args);
        /* set up port */
        port(8080);
        /* set up database */
        createDBDirectory(args[0]);
        StorageInterface database = getDB(args[0]);
        if (database == null) {
            logger.debug("Cannot Instantiate Database");
            System.exit(1);
        }
        /* set up static file folders */
        staticFiles.externalLocation(args[1]);
        staticFileLocation(args[1]);
        /* set up routes*/
        setUpRoutes(database);
        awaitInitialization();
    }

    private void setUpRoutes(StorageInterface database) {
        /* Main Page */
        before("*", new MainFilter());
        get(Constants.Paths.MAIN_PAGE, new MainHandler());
        /* Register */
        before(Constants.Paths.REGISTER, new RegisterFilter());
        post(Constants.Paths.REGISTER, new RegisterHandler(database));
        /* Log In */
        before(Constants.Paths.LOGIN, new LoginFilter());
        post(Constants.Paths.LOGIN, new LoginHandler(database));
        /* Log out*/
        get(Constants.Paths.LOGOUT, new LogoutHandler());

        /* Crawler Page */
        get(Constants.Paths.LOOKUP, new LookupHandler(database));
        /* Channel Creation Page */
        get(Constants.Paths.CREATE, new ChannelCreateHandler());
        /* Channel Show Page */
        get(Constants.Paths.SHOW, new ChannelShowHandler());
    }

    private void createDBDirectory(String dirName) {
        if (!Files.exists(Paths.get(dirName))) {
            try {
                Files.createDirectory(Paths.get(dirName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private StorageInterface getDB(String dbName) {
        try {
            return StorageFactory.getDatabaseInstance(dbName);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String[] checkArgs(String[] args) {
        //    if (args.length < 1 || args.length > 2) {
        //        logger.debug("Syntax: WebInterface {path} {root}");
        //        System.exit(1);
        //    }
        args = new String[2];
        args[0] = "./dbs";
        args[1] = "/public";
        return args;
    }
}
