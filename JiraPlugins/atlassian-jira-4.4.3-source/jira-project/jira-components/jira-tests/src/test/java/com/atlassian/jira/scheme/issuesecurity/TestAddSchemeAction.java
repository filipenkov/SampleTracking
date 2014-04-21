/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme.issuesecurity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManagerImpl;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.scheme.DefaultSchemeFactory;
import com.atlassian.jira.scheme.SchemeManager;
import com.mockobjects.servlet.MockHttpServletResponse;
import webwork.action.Action;

public class TestAddSchemeAction extends AbstractWebworkTestCase
{
    SchemeManager issueSchemeManager;

    public TestAddSchemeAction(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());
        DefaultSchemeFactory schemeFactory = new DefaultSchemeFactory();
        ManagerFactory.addService(IssueSecuritySchemeManager.class, new IssueSecuritySchemeManagerImpl(null, null, null, schemeFactory, null, null, ofBizDelegator, null));
        issueSchemeManager = ManagerFactory.getIssueSecuritySchemeManager();
    }

    public void testAddIssueSecurityScheme() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewIssueSecuritySchemes!default.jspa?schemeId=1");

        com.atlassian.jira.web.action.admin.issuesecurity.AddScheme addScheme = new com.atlassian.jira.web.action.admin.issuesecurity.AddScheme();

        //set the scheme details
        addScheme.setName("test scheme");
        addScheme.setDescription("test scheme");

        //The scheme manager should be set correctly
        assertEquals(addScheme.getSchemeManager(), issueSchemeManager);

        assertTrue(issueSchemeManager.getSchemes().size() == 0);

        //add the scheme
        String result = addScheme.execute();

        //the new scheme should be there
        assertTrue(issueSchemeManager.getSchemes().size() == 1);

        //there should be no errors
        assertEquals(0, addScheme.getErrors().size());

        assertEquals(Action.NONE, result);

        response.verify();
    }
}
