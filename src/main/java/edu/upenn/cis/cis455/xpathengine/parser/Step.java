package edu.upenn.cis.cis455.xpathengine.parser;

import java.util.LinkedList;
import java.util.List;

public class Step {

    String nodename;
    Test equalTest;
    List<Test> containTests;

    public Step(String nn){
        nodename = nn;
        containTests = new LinkedList<Test>();
    }

    public List<Test> getContainTests() {
        return containTests;
    }

    public String getNodename() {
        return nodename;
    }

    public Test getEqualTest() {
        return equalTest;
    }

    @Override
    public String toString() {
        StringBuilder bd = new StringBuilder();
        bd.append(nodename);
        if (equalTest != null) {
            bd.append(String.format("[%s]", equalTest.toString()));
        }
        for (Test t : containTests) {
            bd.append(String.format("[%s]", t.toString()));
        }
        return bd.toString();
    }

    public void addContainsTest(Test t){
        containTests.add(t);
    }
}
