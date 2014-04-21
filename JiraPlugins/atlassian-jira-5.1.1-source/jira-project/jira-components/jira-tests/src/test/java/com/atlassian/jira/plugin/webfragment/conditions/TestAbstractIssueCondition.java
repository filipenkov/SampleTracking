package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;

import java.util.HashMap;

import org.easymock.IMocksControl;
import org.easymock.EasyMock;

public class TestAbstractIssueCondition extends ListeningTestCase
{
    @Test
    public void testNoIssue()
    {
        final HashMap<String, Object> params = new HashMap<String, Object>();
        final JiraHelper jiraHelper = new JiraHelper(null, null, params);

        final AbstractIssueCondition condition = new AbstractIssueCondition()
        {

            public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
            {
                return true;
            }
        };

        assertFalse(condition.shouldDisplay(null, jiraHelper));

    }
    @Test
    public void testTrue()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);

        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("issue", issue);

        final JiraHelper jiraHelper = new JiraHelper(null, null, params);

        final AbstractIssueCondition condition = new AbstractIssueCondition()
        {

            public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
            {
                return true;
            }
        };

        mocksControl.replay();
        assertTrue(condition.shouldDisplay(null, jiraHelper));
        mocksControl.verify();

    }
    @Test
    public void testFalse()
    {
        final IMocksControl mocksControl = EasyMock.createControl();
        final Issue issue = mocksControl.createMock(Issue.class);

        final HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("issue", issue);

        final JiraHelper jiraHelper = new JiraHelper(null, null, params);

        final AbstractIssueCondition condition = new AbstractIssueCondition()
        {

            public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
            {
                return false;
            }
        };

        mocksControl.replay();
        assertFalse(condition.shouldDisplay(null, jiraHelper));
        mocksControl.verify();

    }

}
