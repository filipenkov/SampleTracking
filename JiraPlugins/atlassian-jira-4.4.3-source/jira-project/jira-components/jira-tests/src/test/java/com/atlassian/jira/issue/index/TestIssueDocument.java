/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.web.FieldVisibilityManager;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;


import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestIssueDocument extends LegacyJiraMockTestCase
{
    MockIssue issue;
    private FieldVisibilityManager origFieldVisibilityManager;

    public TestIssueDocument(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        origFieldVisibilityManager = ComponentManager.getComponentInstanceOfType(FieldVisibilityManager.class);

        final FieldVisibilityManager visibilityBean = EasyMock.createMock(FieldVisibilityManager.class);
        EasyMock.expect(visibilityBean.isFieldHidden((String)EasyMock.anyObject(), (Issue)EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.replay(visibilityBean);
        ManagerFactory.addService(FieldVisibilityManager.class, visibilityBean);

        MockGenericValue mockGv = new MockGenericValue("Issue", EasyMap.build("id", 132L, "project", 21L, "key", "fookey",
                "summary", "foosum"));
        issue = new MockIssue();
        issue.setGenericValue(mockGv);
        issue.setId(132L);
        issue.setKey("fookey");
        issue.setProject(new MockGenericValue("Project", EasyMap.build("id", 21L)));
        issue.setSummary("foosum");
        issue.setCreated(null);
        issue.setUpdated(null);
    }

    @Override
    protected void tearDown() throws Exception
    {
        ManagerFactory.addService(FieldVisibilityManager.class, origFieldVisibilityManager);
        super.tearDown();
    }

    public void testGetDocumentBlanks()
    {
        Document doc = IssueDocument.getDocument(issue);

        assertEquals("132", doc.getField(DocumentConstants.ISSUE_ID).stringValue());
        assertEquals("fookey", doc.getField(DocumentConstants.ISSUE_KEY).stringValue());
        assertEquals("21", doc.getField(DocumentConstants.PROJECT_ID).stringValue());
        assertEquals("-1", doc.getField(DocumentConstants.ISSUE_FIXVERSION).stringValue());
        assertEquals("-1", doc.getField(DocumentConstants.ISSUE_COMPONENT).stringValue());
        assertEquals("-1", doc.getField(DocumentConstants.ISSUE_VERSION).stringValue());

        assertEquals("foosum", doc.getField(DocumentConstants.ISSUE_SUMMARY).stringValue());
        assertNull(doc.getField(DocumentConstants.ISSUE_DESC));
        assertNull(doc.getField(DocumentConstants.ISSUE_ENV));

        assertNull(doc.getField(DocumentConstants.ISSUE_TYPE));
        assertEquals(DocumentConstants.ISSUE_NO_AUTHOR, doc.getField(DocumentConstants.ISSUE_AUTHOR).stringValue());
        assertEquals(DocumentConstants.ISSUE_UNASSIGNED, doc.getField(DocumentConstants.ISSUE_ASSIGNEE).stringValue());
        assertNull(doc.getField(DocumentConstants.ISSUE_STATUS));
        assertEquals("-1", doc.getField(DocumentConstants.ISSUE_RESOLUTION).stringValue());
        assertEquals("-1", doc.getField(DocumentConstants.ISSUE_PRIORITY).stringValue());

        assertNull(doc.getField(DocumentConstants.ISSUE_CREATED));
        assertNull(doc.getField(DocumentConstants.ISSUE_UPDATED));
        assertEquals("-1", doc.getField(DocumentConstants.ISSUE_SECURITY_LEVEL).stringValue());
    }

    public void testGetDocument()
    {
        issue.setEnvironment("fooenv");
        issue.setDescription("foodesc");
        issue.setSecurityLevelId(new Long(10000));

        Document doc = IssueDocument.getDocument(issue);

        assertEquals("132", doc.getField(DocumentConstants.ISSUE_ID).stringValue());
        assertEquals("foodesc", doc.getField(DocumentConstants.ISSUE_DESC).stringValue());
        assertEquals("fooenv", doc.getField(DocumentConstants.ISSUE_ENV).stringValue());
        assertEquals("10000", doc.getField(DocumentConstants.ISSUE_SECURITY_LEVEL).stringValue());
    }

    public void testNonEmptyFieldIds() throws Exception
    {
        MockGenericValue mockGv = new MockGenericValue("Issue", EasyMap.build("id", 456L, "project", 21L, "key", "newkey",
                "summary", "foosum", "environment", "fooenv"));
        MockIssue issue2 = new MockIssue();
        issue2.setGenericValue(mockGv);
        issue2.setId(new Long(456));
        issue2.setKey("newkey");
        issue2.setProject(new MockGenericValue("Project", EasyMap.build("id", new Long(21))));
        issue2.setSummary("foosum");
        issue2.setCreated(null);
        issue2.setUpdated(null);

       
        final IssueManager mockIssueManager = createMock(IssueManager.class);
        expect(mockIssueManager.getEntitiesByIssueObject(isA(String.class),isA(Issue.class)))
                .andReturn(Collections.<GenericValue>emptyList()).anyTimes();

        replay(mockIssueManager);

        final MemoryIndexManager memoryIndexManager = new MemoryIndexManager(mockIssueManager);
        memoryIndexManager.reIndex(issue);
        memoryIndexManager.reIndex(issue2);

        final IndexSearcher indexSearcher = memoryIndexManager.getIssueSearcher();

        final BooleanQuery notHaveEnvironmentQuery = new BooleanQuery();
        notHaveEnvironmentQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "environment")), BooleanClause.Occur.MUST_NOT);
        notHaveEnvironmentQuery.add(new TermQuery(new Term("resolution", "-1")), BooleanClause.Occur.MUST);
        Hits hits = indexSearcher.search(notHaveEnvironmentQuery);

        // Make sure we only got one out
        assertEquals(1, hits.length());
        // Make sure it is the right one
        String[] values = ((Hit) hits.iterator().next()).getDocument().getValues(DocumentConstants.ISSUE_KEY);
        assertEquals("fookey", values[0]);

        final BooleanQuery hasEnvironmentQuery = new BooleanQuery();
        hasEnvironmentQuery.add(new TermQuery(new Term(DocumentConstants.ISSUE_NON_EMPTY_FIELD_IDS, "environment")), BooleanClause.Occur.MUST);
        hasEnvironmentQuery.add(new TermQuery(new Term("resolution", "-1")), BooleanClause.Occur.MUST);
        hits = indexSearcher.search(hasEnvironmentQuery);

        // Make sure we only got one out
        assertEquals(1, hits.length());
        // Make sure it is the right one
        values = ((Hit) hits.iterator().next()).getDocument().getValues(DocumentConstants.ISSUE_KEY);
        assertEquals("newkey", values[0]);

        verify(mockIssueManager);
    }
}
