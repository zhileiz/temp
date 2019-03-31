package edu.upenn.cis.cis455.xpathengine.occurrence;


/**
 This is a REALLY SIMPLE class that encapsulates the tokens we care about parsing in XML (or HTML).  You might want to do more than this, or not.
 */
public class OccurrenceEvent {
    enum Type {Open, Close, Text};

    Type type;
    String value;

    public OccurrenceEvent(Type t, String value) {
        this.type = t;
        this.value = value;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        if (type == Type.Open) { return "<" + value + ">"; }
        else if (type == Type.Close) { return "</" + value + ">"; }
        else { return value; }
    }
}
