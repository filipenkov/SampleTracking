/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme.issuesecurity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.MockEventPublisher;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManagerImpl;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.scheme.DefaultSchemeFactory;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.core.util.map.EasyMap;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

public class TestCopySchemeAction extends AbstractWebworkTestCase
{
    SchemeManager issueSchemeManager;

    public TestCopySchemeAction(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        DefaultSchemeFactory schemeFactory = new DefaultSchemeFactory();
        OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());
        EventPublisher eventPublisher = new MockEventPublisher();
        ManagerFactory.addService(IssueSecuritySchemeManager.class, new IssueSecuritySchemeManagerImpl(null, null, null, schemeFactory, eventPublisher, null, ofBizDelegator, null));
        issueSchemeManager = ManagerFactory.getIssueSecuritySchemeManager();
    }

    public void testCopyIssueSecurityScheme() throws Exception
    {
        //use a mock servlet response
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewIssueSecuritySchemes.jspa");

        com.atlassian.jira.web.action.admin.issuesecurity.CopyScheme copyScheme = new com.atlassian.jira.web.action.admin.issuesecurity.CopyScheme();

        GenericValue scheme = issueSchemeManager.createScheme("IScheme", "Test Desc");

        //create a security level
        GenericValue newlevel = EntityUtils.createValue("SchemeIssueSecurityLevels", EasyMap.build("scheme", scheme.getLong("id"), "name", "name", "description", "description"));

        issueSchemeManager.createSchemeEntity(scheme, new SchemeEntity("type", "param", new Long(1)));

        //The scheme manager should be set correctly
        assertEquals(copyScheme.getSchemeManager(), issueSchemeManager);

        assertTrue(issueSchemeManager.getSchemes().size() == 1);
        assertTrue(issueSchemeManager.getEntities(scheme).size() == 1);

        copyScheme.setSchemeId(scheme.getLong("id"));

        //edit the scheme
        String result = copyScheme.execute();

        //the new scheme should be there
        assertTrue(issueSchemeManager.getSchemes().size() == 2);

        //The name should have a prefix of the original
        assertTrue(issueSchemeManager.getScheme(new Long(2)).getString("name").equals("Copy of IScheme"));

        GenericValue newScheme = issueSchemeManager.getScheme("Copy of IScheme");

        assertTrue(issueSchemeManager.getEntities(scheme).size() == issueSchemeManager.getEntities(issueSchemeManager.getScheme(new Long(2))).size());

        //there should be no errors
        assertEquals(0, copyScheme.getErrors().size());

        assertEquals(Action.NONE, result);

        response.verify();
    }
}
