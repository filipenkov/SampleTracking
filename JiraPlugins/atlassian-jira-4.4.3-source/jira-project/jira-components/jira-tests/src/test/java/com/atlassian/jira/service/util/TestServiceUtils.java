/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.util;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.project.ProjectManager;

import org.ofbiz.core.entity.GenericValue;

import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;

public class TestServiceUtils extends LegacyJiraMockTestCase
{
    private Mock projectManagerMock;
    private Mock issueManagerMock;
    private GenericValue TEST_PROJECT_VALUE;
    private GenericValue TEST_ISSUE_VALUE;

    public TestServiceUtils(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        TEST_PROJECT_VALUE = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1)));
        TEST_ISSUE_VALUE = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1)));
    }

    public void testGetProjectSuccessful()
    {
        _testSuccessfulProject("PRJ   ", "PRJ");
        _testSuccessfulProject("asdf PRJ   ", "PRJ");
        _testSuccessfulProject("ABC PRJ   ", "ABC");
        _testSuccessfulProject("PRJ\n   ", "PRJ");
        _testSuccessfulProject("PRJ\r\n   ", "PRJ"); //Test verifies JRA-1616
    }

    private void _testSuccessfulProject(final String searchString, final String result)
    {
        projectManagerMock = new Mock(ProjectManager.class);
        ManagerFactory.addService(ProjectManager.class, (ProjectManager) projectManagerMock.proxy());
        projectManagerMock.expectAndReturn("getProjectByKey", P.args(new IsEqual(result)), TEST_PROJECT_VALUE);
        ServiceUtils.getProject(searchString);
        projectManagerMock.verify();
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
        issueManagerMock.expectAndReturn("getIssue", P.args(new IsEqual(result)), TEST_ISSUE_VALUE);
        ServiceUtils.findIssueInString(searchString);
        issueManagerMock.verify();
    }
}
