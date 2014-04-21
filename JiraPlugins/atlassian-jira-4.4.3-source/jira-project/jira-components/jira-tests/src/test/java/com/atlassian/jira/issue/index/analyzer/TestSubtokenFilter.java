package com.atlassian.jira.issue.index.analyzer;

import org.junit.Test;
import static org.junit.Assert.*;
import static com.atlassian.jira.util.collect.CollectionBuilder.list;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.el.GreekAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import com.atlassian.jira.local.ListeningTestCase;

/**
 * @since v3.13
 */
public class TestSubtokenFilter extends ListeningTestCase
{
    @Test
    public void testEmptyText() throws Exception
    {
        final TokenStream tokenStream = getTokenStreamFromEnglishAnalyzer("");
        assertNull(new SubtokenFilter(tokenStream).next());
    }

    @Test
    public void testExceptionString() throws Exception
    {
        final TokenStream tokenStream = getTokenStreamFromEnglishAnalyzer("Throws java.lang.NullPointerException sometimes.");
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertToken("throw", "<ALPHANUM>", filter.next());
        // The Analyser thinks this is a server hostname like www.atlassian.com
        assertToken("java.lang.nullpointerexcept", "<HOST>", filter.next());
        assertToken("nullpointerexcept", "EXCEPTION", filter.next());
        assertToken("lang", "EXCEPTION", filter.next());
        assertToken("java", "EXCEPTION", filter.next());
        assertToken("sometim", "<ALPHANUM>", filter.next());
        assertNull(filter.next());
    }

    @Test
    public void testExceptionStringWithLeadingDot() throws Exception
    {
        final TokenStream tokenStream = getTokenStreamFromEnglishAnalyzer("Throws .java.lang.NullPointerException sometimes.");
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertToken("throw", "<ALPHANUM>", filter.next());
        // The Lead dot should be removed, and tehn act as per usual.
        assertToken("java.lang.nullpointerexcept", "<HOST>", filter.next());
        assertToken("nullpointerexcept", "EXCEPTION", filter.next());
        assertToken("lang", "EXCEPTION", filter.next());
        assertToken("java", "EXCEPTION", filter.next());
        assertToken("sometim", "<ALPHANUM>", filter.next());
        assertNull(filter.next());
    }

    @Test
    public void testExceptionStringWithTrailingDot() throws Exception
    {
        final TokenStream tokenStream = getTokenStreamFromEnglishAnalyzer("Throws java.lang.NullPointerException.");
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertToken("throw", "<ALPHANUM>", filter.next());

        // JRA-15484 Lucene used to get this wrong with a dot at the end. In order to get it right, you need to call the
        // StandardTokenizer constructor with replaceInvalidAcronym=true.
        // see http://issues.apache.org/jira/browse/LUCENE-1068
        assertToken("java.lang.nullpointerexcept", "<HOST>", filter.next());
        assertToken("nullpointerexcept", "EXCEPTION", filter.next());
        assertToken("lang", "EXCEPTION", filter.next());
        assertToken("java", "EXCEPTION", filter.next());
        assertNull(filter.next());
    }

    @Test
    public void testExceptionStringGreek() throws Exception
    {
        // Greek uses different characters to English, so the latin letters used in our java Exception will get type "word", instead of "<ALPHANUM>".

        final TokenStream tokenStream = new GreekAnalyzer().tokenStream("TestField", new StringReader(
            "Throws java.lang.NullPointerException sometimes."));
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        // No stemming for English (its all Greek to me).
        assertToken("throws", "<ALPHANUM>", filter.next());
        // Note that this comes through as "word" (the default type), not <ALPHANUM>.
        assertToken("java.lang.nullpointerexception", "<HOST>", filter.next());
        assertToken("nullpointerexception", "EXCEPTION", filter.next());
        assertToken("lang", "EXCEPTION", filter.next());
        assertToken("java", "EXCEPTION", filter.next());
        assertToken("sometimes", "<ALPHANUM>", filter.next());
        assertNull(filter.next());
    }

    @Test
    public void testExceptionStringWithTrailingDotInGreek() throws Exception
    {
        final TokenStream tokenStream = new GreekAnalyzer().tokenStream("TestField", new StringReader("Throws java.lang.NullPointerException."));
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertToken("throws", "<ALPHANUM>", filter.next());

        // For JIRA, we would still like to find the "NullPointerException".
        // This was found to not work out of the box for English (JRA-15484), but is fine in Greek.
        assertToken("java.lang.nullpointerexception.", "<ACRONYM>", filter.next());

        // @TODO fix this for Lucene 3.0
        // assertToken("nullpointerexception", "EXCEPTION", filter.next());
        // assertToken("lang", "EXCEPTION", filter.next());
        // assertToken("java", "EXCEPTION", filter.next());
        assertNull(filter.next());
    }

