package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.web.action.setup.AbstractSetupAction;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

import java.util.*;

/**
 * Tests to make sure that the default permission scheme gets updated to use roles instead of groups.
 */
public class TestUpgradeTask_Build176 extends LegacyJiraMockTestCase
{

    private UpgradeTask_Build176 upgradeTask;
    private static final Long ADMIN_ID = new Long(123);
    private static final Long DEVEL_ID = new Long(456);
    private static final Long USER_ID = new Long(789);

    protected void setUp() throws Exception
    {
        super.setUp();

    }

    // Test that the upgrade task will not run when there are no projects
    public void testUpgradeConditionNoProjects() throws Exception
    {
        Mock mockProjectManager = new Mock(ProjectManager.class);
        // Create a list of size 1
        Collection projects = new ArrayList();
        projects.add(new Object());
        mockProjectManager.expectAndReturn("getProjects", projects);

        Mock mockProjectRoleManager = new Mock(ProjectRoleManager.class);
        mockProjectRoleManager.expectNotCalled("getProjectRoles");

        upgradeTask = new UpgradeTask_Build176((ProjectManager)mockProjectManager.proxy(), null, (ProjectRoleManager) mockProjectRoleManager.proxy());
        upgradeTask.doUpgrade(false);

        mockProjectRoleManager.verify();
        mockProjectManager.verify();
    }

    public void testGroupToRoleMapping()
    {
        ArrayList roles = getProjectRoles();

        Mock mockProjectRoleManager = new Mock(ProjectRoleManager.class);
        mockProjectRoleManager.expectAndReturn("getProjectRoles", roles);

        upgradeTask = new UpgradeTask_Build176(null, null, (ProjectRoleManager) mockProjectRoleManager.proxy());
        upgradeTask.initGroupToProjectRoleMappings();
        assertTrue(upgradeTask.groupToRoleMappings.containsKey(AbstractSetupAction.DEFAULT_GROUP_ADMINS));
        assertEquals(ADMIN_ID, upgradeTask.groupToRoleMappings.get(AbstractSetupAction.DEFAULT_GROUP_ADMINS));
        assertTrue(upgradeTask.groupToRoleMappings.containsKey(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS));
        assertEquals(DEVEL_ID, upgradeTask.groupToRoleMappings.get(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS));
        assertTrue(upgradeTask.groupToRoleMappings.containsKey(AbstractSetupAction.DEFAULT_GROUP_USERS));
        assertEquals(USER_ID, upgradeTask.groupToRoleMappings.get(AbstractSetupAction.DEFAULT_GROUP_USERS));
    }

    private ArrayList getProjectRoles()
    {
        ArrayList roles = new ArrayList();
        roles.add(new ProjectRoleImpl(ADMIN_ID, UpgradeTask_Build175.ROLE_ADMINISTRATORS, null));
        roles.add(new ProjectRoleImpl(DEVEL_ID, UpgradeTask_Build175.ROLE_DEVELOPERS, null));
        roles.add(new ProjectRoleImpl(USER_ID, UpgradeTask_Build175.ROLE_USERS, null));
        return roles;
    }

    public void testConvertDefaultPermissionSchemeToUserRoles() throws GenericEntityException
    {
        // We are going to create some db entries here and test that they are modified correctly
        GenericValue bingoEntity = EntityUtils.createValue("SchemePermissions", EasyMap.build("type", "bingo", "parameter", "stuff", "scheme", new Long(0)));
        GenericValue realEntity = EntityUtils.createValue("SchemePermissions", EasyMap.build("type", GroupDropdown.DESC, "parameter", AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, "scheme", new Long(0)));

        Mock mockPermissionSchemeManager = new Mock(PermissionSchemeManager.class);
        mockPermissionSchemeManager.expectAndReturn("getDefaultScheme", null);
        mockPermissionSchemeManager.expectAndReturn("getEntities", P.ANY_ARGS, EasyList.build(bingoEntity, realEntity));

        ArrayList roles = getProjectRoles();

        Mock mockProjectRoleManager = new Mock(ProjectRoleManager.class);
        mockProjectRoleManager.expectAndReturn("getProjectRoles", roles);

        upgradeTask = new UpgradeTask_Build176(null, (PermissionSchemeManager) mockPermissionSchemeManager.proxy(),
                (ProjectRoleManager) mockProjectRoleManager.proxy());
        upgradeTask.initGroupToProjectRoleMappings();
        upgradeTask.convertDefaultPermissionSchemeToUseRoles();

        GenericValue schemeEntity = CoreFactory.getGenericDelegator().findByPrimaryKey("SchemePermissions", EasyMap.build("id", bingoEntity.getLong("id")));
        String entityType = schemeEntity.getString("type");
        String entityParam = schemeEntity.getString("parameter");

        assertEquals("stuff", entityParam);
        assertEquals("bingo", entityType);

        schemeEntity = CoreFactory.getGenericDelegator().findByPrimaryKey("SchemePermissions", EasyMap.build("id", realEntity.getLong("id")));
        entityType = schemeEntity.getString("type");
        entityParam = schemeEntity.getString("parameter");

        assertEquals(DEVEL_ID, new Long(entityParam));
        assertEquals(UpgradeTask_Build176.PROJECT_ROLE_SECURITY_TYPE, entityType);
    }
}
