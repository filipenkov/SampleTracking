package com.atlassian.jira.notification.type;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.type.enterprise.ComponentLead;
import com.mockobjects.dynamic.Mock;
import com.atlassian.jira.local.AbstractUsersTestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Unit tests to test the ComponentLead notification type.
 */
public class TestComponentLead extends AbstractUsersTestCase
{
    public TestComponentLead(String s)
    {
        super(s);
    }

    // Call the getRecipients method of ComponentLead with a component that has no lead and make
    // sure that no exception is thrown JRA-9993 and that the list is empty
    public void testGetRecipientsWithNoLeadSetOnComponent()
    {
        Mock mockIssue = new Mock(Issue.class);
        HashMap fields = new HashMap();
        fields.put("lead", null);
        MockGenericValue component = new MockGenericValue("Component", fields);
        List components = new ArrayList();
        components.add(component);
        mockIssue.expectAndReturn("getComponents", components);
        IssueEvent event = new IssueEvent((Issue) mockIssue.proxy(), new HashMap(), null, null);
        ComponentLead componentLead = new ComponentLead(ComponentAccessor.getJiraAuthenticationContext());

        try
        {
            assertEquals(componentLead.getRecipients(event, null).size(), 0);
        }
        catch(Exception e)
        {
            fail();
        }
    }

    public void testGetRecipeientsWithNoUserForComponentLead()
    {
        Mock mockIssue = new Mock(Issue.class);
        HashMap fields = new HashMap();
        fields.put("lead", "dudeUser");
        MockGenericValue component = new MockGenericValue("Component", fields);
        List components = new ArrayList();
        components.add(component);
        mockIssue.expectAndReturn("getComponents", components);
        IssueEvent event = new IssueEvent((Issue) mockIssue.proxy(), new HashMap(), null, null);
        ComponentLead componentLead = new ComponentLead(ComponentAccessor.getJiraAuthenticationContext());

        try
        {
            assertEquals(componentLead.getRecipients(event, null).size(), 0);
        }
        catch(Exception e)
        {
            fail();
        }
    }
}
