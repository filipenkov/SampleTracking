package com.atlassian.jira.issue.index;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import java.io.IOException;
import java.io.Reader;
import java.util.concurrent.atomic.AtomicBoolean;

import com.atlassian.jira.local.ListeningTestCase;

public class TestJavaExceptionAnalyzer extends ListeningTestCase
{
    @Test
    public void testTokenStream() throws Exception
    {
        final AtomicBoolean tokenStreamCalled = new AtomicBoolean(false);
        final Analyzer mockAnalyzer = new Analyzer()
        {
            @Override
            public TokenStream tokenStream(final String fieldName, final Reader reader)
            {
                tokenStreamCalled.set(true);
                return new MockTokenStream();
            }
        };
        final JavaExceptionAnalyzer analyzer = new JavaExceptionAnalyzer(mockAnalyzer);

        final TokenStream tokenStream = analyzer.tokenStream(null, null);
        assertEquals("java.lang.NullPointerException", tokenStream.next().termText());
        assertEquals("NullPointerException", tokenStream.next().termText());
        assertEquals("lang", tokenStream.next().termText());
        assertEquals("java", tokenStream.next().termText());
        assertEquals(null, tokenStream.next());

    }

    private class MockTokenStream extends TokenStream
    {
        int count = 0;

        // return token the first time, null in any consequent call
        @Override
        public Token next() throws IOException
        {
            if (count == 0)
            {
                count++;
                return new Token("java.lang.NullPointerException", 0, 1);
            }
            else
            {
                return null;
            }
        }
    }

    @Test
    public void testGetPositionIncrementGap() throws Exception
    {
        final AtomicBoolean getPositionIncrementGapCalled = new AtomicBoolean(false);
        final Analyzer mockAnalyzer = new Analyzer()
        {
            @Override
            public TokenStream tokenStream(final String fieldName, final Reader reader)
            {
                return null;
            }

            @Override
            public int getPositionIncrementGap(final String fieldName)
            {
                getPositionIncrementGapCalled.set(true);
                return 123;
            }
        };
        final JavaExceptionAnalyzer analyzer = new JavaExceptionAnalyzer(mockAnalyzer);

        assertEquals(123, analyzer.getPositionIncrementGap(null));
        assertTrue(getPositionIncrementGapCalled.get());

    }

}
