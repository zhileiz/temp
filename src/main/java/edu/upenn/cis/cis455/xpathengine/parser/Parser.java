package edu.upenn.cis.cis455.xpathengine.parser;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Grammar:
 * xpath := (/ step)+
 * step := nodename ([ test ])*
 * test := test () = "..." | contains ( text (), " ... ")
 */

public class Parser {

    private String d_input; // Holds the rest of the input.
    private int d_currOffset; // Current offset into the original input.

    private Pattern d_nodenamePattern;
    {
        try {
            d_nodenamePattern = Pattern.compile("[a-zA-Z]+");
        } catch (PatternSyntaxException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }

    private void consume(int numChar) {
        d_input = d_input.substring(numChar);
        d_currOffset += numChar;
    }

    private void consumeWhitespace() {
        while (d_input.length() > 0
               && Character.isWhitespace(d_input.charAt(0))) {
            consume(1);
        }
    }

    /*******************************************************************
     * Note: All of the parse***() functions should not consume any of *
     * the input if they fail to parse due to recoverable conditions.  *
     *******************************************************************/

    private Test parseTest() throws ParseException {
        consumeWhitespace();
        if (d_input.length() < 4) {
            throw new ParseException("Excepted at least 4 chars, got less", d_currOffset);
        }
//        match EQUALS type
        if (d_input.charAt(0) == 't'){
            parseStrToken("text");
            consumeWhitespace();
            parseCharToken('(');
            consumeWhitespace();
            parseCharToken(')');
            consumeWhitespace();
            parseCharToken('=');
            consumeWhitespace();
            parseCharToken('\"');
            if (d_input.contains("\"")) {
                int end = d_input.indexOf("\"");
                Test t = new Test(TestType.EQUALS, d_input.substring(0, end));
                consume(end + 1);
                return t;
            } else {
                throw new ParseException("Inner text invalid, \" not found", d_currOffset);
            }
        } else if (d_input.charAt(0) == 'c') {
            parseStrToken("contains");
            consumeWhitespace();
            parseCharToken('(');
            consumeWhitespace();
            parseStrToken("text");
            parseCharToken('(');
            consumeWhitespace();
            parseCharToken(')');
            consumeWhitespace();
            parseCharToken(',');
            consumeWhitespace();
            parseCharToken('\"');
            if (d_input.contains("\"")) {
                int end = d_input.indexOf("\"");
                Test t = new Test(TestType.CONTAINS, d_input.substring(0, end));
                consume(end + 1);
                consumeWhitespace();
                parseCharToken(')');
                return t;
            } else {
                throw new ParseException("Inner text invalid, \" not found", d_currOffset);
            }
        } else {
            throw new ParseException("Parse test failed from the beginning, no c or t found", d_currOffset);
        }
    }

    // Precondition: d_input does not start with whitespace.
    private void parseCharToken(char c) throws ParseException {
        if (d_input.length() == 0 ) {
            throw new ParseException("Excepted operator, got EOL", d_currOffset);
        }
        if (d_input.charAt(0) != c) {
            throw new ParseException(String.format("Character %c not matched", c), d_currOffset);
        }
        consume(1);
    }

    // Precondition: d_input does not start with whitespace.
    private void parseStrToken(String s) throws ParseException {
        final int length = s.length();
        if (d_input.length() < length) {
            throw new ParseException(String.format("String length smaller than %d", length), d_currOffset);
        }
        if (!d_input.substring(0, length).equals(s)) {
            throw new ParseException(String.format("String %s not matched", s), d_currOffset);
        }
        consume(length);
    }

    private String parseNodename() throws ParseException {
        consumeWhitespace();
        Matcher matcher  = d_nodenamePattern.matcher(d_input);
        boolean foundNodename = matcher.find();

        if (!foundNodename) {
            throw new ParseException("Expected nodename", d_currOffset);
        }

        int startIdx = matcher.start();
        int endIdx   = matcher.end();

        // Since input doesn't start with whitespace, we expect the
        // first character to be the starting character of a number.
        if (startIdx != 0) {
            throw new ParseException("Expected nodename", d_currOffset);
        }

        // Parse and return the number
        String nodename = d_input.substring(startIdx, endIdx);
        consume(nodename.length());
        return nodename;
    }

    private Step parseStep() throws ParseException {
        String nn = parseNodename();
        Step s = new Step(nn);
        consumeWhitespace();
        while (d_input.length() != 0 && d_input.charAt(0) == '[') {
            consume(1);
            Test t = parseTest();
            if (t.type == TestType.EQUALS) {
                if (s.equalTest != null) {
                    throw new ParseException("Duplicate Equal Test parsed", d_currOffset);
                } else {
                    s.equalTest = t;
                }
            } else {
                s.addContainsTest(t);
            }
            parseCharToken(']');
            consumeWhitespace();
        }
        return s;
    }

    public XPath parseXPath() throws ParseException {
        consumeWhitespace();
        parseCharToken('/');
        XPath xp = new XPath();
        xp.addStep(parseStep());

        while (d_input.length() != 0) {
            if (d_input.charAt(0) != '/') {
                throw new ParseException(String.format("Expected /, got %c instead", d_input.charAt(0)), d_currOffset);
            } else {
                consume(1);
                xp.addStep(parseStep());
            }
        }
        return xp;
    }

    public XPath parse() throws ParseException {
        return parseXPath();
    }
    
    public Parser(String input) {
        d_input = input;
        d_currOffset = 0;
    }
}