    @Test
    public void testExceptionStringWithoutTrailingDotInGreek() throws Exception
    {
        final TokenStream tokenStream = new GreekAnalyzer().tokenStream("TestField", new StringReader("Throws java.lang.NullPointerException"));
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertToken("throws", "<ALPHANUM>", filter.next());

        // For JIRA, we would still like to find the "NullPointerException".
        // This was found to not work out of the box for English (JRA-15484), but is fine in Greek.
        assertToken("java.lang.nullpointerexception", "<HOST>", filter.next());

        // @TODO fix this for 2.9

        assertToken("nullpointerexception", "EXCEPTION", filter.next());
        assertToken("lang", "EXCEPTION", filter.next());
        assertToken("java", "EXCEPTION", filter.next());
        assertNull(filter.next());
    }

    @Test
    public void testNumberList() throws Exception
    {
        final TokenStream tokenStream = getTokenStreamFromEnglishAnalyzer("2,500");
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertToken("2,500", "<NUM>", filter.next());
        assertToken("500", "<NUM>", filter.next());
        assertToken("2", "<NUM>", filter.next());
        assertNull(filter.next());
    }

    @Test
    public void testNumberListInGreek() throws Exception
    {
        final TokenStream tokenStream = new GreekAnalyzer().tokenStream("TestField", new StringReader("2,500"));
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        // note that the GreekAnalyzer did not tokenize "2,500" as a number in 2.3 and now it does!!!.
        assertToken("2,500", "<NUM>", filter.next());
        assertToken("500", "<NUM>", filter.next());
        assertToken("2", "<NUM>", filter.next());
        assertNull(filter.next());
    }

    /**
     * The implementations of tokenizers that we use will not actually create such tokens currently, but our Filter should
     * be able to handle preceding dots, trailing dots, and multiple dots in a row.
     * @throws IOException IOException
     */
    @Test
    public void testTheoreticalEdgeCases() throws IOException
    {
        final TokenStream tokenStream = new MockTokenStream(list("..java..lang..NullPointerException.."));
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertToken("..java..lang..NullPointerException..", "word", filter.next());
        assertToken("NullPointerException", "EXCEPTION", filter.next());
        assertToken("lang", "EXCEPTION", filter.next());
        assertToken("java", "EXCEPTION", filter.next());
        assertNull(filter.next());
    }

    @Test
    public void testSingleSubtokenWithDot() throws IOException
    {
        final TokenStream tokenStream = new MockTokenStream(list(".NullPointerException"));
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertToken(".NullPointerException", "word", filter.next());
        // This is not ideal, but this is historically how it works, and won't happen with the real tokenizers.
        assertToken("NullPointerException", "EXCEPTION", filter.next());
        assertNull("Should reach EOS.", filter.next());
    }

    @Test
    public void testSingleSubtokenWithTrailingDot() throws IOException
    {
        final TokenStream tokenStream = new MockTokenStream(list("NullPointerException."));
        final SubtokenFilter filter = new SubtokenFilter(tokenStream);
        assertToken("NullPointerException.", "word", filter.next());
        // This is not ideal, but this is historically how it works, and won't happen with the real tokenizers.
        assertToken("NullPointerException", "EXCEPTION", filter.next());
        assertNull("Should reach EOS.", filter.next());
    }

    private void assertToken(final String termText, final String type, final Token token)
    {
        assertEquals(termText, new String(token.termBuffer(), 0, token.termLength()));
        assertEquals(type, token.type());
    }

    private TokenStream getTokenStreamFromEnglishAnalyzer(final String text)
    {
        return new EnglishAnalyzer(false).tokenStream("TestField", new StringReader(text));
    }

    private class MockTokenStream extends TokenStream
    {
        private final Iterator<String> iterator;

        MockTokenStream(final List<String> tokens)
        {
            iterator = tokens.iterator();
        }

        @Override
        public Token next(final Token result) throws IOException
        {
            if (iterator.hasNext())
            {
                result.setTermBuffer(iterator.next());
                return result;
            }
            else
            {
                // End Of Stream
                return null;
            }
        }
    }
}
