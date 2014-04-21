package com.atlassian.jira.issue.index.analyzer;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.junit.Test;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import static com.atlassian.jira.util.collect.CollectionBuilder.list;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @since v4.3
 */
public class TestWildcardFilter
{

    @Test
    public void testSingleToken() throws IOException
    {
        final TokenStream tokenStream = new MockTokenStream(list("Bugs"));
        final WildcardFilter filter = new WildcardFilter(tokenStream);
        assertToken("Bugs*", "word", filter.next());
        assertNull("Should reach EOS.", filter.next());
    }

    @Test
    public void testMultipleTokens() throws IOException
    {
        final TokenStream tokenStream = new MockTokenStream(list("Bugs","Features"));
        final WildcardFilter filter = new WildcardFilter(tokenStream);
        assertToken("Bugs*", "word", filter.next());
        assertToken("Features*", "word", filter.next());
        assertNull("Should reach EOS.", filter.next());
    }

    private void assertToken(final String termText, final String type, final Token token)
    {
        assertEquals(termText, new String(token.termBuffer(), 0, token.termLength()));
        assertEquals(type, token.type());
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
