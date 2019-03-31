package edu.upenn.cis.cis455.xpathengine.parser;

public class Test {
    TestType type;
    String substring;

    public Test(TestType tp, String sub) {
        type = tp;
        substring = sub;
    }

    public String getSubstring() {
        return substring;
    }

    @Override
    public String toString() {
        if (type == TestType.CONTAINS) {
            return String.format("contains(text(), \"%s\")", substring);
        } else if (type == TestType.EQUALS) {
            return String.format("text() = \"%s\"", substring);
        } else {
            return "<Invalid Test Grammar>";
        }
    }
}