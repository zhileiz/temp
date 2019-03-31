package edu.upenn.cis.cis455.xpathengine.parser;

import java.util.LinkedList;
import java.util.List;

public class XPath {

    List<Step> steps;

    public XPath(){
        steps = new LinkedList<Step>();
    }

    @Override
    public String toString() {
        StringBuilder bd = new StringBuilder();
        for (Step s : steps) {
            bd.append(String.format("/%s", s.toString()));
        }
        return bd.toString();
    }

    public void addStep(Step step){
        steps.add(step);
    }

    public List<Step> getSteps() { return steps; }
}