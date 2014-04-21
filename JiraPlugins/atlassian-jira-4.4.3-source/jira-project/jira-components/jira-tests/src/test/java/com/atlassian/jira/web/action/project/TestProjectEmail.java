/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.project;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectKeys;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.mail.MailException;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.mockobjects.servlet.MockHttpServletResponse;
import com.opensymphony.module.propertyset.PropertySet;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.io.IOException;

public class TestProjectEmail extends AbstractUsersTestCase
{
    private long projectId = 1;
    private String serverFromAddress = "server@test.com";
    private String testFromAddress = "test1@test.com";
    private String invalidTestFromAddress = "test.com";
    private String nullTestFromAddress = "";

    public TestProjectEmail(String s)
    {
        super(s);
    }

    public void testGettersSetters()
    {
        Mock mockProjectManager = new Mock(ProjectManager.class);
        Mock mockMailServerManager = new Mock(MailServerManager.class);

        ProjectEmail projectEmail = new ProjectEmail((ProjectManager) mockProjectManager.proxy(), (MailServerManager) mockMailServerManager.proxy());

        projectEmail.setProjectId(projectId);
        assertEquals(projectId, projectEmail.getProjectId());

        projectEmail.setFromAddress(testFromAddress);
        assertEquals(testFromAddress, projectEmail.getFromAddress());
    }

    public void testDoDefaultWithInvalidProjectId()
    {
        Mock mockProjectManager = new Mock(ProjectManager.class);
        Mock mockMailServerManager = new Mock(MailServerManager.class);

        ProjectEmail projectEmail = new ProjectEmail((ProjectManager) mockProjectManager.proxy(), (MailServerManager) mockMailServerManager.proxy());

        // Invalid project id
        projectEmail.setProjectId(0L);

        try
        {
            projectEmail.doDefault();
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException iae)
        {
            assertNotNull(iae.getMessage());
        }
        catch (Exception e)
        {
            fail("IllegalArgumentException should have been caught already.");
        }
    }

    public void testDoDefaultWithMailServerException()
    {
        Mock mockProjectManager = new Mock(ProjectManager.class);
        Mock mockMailServerManager = new Mock(MailServerManager.class);
        GenericValue project;

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "name", "ProjectOne"));
        MockProject projectObj = new MockProject(1, "FTH", "ProjectOne", project);

        mockProjectManager.expectAndReturn("getProjectObj", P.args(new IsEqual(projectId)), projectObj);
        mockMailServerManager.expectAndThrow("getDefaultSMTPMailServer", new MailException());

        ProjectEmail projectEmail = new ProjectEmail((ProjectManager) mockProjectManager.proxy(), (MailServerManager) mockMailServerManager.proxy());

        projectEmail.setProjectId(1L);

        try
        {
            projectEmail.doDefault();
            fail("MailException should have been thrown.");
        }
        catch (MailException me)
        {
            assertNotNull(me);
        }
        catch (Exception e)
        {
            //  Not testing for this exception here.
            fail("MailException should have been caught already.");
            assertNull(e);
        }
        mockProjectManager.verify();
        mockMailServerManager.verify();
    }

    public void testDoDefaultWithPropertySet() throws IOException
    {
        Mock mockProjectManager = new Mock(ProjectManager.class);
        Mock mockMailServerManager = new Mock(MailServerManager.class);
        GenericValue project;

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "name", "ProjectOne"));

        mockProjectManager.expectAndReturn("getProjectObj", P.args(new IsEqual(projectId)), new MockProject(1, "KEY", "ProjectOne"));

        ProjectEmail projectEmail = new ProjectEmail((ProjectManager) mockProjectManager.proxy(), (MailServerManager) mockMailServerManager.proxy());

        // Create a property set
        PropertySet ps = OFBizPropertyUtils.getPropertySet(project);
        ps.setString(ProjectKeys.EMAIL_SENDER, testFromAddress);

        projectEmail.setProjectId(1L);

        try
        {
            projectEmail.doDefault();
        }
        catch (Exception e)
        {
            fail("No exceptions should have been thrown: " + e);
        }

        assertEquals(testFromAddress, projectEmail.getFromAddress());
        mockProjectManager.verify();
    }

    public void testDoDefaultWithoutPropertySet() throws IOException
    {
        Mock mockProjectManager = new Mock(ProjectManager.class);
        Mock mockMailServerManager = new Mock(MailServerManager.class);
        Mock mockSMTPMailServer = new Mock(SMTPMailServer.class);
        GenericValue project;

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "name", "ProjectOne"));
        MockProject projectObj = new MockProject(1, "FTH", "ProjectOne", project);

        mockProjectManager.expectAndReturn("getProjectObj", P.args(new IsEqual(projectId)), projectObj);
        mockMailServerManager.expectAndReturn("getDefaultSMTPMailServer", mockSMTPMailServer.proxy());
        mockSMTPMailServer.expectAndReturn("getDefaultFrom", serverFromAddress);

        ProjectEmail projectEmail = new ProjectEmail((ProjectManager) mockProjectManager.proxy(), (MailServerManager) mockMailServerManager.proxy());

        projectEmail.setProjectId(1L);

        try
        {
            projectEmail.doDefault();
        }
        catch (Exception e)
        {
            fail("No exceptions should have been thrown: " + e);
        }
        assertEquals(serverFromAddress, projectEmail.getFromAddress());
        mockProjectManager.verify();
        mockMailServerManager.verify();
        mockSMTPMailServer.verify();
    }

    public void testDoExecute() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("/plugins/servlet/project-config/FTH");
        Mock mockProjectManager = new Mock(ProjectManager.class);
        Mock mockMailServerManager = new Mock(MailServerManager.class);
        GenericValue project;

        project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "name", "ProjectOne"));
        MockProject projectObj = new MockProject(1, "FTH", "ProjectOne", project);

        mockProjectManager.expectAndReturn("getProjectObj", P.args(new IsEqual(projectId)), projectObj);

        ProjectEmail projectEmail = new ProjectEmail((ProjectManager) mockProjectManager.proxy(), (MailServerManager) mockMailServerManager.proxy());

        projectEmail.setProjectId(1L);
        projectEmail.setFromAddress(testFromAddress);

        assertEquals(Action.NONE, projectEmail.execute());

        // Get the property set
        PropertySet ps = OFBizPropertyUtils.getPropertySet(project);
        assertEquals(testFromAddress, ps.getString(ProjectKeys.EMAIL_SENDER));
        mockProjectManager.verify();
        response.verify();
    }

    public void testExecuteWithInvalidEmail()
    {
        Mock mockProjectManager = new Mock(ProjectManager.class);
        Mock mockMailServerManager = new Mock(MailServerManager.class);

        ProjectEmail projectEmail = new ProjectEmail((ProjectManager) mockProjectManager.proxy(), (MailServerManager) mockMailServerManager.proxy());

        projectEmail.setProjectId(1L);
        projectEmail.setFromAddress(invalidTestFromAddress);

        try
        {
            assertEquals(Action.INPUT, projectEmail.execute());
            assertNotNull(projectEmail.getErrors().get("fromAddress"));
        }
        catch (Exception e)
        {
            fail("No exceptions should have been thrown: " + e);
        }
    }
}
