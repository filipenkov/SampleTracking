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
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

public class TestEditSchemeAction extends AbstractWebworkTestCase
{
    SchemeManager issueSchemeManager;

    public TestEditSchemeAction(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        DefaultSchemeFactory schemeFactory = new DefaultSchemeFactory();
        OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());
        ManagerFactory.addService(IssueSecuritySchemeManager.class, new IssueSecuritySchemeManagerImpl(null, null, null, schemeFactory, null, null, ofBizDelegator, null));
        issueSchemeManager = ManagerFactory.getIssueSecuritySchemeManager();
    }

    public void testEditIssueSecurityScheme() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewIssueSecuritySchemes.jspa");

        com.atlassian.jira.web.action.admin.issuesecurity.EditScheme editScheme = new com.atlassian.jira.web.action.admin.issuesecurity.EditScheme();

        GenericValue scheme = issueSchemeManager.createScheme("IScheme", "Test Desc");

        //set the scheme details
        editScheme.setName("New Name");
        editScheme.setDescription("New Description");

        //The scheme manager should be set correctly
        assertEquals(editScheme.getSchemeManager(), issueSchemeManager);

        assertTrue(issueSchemeManager.getSchemes().size() == 1);

        editScheme.setSchemeId(scheme.getLong("id"));

        //edit the scheme
        String result = editScheme.execute();

        //the new scheme should be there
        assertTrue(issueSchemeManager.getScheme(scheme.getLong("id")).getString("name").equals("New Name"));
        assertTrue(issueSchemeManager.getScheme(scheme.getLong("id")).getString("description").equals("New Description"));

        //there should be no errors
        assertEquals(0, editScheme.getErrors().size());

        assertEquals(Action.NONE, result);

        response.verify();
    }
}
