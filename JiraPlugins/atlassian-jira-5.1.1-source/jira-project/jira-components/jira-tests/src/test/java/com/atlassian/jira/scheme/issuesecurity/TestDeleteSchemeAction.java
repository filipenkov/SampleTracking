/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme.issuesecurity;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.MockEventPublisher;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManager;
import com.atlassian.jira.issue.security.IssueSecuritySchemeManagerImpl;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.scheme.DefaultSchemeFactory;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.web.action.admin.issuesecurity.DeleteScheme;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

public class TestDeleteSchemeAction extends AbstractWebworkTestCase
{
    SchemeManager issueSchemeManager;
    private DeleteScheme deleteScheme;
    private GenericValue scheme;

    public TestDeleteSchemeAction(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        DefaultSchemeFactory schemeFactory = new DefaultSchemeFactory();
        OfBizDelegator ofBizDelegator = new DefaultOfBizDelegator(CoreFactory.getGenericDelegator());
        AssociationManager associationManager = CoreFactory.getAssociationManager();
        EventPublisher eventPublisher = new MockEventPublisher();
        ManagerFactory.addService(IssueSecuritySchemeManager.class, new IssueSecuritySchemeManagerImpl(null, null, null, schemeFactory, eventPublisher, associationManager, ofBizDelegator, null));
        issueSchemeManager = ManagerFactory.getIssueSecuritySchemeManager();

        deleteScheme = new DeleteScheme();

        //The scheme manager should be set correctly
        assertEquals(deleteScheme.getSchemeManager(), issueSchemeManager);

        //create a scheme to delete
        scheme = issueSchemeManager.createScheme("IScheme", "Test Desc");
    }

    public void testDeleteIssueSecuritySchemeWithAssociatedProjects() throws Exception
    {
        // Create a project
        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "Test Project"));

        // Associate the scheme with the
        ManagerFactory.getIssueSecuritySchemeManager().addSchemeToProject(project, scheme);

        //delete the scheme
        deleteScheme.setSchemeId(scheme.getLong("id"));
        deleteScheme.setConfirmed(true);

        String result = deleteScheme.execute();
        assertEquals(Action.INPUT, result);
        checkSingleElementCollection(deleteScheme.getErrorMessages(), "The scheme cannot be deleted when there are projects associated with it.");
    }

    public void testDeleteIssueSecurityScheme() throws Exception
    {
        //use a mock servlet response
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewIssueSecuritySchemes.jspa");

        assertTrue(issueSchemeManager.getSchemes().size() == 1);

        //delete the scheme
        deleteScheme.setSchemeId(scheme.getLong("id"));
        deleteScheme.setConfirmed(true);

        String result = deleteScheme.execute();

        //the scheme should be gone
        assertTrue(issueSchemeManager.getSchemes().size() == 0);

        //there should be no errors
        assertEquals(0, deleteScheme.getErrors().size());

        assertEquals(Action.NONE, result);

        response.verify();
    }
}
