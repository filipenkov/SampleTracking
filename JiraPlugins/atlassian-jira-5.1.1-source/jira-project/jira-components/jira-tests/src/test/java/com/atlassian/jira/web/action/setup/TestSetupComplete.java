/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.setup;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.license.LicenseJohnsonEventRaiser;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.upgrade.UpgradeManager;
import com.atlassian.plugin.event.PluginEventManager;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.servlet.MockHttpServletResponse;
import com.mockobjects.servlet.MockServletContext;
import webwork.action.Action;
import webwork.action.ServletActionContext;

import java.util.Collections;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TestSetupComplete extends LegacyJiraMockTestCase
{
    private SetupComplete sc;
    private Mock upgradeManager;
    private LicenseJohnsonEventRaiser licenseJohnsonEventRaiser;
    private JiraLicenseService jiraLicenseService;
    private SubTaskManager subTaskManager;
    private FieldLayoutManager fieldLayoutManager;
    private PluginEventManager pluginEventManager;

    public TestSetupComplete(final String s)
    {
        super(s);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        upgradeManager = new Mock(UpgradeManager.class);

        //defualt to no error messages
        upgradeManager.expectAndReturn("doSetupUpgrade", Collections.EMPTY_LIST);
        ManagerFactory.addService(UpgradeManager.class, (UpgradeManager) upgradeManager.proxy());

        licenseJohnsonEventRaiser = createMock(LicenseJohnsonEventRaiser.class);
        jiraLicenseService = createMock(JiraLicenseService.class);

        final MockServletContext mockServletContext = new MockServletContext();
        ServletActionContext.setServletContext(mockServletContext);

        subTaskManager = createMock(SubTaskManager.class);

        fieldLayoutManager = createMock(FieldLayoutManager.class);
        pluginEventManager = createMock(PluginEventManager.class);

        //mock out the license test
        sc = new SetupComplete(ComponentManager.getInstance().getUpgradeManager(), (LicenseJohnsonEventRaiser) licenseJohnsonEventRaiser, jiraLicenseService, subTaskManager, fieldLayoutManager, null, pluginEventManager)
        {
            @Override
            protected boolean licenseTooOld()
            {
                return false;
            }

            @Override
            void setWikiRendererOnAllRenderableFields()
            {
            }
        };
    }

    @Override
    protected void tearDown() throws Exception
    {
    }

    public void testDoDefault() throws Exception
    {
        assertEquals(Action.SUCCESS, sc.doDefault());
    }

    public void testExecuteWhenAlreadySetup() throws Exception
    {
        sc.getApplicationProperties().setString(APKeys.JIRA_SETUP, "true");
        assertEquals("setupalready", sc.execute());
    }

    public void testExecuteWhenNotSetupYet() throws Exception
    {
        sc.getApplicationProperties().setString(APKeys.JIRA_SETUP, null);
        assertEquals(Action.SUCCESS, sc.execute());
    }

    public void testExecuteSetsApplicationProperties() throws Exception
    {
        subTaskManager.enableSubTasks();
        expectLastCall();
        replay(subTaskManager, fieldLayoutManager);

        sc.getApplicationProperties().setString(APKeys.JIRA_SETUP, null);
        assertEquals(Action.SUCCESS, sc.execute());

        // default application properties
        assertEquals("true", sc.getApplicationProperties().getString(APKeys.JIRA_SETUP));
        assertTrue(!sc.getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED));
        assertTrue(!sc.getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS));
        assertTrue(!sc.getApplicationProperties().getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT));
        assertTrue(sc.getApplicationProperties().getOption(APKeys.JIRA_OPTION_VOTING));
        assertTrue(sc.getApplicationProperties().getOption(APKeys.JIRA_OPTION_WATCHING));

        verify(subTaskManager);
    }

    public void testExecuteCallsUpgradeManager() throws Exception
    {
        //return no errors
        upgradeManager.expectAndReturn("doSetupUpgrade", Collections.EMPTY_LIST);
        assertEquals(Action.SUCCESS, sc.execute());
        upgradeManager.verify();
    }

    public void testExecuteUpgradeManagerErrorsAreAdded() throws Exception
    {
        //return 2 errors
        upgradeManager.expectAndReturn("doSetupUpgrade", EasyList.build("Error1", "Error2"));
        assertEquals(Action.ERROR, sc.execute());
        assertEquals(2, sc.getErrorMessages().size());
        assertTrue(sc.getErrorMessages().contains("Error1"));
        assertTrue(sc.getErrorMessages().contains("Error2"));

        upgradeManager.verify();
    }

    public void testEventNotRaised() throws Exception
    {
        final SetupComplete licenseValidSetupComplete = new SetupComplete(ComponentManager.getInstance().getUpgradeManager(), licenseJohnsonEventRaiser, jiraLicenseService, subTaskManager, fieldLayoutManager, null, pluginEventManager)
        {
            @Override
            protected boolean licenseTooOld()
            {
                //license is not too old for build
                return false;
            }

            @Override
            void setWikiRendererOnAllRenderableFields()
            {
            }
        };

        final String result = licenseValidSetupComplete.execute();
        assertEquals(Action.SUCCESS, result);
    }

    public void testEventRaised() throws Exception
    {
        final MockHttpServletResponse mockHttpServletResponse = JiraTestUtil.setupExpectedRedirect("/secure/errors.jsp");
        final SetupComplete licenseValidSetupComplete = new SetupComplete(ComponentManager.getInstance().getUpgradeManager(), licenseJohnsonEventRaiser, jiraLicenseService, subTaskManager, fieldLayoutManager, null, pluginEventManager)
        {
            @Override
            protected boolean licenseTooOld()
            {
                //license is too old for build
                return true;
            }

            @Override
            void setWikiRendererOnAllRenderableFields()
            {
            }
        };

        final String result = licenseValidSetupComplete.execute();
        //returns SUCCESS as it redirects the page to the error page - JRA-11988
        assertEquals(Action.NONE, result);
        mockHttpServletResponse.verify();
    }
}
