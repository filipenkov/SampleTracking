package com.atlassian.jira;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.Collections;
import java.util.Date;

/**
 * @since v4.0
 */
public class TestJiraDataTypeImpl extends ListeningTestCase
{
    @Test
    public void testAsString() throws Exception
    {
        assertTrue(Collections.singleton("java.lang.Object").containsAll(JiraDataTypes.ALL.asStrings()));
        assertTrue(Collections.singleton("java.util.Date").containsAll(JiraDataTypes.DATE.asStrings()));
        assertTrue(CollectionBuilder.newBuilder("java.lang.Object", "com.atlassian.jira.issue.Issue").asList().containsAll(new JiraDataTypeImpl(CollectionBuilder.list(Object.class, Issue.class)).asStrings()));
    }

    @Test
    public void testMatch() throws Exception
    {
        assertFalse(JiraDataTypes.DATE.matches(JiraDataTypes.ISSUE_TYPE));
        assertTrue(JiraDataTypes.ALL.matches(JiraDataTypes.ISSUE_TYPE));
        assertTrue(JiraDataTypes.ISSUE_TYPE.matches(JiraDataTypes.ALL));
        assertTrue(JiraDataTypes.ISSUE_TYPE.matches(JiraDataTypes.ISSUE_TYPE));

        final JiraDataTypeImpl bigType = new JiraDataTypeImpl(CollectionBuilder.list(Date.class, Issue.class, String.class));
        assertTrue(bigType.matches(JiraDataTypes.DATE));
        assertTrue(JiraDataTypes.DATE.matches(bigType));
        assertTrue(bigType.matches(JiraDataTypes.ISSUE));
        assertTrue(JiraDataTypes.ISSUE.matches(bigType));
        assertTrue(bigType.matches(JiraDataTypes.TEXT));
        assertTrue(JiraDataTypes.TEXT.matches(bigType));
        assertTrue(JiraDataTypes.ALL.matches(bigType));
    }
}
