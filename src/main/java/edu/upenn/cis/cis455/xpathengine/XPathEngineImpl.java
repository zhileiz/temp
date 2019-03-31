package edu.upenn.cis.cis455.xpathengine;

import edu.upenn.cis.cis455.model.OccurrenceEvent;
import edu.upenn.cis.cis455.xpathengine.occurrence.DomToOccurrence;
import edu.upenn.cis.cis455.xpathengine.parser.Parser;
import edu.upenn.cis.cis455.xpathengine.parser.XPath;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XPathEngineImpl implements XPathEngine {

    private XPathMatcher matcher;
    private String[] paths;

    public XPathEngineImpl() {
        matcher = new XPathMatcher();
    }

    @Override
    public void setXPaths(String[] expressions) {
        this.paths = expressions;
        List<XPath> xpaths = new ArrayList<>();
        for (String s : expressions) {
            try {
                XPath path = new Parser(s).parse();
                xpaths.add(path);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        matcher.setXpaths(xpaths);
    }

    @Override
    public boolean isValid(int i) {
        return false;
    }

    @Override
    public boolean[] evaluateEvent(OccurrenceEvent event) {
        return new boolean[0];
    }

    @Override
    public boolean[] evaluate(Document doc) {
        matcher.setDoc(doc);
        List<Boolean> matchResult = matcher.match();
        boolean[] result = new boolean[matchResult.size()];
        for (int i = 0; i < matchResult.size(); i++) {
            result[i] = matchResult.get(i);
        }
        return result;
    }

    @Override
    public String[] getPaths() {
        return paths;
    }
}
