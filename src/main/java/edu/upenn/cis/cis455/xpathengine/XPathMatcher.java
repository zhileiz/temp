package edu.upenn.cis.cis455.xpathengine;

import edu.upenn.cis.cis455.xpathengine.parser.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedList;
import java.util.List;

public class XPathMatcher {
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
            System.out.println(xp.toString());
            result.add(matchStep(xp.getSteps(), node, 0));
        }
        return result;
    }

    private boolean matchStep(List<Step> steps, Element node, int checkInx) {
        if (steps.size() <= checkInx) { return true; }
        Step s = steps.get(checkInx);
        System.out.println(s.getNodename() + "/" +node.getTagName() + "/" +checkInx);
        if (!node.getTagName().equals(s.getNodename())) { return false; }
        if (s.getEqualTest() != null) {
            boolean passed = false;
            NodeList nl = node.getChildNodes();
            for (int i = 0; i < nl.getLength(); i += 1) {
                if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
                    if (nl.item(i).getTextContent().toLowerCase().equals(s.getEqualTest().getSubstring().toLowerCase())) {
                        passed = true;
                    }
                }
            }
            if (!passed) { return false; }
        }
        if (s.getContainTests().size() != 0) {
            for (Test t : s.getContainTests()) {
                boolean passed = false;
                NodeList nl = node.getChildNodes();
                for (int i = 0; i < nl.getLength(); i += 1) {
                    if (nl.item(i).getNodeType() == Node.TEXT_NODE) {
                        if (nl.item(i).getTextContent().toLowerCase().contains(t.getSubstring().toLowerCase())) {
                            passed = true;
                        }
                    }
                }
                if (!passed) { return false; }
            }
        }
        NodeList children = node.getChildNodes();
        int count =  children.getLength();
        boolean existsElement = false;
        for (int i = 0; i < count; i += 1) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                existsElement = true;
                if (matchStep(steps, (Element) child, checkInx + 1)) return true;
            }
        }
        return !existsElement && steps.size() == checkInx + 1;
    }
}
