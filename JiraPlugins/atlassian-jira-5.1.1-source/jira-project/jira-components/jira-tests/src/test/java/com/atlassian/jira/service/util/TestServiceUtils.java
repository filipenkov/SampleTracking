/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.util;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

public class TestServiceUtils extends LegacyJiraMockTestCase
{
    private Mock issueManagerMock;
    private MockIssue issue;

    public TestServiceUtils(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        issue = new MockIssue(1L);
    }

    public void testGetIssueSuccessful()
    {
        _testSuccessfulIssue("PRJ-123   ", "PRJ-123");
        _testSuccessfulIssue("asdf PRJ-123   ", "PRJ-123");
        _testSuccessfulIssue("ABC-PRJ ABC-456  ", "ABC-456");
        _testSuccessfulIssue("PRJ-123\n   ", "PRJ-123");
        _testSuccessfulIssue("PRJ-123\r\n   ", "PRJ-123"); //Test verifies JRA-1616
    }

    private void _testSuccessfulIssue(final String searchString, final String result)
    {
        issueManagerMock = new Mock(IssueManager.class);
        ManagerFactory.addService(IssueManager.class, (IssueManager) issueManagerMock.proxy());
        issueManagerMock.expectAndReturn("getIssueObject", P.args(new IsEqual(result)), issue);
        ServiceUtils.findIssueObjectInString(searchString);
        issueManagerMock.verify();
    }
}
