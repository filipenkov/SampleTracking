/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.acceptance.issuevisibility;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.dao.application.ApplicationDAO;
import com.atlassian.crowd.embedded.api.DirectoryType;
import com.atlassian.crowd.embedded.api.OperationType;
import com.atlassian.crowd.embedded.api.PasswordCredential;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.spi.DirectoryDao;
import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.model.application.ApplicationType;
import com.atlassian.crowd.model.directory.DirectoryImpl;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.SystemPropertyKeys;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.jelly.service.EmbededJellyContext;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.UpgradeTask;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build101;
import com.atlassian.jira.upgrade.tasks.UpgradeTask_Build83;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import org.easymock.classextension.EasyMock;

import java.io.File;

public abstract class AbstractTestPermissionsStatisticsAndSearching extends AbstractUsersIndexingTestCase
{
    private FieldVisibilityBean origFieldVisibilityBean;

    public AbstractTestPermissionsStatisticsAndSearching(String s)
    {
        super(s);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        origFieldVisibilityBean = ComponentManager.getComponentInstanceOfType(FieldVisibilityBean.class);
        final FieldVisibilityBean visibilityBean = EasyMock.createMock(FieldVisibilityBean.class);
        EasyMock.expect(visibilityBean.isFieldHidden((String)EasyMock.anyObject(), (Issue)EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.replay(visibilityBean);
        ManagerFactory.addService(FieldVisibilityBean.class, visibilityBean);

        System.setProperty(SystemPropertyKeys.JELLY_SYSTEM_PROPERTY, "true");
        User u = new MockUser("misc-user");
        ComponentAccessor.getCrowdService().addUser(u, "password");
        ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, null);

        UtilsForTests.getTestEntity("PermissionScheme", EasyMap.build("name", "Default Permission Scheme", "description", "Permission scheme", "id", 0L));
        UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "Bug", "sequence", 1L));

        // Create a Issue Type Screen Scheme as there should always be one in the database
        // This is a huge hack - but it is the easiest way to initialise the database to have the field screens and field screen schemes
        // that should be there at all times
        UpgradeTask upgradeTask = JiraUtils.loadComponent(UpgradeTask_Build83.class);
        upgradeTask.doUpgrade(false);

        UpgradeTask upgradeTask101 = JiraUtils.loadComponent(UpgradeTask_Build101.class);
        upgradeTask101.doUpgrade(false);

        // Add in a Crowd Embedded Application for user-based Jelly Tests
        Application application = ApplicationImpl.newInstance("crowd-embedded", ApplicationType.CROWD);
        ComponentAccessor.getComponentOfType(ApplicationDAO.class).add(application, new PasswordCredential("foo", true));
        // Add in a Crowd Embedded Directory for user-based Jelly Tests
        DirectoryImpl directory = new DirectoryImpl("Internal", DirectoryType.INTERNAL, "xxx");
        directory.addAllowedOperation(OperationType.CREATE_USER);
        directory.addAllowedOperation(OperationType.UPDATE_USER);
        directory.addAllowedOperation(OperationType.DELETE_USER);
        ComponentAccessor.getComponentOfType(DirectoryDao.class).add(directory);

        String outputFileName =  getOutputFileName();
        try
        {
            EmbededJellyContext embededJellyContext = new EmbededJellyContext();
            File f = new File(getJellyDataScriptName());
            embededJellyContext.runScript(f.getAbsolutePath(), outputFileName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        File outputF = new File(outputFileName);
        outputF.deleteOnExit();

        // Reindex - We need to do this as the IssueIndexListener is not inplace for Unit tests.
        ComponentAccessor.getIssueIndexManager().reIndexAll();
    }

    public void tearDown() throws Exception
    {
        ManagerFactory.addService(FieldVisibilityBean.class, origFieldVisibilityBean);
        System.setProperty(SystemPropertyKeys.JELLY_SYSTEM_PROPERTY, "false");
        super.tearDown();
    }

    protected abstract String getOutputFileName();

    protected String getJellyDataScriptName()
    {
        final String currentDir = System.getProperty("user.dir");
        return (new File(this.getClass().getResource("/" + this.getClass().getName().replace('.', '/') + ".class").getFile()).getParent() + "/").substring(currentDir.length() + 1);
    }
}
