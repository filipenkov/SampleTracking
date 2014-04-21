package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.mock.MockProjectRoleManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.permission.DefaultPermissionSchemeManager;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

/**
 * Author: jdoklovic
 */
public class TestUpgradeTask_Build641 {

    private GenericValue defaultScheme;

    @Test
    public void doUpgradeRunsWithoutError() throws Exception {
        //this really just makes sure it runs ok.
        //Since it only wraps a couple of calls to the permissionManager, there's not too much to test.

        //setup the default scheme
        defaultScheme = new MockGenericValue("PermissionScheme", EasyMap.build("name", "default scheme"));
        final ProjectRoleManager projectRoleManager = createMock(ProjectRoleManager.class);

        PermissionManager permissionManager = createMock(PermissionManager.class);
        PermissionSchemeManager permissionSchemeManager = new MockPermissionSchemeManager();

        expect(projectRoleManager.getProjectRole("Users")).andReturn(new MockProjectRoleManager.MockProjectRole(10099L, "Users", "Users desc"));
        permissionManager.addPermission(Permissions.VIEW_WORKFLOW_READONLY, defaultScheme, "10099", "projectrole");
        replay(projectRoleManager, permissionManager);

        UpgradeTask_Build641 task = new UpgradeTask_Build641(permissionManager,permissionSchemeManager, projectRoleManager);
        task.doUpgrade(false);

        assertEquals(task.getBuildNumber(),"641");

        verify(projectRoleManager, permissionManager);
    }

    private class MockPermissionSchemeManager extends DefaultPermissionSchemeManager
    {
        public MockPermissionSchemeManager()
        {
            super(null, null, null, null, null, null, null);
        }

        @Override
        public GenericValue getDefaultScheme() throws GenericEntityException {
            return defaultScheme;
        }
    }
}
