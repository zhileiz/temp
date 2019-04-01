package edu.upenn.cis.cis455.xpathengine;

import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.model.Channel;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Implement this factory to produce your XPath engine
 * and SAX handler as necessary.  It may be called by
 * the test/grading infrastructure.
 * 
 * @author cis455
 *
 */
public class XPathEngineFactory {

	private static Logger logger = LogManager.getLogger(XPathEngine.class);

	public static XPathEngine instance;

	public static XPathEngine getXPathEngine() {
		if (instance == null) {
			instance = new XPathEngineImpl();
			try {
				StorageInterface db = StorageFactory.getDatabaseInstance("");
				List<Channel> channels = db.getAllChannels();
				List<String> xpaths = new ArrayList<>();
				for (Channel c : channels) { xpaths.add(c.getXpath()); }
				instance.setXPaths(xpaths.toArray(new String[xpaths.size()]));
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
		}
		return instance;
	}
	
	public static DefaultHandler getSAXHandler() {
		return null;
	}
}
