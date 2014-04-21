package com.atlassian.jira.issue.index.indexers.impl;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.mock.issue.MockIssue;
import org.apache.lucene.document.Document;


public class TestIssueKeyIndexer extends ListeningTestCase
{
    @Test
    public void testNullIssue()
    {
        IssueKeyIndexer indexer = new IssueKeyIndexer(null);
        Document doc = new Document();
        indexer.addIndex(doc, null);
        assertTrue(doc.getFields().isEmpty());
    }

    @Test
    public void testNullKey()
    {
        Issue issue = getMockIssue(null);
        IssueKeyIndexer indexer = new IssueKeyIndexer(null);
        Document doc = new Document();
        indexer.addIndex(doc, issue);
        assertTrue(doc.getFields().isEmpty());
    }

    @Test
    public void testNoNumber()
    {
        Issue issue = getMockIssue("NICK");
        IssueKeyIndexer indexer = new IssueKeyIndexer(null);
        Document doc = new Document();
        indexer.addIndex(doc, issue);
        assertEquals("NICK", doc.get(DocumentConstants.ISSUE_KEY));
        assertEquals("nick", doc.get(DocumentConstants.ISSUE_KEY_FOLDED));
        assertEquals(null, doc.get(DocumentConstants.ISSUE_KEY_NUM_PART));
        assertNull(doc.get(DocumentConstants.ISSUE_KEY_NUM_PART_RANGE));

    }
    @Test
    public void testNoNumberWithDash()
    {
        Issue issue = getMockIssue("NICK-");
        IssueKeyIndexer indexer = new IssueKeyIndexer(null);
        Document doc = new Document();
        indexer.addIndex(doc, issue);
        assertEquals("NICK-", doc.get(DocumentConstants.ISSUE_KEY));
        assertEquals("nick-", doc.get(DocumentConstants.ISSUE_KEY_FOLDED));
        assertEquals(null, doc.get(DocumentConstants.ISSUE_KEY_NUM_PART));
        assertNull(doc.get(DocumentConstants.ISSUE_KEY_NUM_PART_RANGE));

    }
    @Test
    public void testDoubleDash()
    {
        Issue issue = getMockIssue("NICK-NICK-1234");
        IssueKeyIndexer indexer = new IssueKeyIndexer(null);
        Document doc = new Document();
        indexer.addIndex(doc, issue);
        assertEquals("NICK-NICK-1234", doc.get(DocumentConstants.ISSUE_KEY));
        assertEquals("nick-nick-1234", doc.get(DocumentConstants.ISSUE_KEY_FOLDED));
        assertEquals("1234", doc.get(DocumentConstants.ISSUE_KEY_NUM_PART));

        //This is NumberTools.longToString(1234L). It is a base-36 padded number.
        assertEquals("000000000000ya",  doc.get(DocumentConstants.ISSUE_KEY_NUM_PART_RANGE));

    }
    @Test
    public void testGoodKey()
    {
        Issue issue = getMockIssue("NICK-1234");
        IssueKeyIndexer indexer = new IssueKeyIndexer(null);
        Document doc = new Document();
        indexer.addIndex(doc, issue);
        assertEquals("NICK-1234", doc.get(DocumentConstants.ISSUE_KEY));
        assertEquals("nick-1234", doc.get(DocumentConstants.ISSUE_KEY_FOLDED));
        assertEquals("1234", doc.get(DocumentConstants.ISSUE_KEY_NUM_PART));

        //This is NumberTools.longToString(1234L). It is a base-36 padded number.
        assertEquals("000000000000ya",  doc.get(DocumentConstants.ISSUE_KEY_NUM_PART_RANGE));

    }
    private Issue getMockIssue(final String key)
    {
        return new MockIssue(999)
        {
            public String getKey()
            {
                return key;
            }
        };
    }
}
