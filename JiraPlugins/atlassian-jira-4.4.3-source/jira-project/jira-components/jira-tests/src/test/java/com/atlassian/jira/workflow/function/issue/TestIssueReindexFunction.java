/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.core.util.map.EasyMap;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

import java.util.Map;

public class TestIssueReindexFunction extends LegacyJiraMockTestCase
{
    private Mock mock;

    public TestIssueReindexFunction(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        mock = new Mock(IssueIndexManager.class);
        ManagerFactory.addService(IssueIndexManager.class, (IssueIndexManager) mock.proxy());
    }

    public void testReindexIssueFunction() throws GenericEntityException
    {
        GenericValue issueGV = EntityUtils.createValue("Issue", EasyMap.build("id", new Long(1), "assignee", "Test Assignee", "key", "Test-1", "resolution", "Test Resolution"));
        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
        mockIssue.expectAndReturn("getGenericValue", issueGV);
        Issue issueObject = (Issue) mockIssue.proxy();
        mock.expectVoid("reIndex", P.args(new IsEqual(issueGV)));

        Map input = EasyMap.build("issue", issueObject);
        IssueReindexFunction irf = new IssueReindexFunction();
        irf.execute(input, null, null);

        mock.verify();
        mockIssue.verify();
    }
}
