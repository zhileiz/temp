package edu.upenn.cis.cis455.xpathengine;

import edu.upenn.cis.cis455.xpathengine.parser.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedList;
import java.util.List;

public class XPathMatcher {

    Logger logger = LogManager.getLogger(XPathMatcher.class);
    List<XPath> xpaths;
    Document doc;

    public XPathMatcher() { }

    public XPathMatcher(List<XPath> xps, Document doc) {
        this.xpaths = xps;
        this.doc = doc;
    }

    public void setDoc(Document doc) {
        this.doc = doc;
    }

    public void setXpaths(List<XPath> xpaths) {
        this.xpaths = xpaths;
    }

    public List<Boolean> match() {
        List<Boolean> result = new LinkedList<Boolean>();
        Element node = doc.getDocumentElement();
        for (XPath xp : xpaths) {
            logger.debug(xp.toString());
            result.add(matchStep(xp.getSteps(), node, 0));
        }
        return result;
    }

    public boolean passEqualTest(Step s, Element node) {
        NodeList nl = node.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
                String content = nl.item(i).getTextContent().toLowerCase();
                String stepValue = s.getEqualTest().getSubstring().toLowerCase();
                if (content.equals(stepValue)) { return true; }
            }
        }
        return false;
    }

    public boolean passAllContainTests(Step s, Element node) {
        if (s.getContainTests().size() == 0) { return true; }
        for (Test t : s.getContainTests()) {
            boolean passed = false;
            NodeList nl = node.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
                    String content = nl.item(i).getTextContent().toLowerCase();
                    String testVal = t.getSubstring().toLowerCase();
                    if (content.contains(testVal)) { passed = true; break; }
                }
            }
            if (!passed) { return false; }
        }
        return true;
    }

    private boolean matchStep(List<Step> steps, Element node, int checkInx) {
        if (steps.size() <= checkInx) { return true; }
        Step s = steps.get(checkInx);
        if (!node.getTagName().equals(s.getNodename())) { return false; }
        if (s.getEqualTest() != null && !passEqualTest(s, node)) { return false; }
        if (!passAllContainTests(s, node)) { return false; }
        NodeList children = node.getChildNodes();
        boolean existsElement = false;
        for (int i = 0; i < children.getLength(); i ++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                existsElement = true;
                if (matchStep(steps, (Element) child, checkInx + 1)) {
                    return true;
                }
            }
        }
        return !existsElement && steps.size() == checkInx + 1;
    }
}
