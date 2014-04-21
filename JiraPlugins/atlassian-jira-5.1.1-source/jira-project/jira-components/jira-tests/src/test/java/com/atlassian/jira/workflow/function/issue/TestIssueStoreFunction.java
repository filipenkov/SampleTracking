/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.mockobjects.dynamic.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Map;

public class TestIssueStoreFunction extends LegacyJiraMockTestCase
{
    public TestIssueStoreFunction(String s)
    {
        super(s);
    }

    public void testIssueStoreFunctionNoIssue() throws GenericEntityException
    {
        Map input = new HashMap();
        IssueStoreFunction isf = new IssueStoreFunction();
        isf.execute(input, null, null);
        assertTrue(CoreFactory.getGenericDelegator().findAll("Issue").isEmpty());
    }

    public void testIssueStoreFunction() throws GenericEntityException
    {
        GenericValue issue = EntityUtils.createValue("Issue", EasyMap.build("id", new Long(1), "assignee", "Test Assignee", "key", "Test-1", "resolution", "Test Resolution"));

        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
        mockIssue.expectVoid("store");

        Map input = EasyMap.build("issue", mockIssue.proxy());
        IssueStoreFunction isf = new IssueStoreFunction();
        isf.execute(input, null, null);

        mockIssue.verify();
    }
}
