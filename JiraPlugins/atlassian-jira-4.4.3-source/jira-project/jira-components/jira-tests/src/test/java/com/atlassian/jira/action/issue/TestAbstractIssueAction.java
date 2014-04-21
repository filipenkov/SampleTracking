/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.action.issue.MockAbstractIssueUpdateAction;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;

public class TestAbstractIssueAction extends LegacyJiraMockTestCase
{
    public TestAbstractIssueAction(String s)
    {
        super(s);
    }

    public void testValidationNoIssue()
    {
        MockAbstractIssueUpdateAction action = new MockAbstractIssueUpdateAction();

        action.validate();
        assertEquals(1, action.getErrorMessages().size());
        assertEquals("You did not specify an issue.", action.getErrorMessages().iterator().next());
    }

    public void testValidationWithIssue()
    {
        ManagerFactory.addService(IssueManager.class, new MockIssueManager());

        MockAbstractIssueUpdateAction action = new MockAbstractIssueUpdateAction();
        MockGenericValue gv = new MockGenericValue("Issue", EasyMap.build("id", new Long(10)));
        action.setIssue(gv);

        action.validate();
        assertEquals(0, action.getErrorMessages().size());
    }
}
