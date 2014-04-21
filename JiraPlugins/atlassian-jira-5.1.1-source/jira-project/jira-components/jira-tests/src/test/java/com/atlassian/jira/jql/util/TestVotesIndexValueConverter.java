package com.atlassian.jira.jql.util;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.jql.operand.QueryLiteral;
import static com.atlassian.jira.jql.operand.SimpleLiteralFactory.createLiteral;

import org.apache.lucene.document.NumberTools;

/**
 * @since v4.0
 */
public class TestVotesIndexValueConverter extends ListeningTestCase
{
    @Test
    public void testValid() throws Exception
    {
        VotesIndexValueConverter converter = new VotesIndexValueConverter();
        assertEquals(NumberTools.longToString(10L), converter.convertToIndexValue(createLiteral(10L)));
        assertEquals(NumberTools.longToString(10L), converter.convertToIndexValue(createLiteral("10")));
        assertEquals(NumberTools.longToString(0L), converter.convertToIndexValue(createLiteral(0L)));
        assertEquals(NumberTools.longToString(0), converter.convertToIndexValue(createLiteral("0")));
    }

    @Test
    public void testNotValid() throws Exception
    {
        VotesIndexValueConverter converter = new VotesIndexValueConverter();
        assertNull(converter.convertToIndexValue(createLiteral(-1L)));
        assertNull(converter.convertToIndexValue(createLiteral("-1")));
        assertNull(converter.convertToIndexValue(createLiteral("ab")));
        assertNull(converter.convertToIndexValue(new QueryLiteral()));
    }
}
