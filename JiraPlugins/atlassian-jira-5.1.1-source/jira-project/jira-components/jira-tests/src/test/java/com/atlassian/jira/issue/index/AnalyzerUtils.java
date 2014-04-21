package com.atlassian.jira.issue.index;

import junit.framework.Assert;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class AnalyzerUtils
{
    public static SimpleToken[] tokensFromAnalysis(Analyzer analyzer, String text) throws IOException
    {
        TokenStream stream =
                analyzer.tokenStream("contents", new StringReader(text));
        CharTermAttribute charTermAttribute = stream.addAttribute(CharTermAttribute.class);
        PositionIncrementAttribute incrementAttribute =  stream.addAttribute(PositionIncrementAttribute.class);
        TypeAttribute typeAttribute = stream.addAttribute(TypeAttribute.class);
        OffsetAttribute offsetAttribute = stream.addAttribute(OffsetAttribute.class);

        ArrayList tokenList = new ArrayList();
        while (stream.incrementToken())
        {
            if (charTermAttribute == null) break;

            tokenList.add(new SimpleToken(charTermAttribute.toString(), incrementAttribute.getPositionIncrement(), typeAttribute.type(), offsetAttribute.startOffset(), offsetAttribute.endOffset()));
        }

        return (SimpleToken[]) tokenList.toArray(new SimpleToken[0]);
    }


    public static void displayTokensWithPositions(Analyzer analyzer,
                                                  String text) throws IOException
    {
        SimpleToken[] tokens = tokensFromAnalysis(analyzer, text);

        int position = 0;

        for (int i = 0; i < tokens.length; i++)
        {
            SimpleToken token = tokens[i];

            int increment = token.increment;

            if (increment > 0)
            {
                position = position + increment;
                System.out.println();
                System.out.print(position + ": ");
            }

            System.out.print("[" + token.term + "] ");
        }
        System.out.println();
    }

    public static void displayTokensWithFullDetails(Analyzer analyzer, String text) throws IOException
    {
        SimpleToken[] tokens = tokensFromAnalysis(analyzer, text);

        int position = 0;

        for (int i = 0; i < tokens.length; i++)
        {
            SimpleToken token = tokens[i];

            int increment = token.increment;

            if (increment > 0)
            {
                position = position + increment;
                System.out.println();
                System.out.print(position + ": ");
            }

            System.out.print("[" + token.term + "|" +
                    token.type + "] ");
        }
        System.out.println();
    }

    public static void assertTokensEqual(Token[] tokens, String[] strings)
    {
        Assert.assertEquals(strings.length, tokens.length);

        for (int i = 0; i < tokens.length; i++)
        {
            Assert.assertEquals("index " + i, strings[i], tokens[i].term());
        }
    }
    public static void displayTokens(Analyzer analyzer, String text) throws IOException
    {
        SimpleToken[] tokens = tokensFromAnalysis(analyzer, text);

        for (int i = 0; i < tokens.length; i++)
        {
            SimpleToken token = tokens[i];

            System.out.print("[" + token.term + "] ");
        }
    }

    private static class SimpleToken
    {
        private final String term;
        private final int increment;
        private final String type;
        private final int start;
        private final int end;

        public SimpleToken(String term, int increment, String type, int start, int end)
        {
            this.term = term;
            this.increment = increment;
            this.type = type;
            this.start = start;
            this.end = end;
        }
    }
}
