package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.roles.ProjectRoleImpl;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.roles.actor.GroupRoleActorFactory;
import com.atlassian.jira.web.action.setup.AbstractSetupAction;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;

/**
 * Tests to make sure that the default roles get created correctly.
 */
public class TestUpgradeTask_Build175 extends MockControllerTestCase
{

    private UpgradeTask_Build175 upgradeTask;
    private static final ProjectRoleImpl PROJECT_ROLE_USERS = new ProjectRoleImpl("Users", "A project role that represents users in a project");
    private static final ProjectRoleImpl PROJECT_ROLE_DEVELOPERS = new ProjectRoleImpl("Developers", "A project role that represents developers in a project");
    private static final ProjectRoleImpl PROJECT_ROLE_ADMINS = new ProjectRoleImpl("Administrators", "A project role that represents administrators in a project");

    @Before
    public void setUp() throws Exception
    {
    }

    @Test
    public void testBuildNumber()
    {
        upgradeTask = new UpgradeTask_Build175(null, null);
        assertEquals("175", upgradeTask.getBuildNumber());
    }

    @Test
    public void testShortDescription()
    {
        upgradeTask = new UpgradeTask_Build175(null, null);
        assertEquals("Adds the default project roles and populates their members.", upgradeTask.getShortDescription());
    }

    // Test a standard upgrade
    @Test
    public void testStandardUpgrade() throws Exception
    {
        final ProjectRoleManager projectRoleManager = createMock(ProjectRoleManager.class);
        expect(projectRoleManager.createRole(PROJECT_ROLE_USERS)).andReturn(PROJECT_ROLE_USERS);
        expect(projectRoleManager.createRole(PROJECT_ROLE_DEVELOPERS)).andReturn(PROJECT_ROLE_DEVELOPERS);
        expect(projectRoleManager.createRole(PROJECT_ROLE_ADMINS)).andReturn(PROJECT_ROLE_ADMINS);

        final OfBizDelegator ofBizDelegator = createMock(OfBizDelegator.class);
        expect(ofBizDelegator.findByAnd("OSGroup", EasyMap.build("name", AbstractSetupAction.DEFAULT_GROUP_USERS))).andReturn(Collections.<GenericValue>singletonList(new MockGenericValue("OSGroup")));
        expect(ofBizDelegator.createValue(UpgradeTask_Build175.ROLE_ACTOR_ENTITY_NAME, EasyMap.build(UpgradeTask_Build175.ROLE_ACTOR_PID, null, UpgradeTask_Build175.ROLE_ACTOR_PROJECTROLEID,
                PROJECT_ROLE_USERS.getId(), UpgradeTask_Build175.ROLE_ACTOR_ROLETYPE, GroupRoleActorFactory.TYPE, "roletypeparameter", AbstractSetupAction.DEFAULT_GROUP_USERS))).andReturn(null);
        expect(ofBizDelegator.findByAnd("OSGroup", EasyMap.build("name", AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS))).andReturn(Collections.<GenericValue>singletonList(new MockGenericValue("OSGroup")));
        expect(ofBizDelegator.createValue(UpgradeTask_Build175.ROLE_ACTOR_ENTITY_NAME, EasyMap.build(UpgradeTask_Build175.ROLE_ACTOR_PID, null, UpgradeTask_Build175.ROLE_ACTOR_PROJECTROLEID,
                PROJECT_ROLE_DEVELOPERS.getId(), UpgradeTask_Build175.ROLE_ACTOR_ROLETYPE, GroupRoleActorFactory.TYPE, "roletypeparameter", AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS))).andReturn(null);
        expect(ofBizDelegator.findByAnd("OSGroup", EasyMap.build("name", AbstractSetupAction.DEFAULT_GROUP_ADMINS))).andReturn(Collections.<GenericValue>singletonList(new MockGenericValue("OSGroup")));
        expect(ofBizDelegator.createValue(UpgradeTask_Build175.ROLE_ACTOR_ENTITY_NAME, EasyMap.build(UpgradeTask_Build175.ROLE_ACTOR_PID, null, UpgradeTask_Build175.ROLE_ACTOR_PROJECTROLEID,
                PROJECT_ROLE_ADMINS.getId(), UpgradeTask_Build175.ROLE_ACTOR_ROLETYPE, GroupRoleActorFactory.TYPE, "roletypeparameter", AbstractSetupAction.DEFAULT_GROUP_ADMINS))).andReturn(null);

        replay();
        upgradeTask = new UpgradeTask_Build175(projectRoleManager, ofBizDelegator);

        upgradeTask.doUpgrade(false);
    }

    //JRA-11721: Test the upgrade task when a default group (i.e. jira-developers) has been deleted.
    @Test
    public void testUpgradeTaskWithGroupmissing() throws Exception
    {
        final ProjectRoleManager projectRoleManager = createMock(ProjectRoleManager.class);
        expect(projectRoleManager.createRole(PROJECT_ROLE_USERS)).andReturn(PROJECT_ROLE_USERS);
        expect(projectRoleManager.createRole(PROJECT_ROLE_DEVELOPERS)).andReturn(PROJECT_ROLE_DEVELOPERS);
        expect(projectRoleManager.createRole(PROJECT_ROLE_ADMINS)).andReturn(PROJECT_ROLE_ADMINS);

        final OfBizDelegator ofBizDelegator = createMock(OfBizDelegator.class);
        expect(ofBizDelegator.findByAnd("OSGroup", EasyMap.build("name", AbstractSetupAction.DEFAULT_GROUP_USERS))).andReturn(Collections.<GenericValue>singletonList(new MockGenericValue("OSGroup")));
        expect(ofBizDelegator.createValue(UpgradeTask_Build175.ROLE_ACTOR_ENTITY_NAME, EasyMap.build(UpgradeTask_Build175.ROLE_ACTOR_PID, null, UpgradeTask_Build175.ROLE_ACTOR_PROJECTROLEID,
                PROJECT_ROLE_USERS.getId(), UpgradeTask_Build175.ROLE_ACTOR_ROLETYPE, GroupRoleActorFactory.TYPE, "roletypeparameter", AbstractSetupAction.DEFAULT_GROUP_USERS))).andReturn(null);
        expect(ofBizDelegator.findByAnd("OSGroup", EasyMap.build("name", AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS))).andReturn(Collections.<GenericValue>emptyList());
        expect(ofBizDelegator.findByAnd("OSGroup", EasyMap.build("name", AbstractSetupAction.DEFAULT_GROUP_ADMINS))).andReturn(Collections.<GenericValue>singletonList(new MockGenericValue("OSGroup")));
        expect(ofBizDelegator.createValue(UpgradeTask_Build175.ROLE_ACTOR_ENTITY_NAME, EasyMap.build(UpgradeTask_Build175.ROLE_ACTOR_PID, null, UpgradeTask_Build175.ROLE_ACTOR_PROJECTROLEID,
                PROJECT_ROLE_ADMINS.getId(), UpgradeTask_Build175.ROLE_ACTOR_ROLETYPE, GroupRoleActorFactory.TYPE, "roletypeparameter", AbstractSetupAction.DEFAULT_GROUP_ADMINS))).andReturn(null);

        replay();
        upgradeTask = new UpgradeTask_Build175(projectRoleManager, ofBizDelegator);

        upgradeTask.doUpgrade(false);
    }
}
