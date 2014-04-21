package com.atlassian.jira.imports.project.validation;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.external.beans.ExternalProjectRoleActor;
import com.atlassian.jira.external.beans.ExternalUser;
import com.atlassian.jira.imports.project.core.ProjectImportOptionsImpl;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapperImpl;
import com.atlassian.jira.imports.project.mapper.ProjectRoleActorMapper;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.MockApplicationProperties;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetAssert;
import com.atlassian.jira.web.bean.MockI18nBean;
import org.easymock.MockControl;
import org.junit.Test;

/**
 * @since v3.13
 */
public class TestProjectRoleActorMapperValidatorImpl extends ListeningTestCase
{
    @Test
    public void testValidateUnknownRoleType() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        projectImportMapper.getProjectRoleActorMapper().flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "Dog", "Rover"));

        projectImportMapper.getProjectRoleMapper().registerOldValue("12", "Dudes");

        ProjectRoleActorMapperValidatorImpl validator = new ProjectRoleActorMapperValidatorImpl(null);
        MessageSet messageSet = validator.validateProjectRoleActors(new MockI18nBean(), projectImportMapper, new ProjectImportOptionsImpl("", "", true));
        MessageSetAssert.assert1Warning(messageSet, "Project role 'Dudes' contains an actor 'Rover' of unknown role type 'Dog'. This actor will not be added to the project role.");
    }

    @Test
    public void testValidateWithDontUpdateDetailsOption() throws Exception
    {
        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, null);

        projectImportMapper.getProjectRoleActorMapper().flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "Dog", "Rover"));

        projectImportMapper.getProjectRoleMapper().registerOldValue("12", "Dudes");

        ProjectRoleActorMapperValidatorImpl validator = new ProjectRoleActorMapperValidatorImpl(null);
        // If the USer chooses "Don't update project details, then we don't change the role memberships,and therefore we have no validation.
        MessageSet messageSet = validator.validateProjectRoleActors(new MockI18nBean(), projectImportMapper, new ProjectImportOptionsImpl("", "", false));
        MessageSetAssert.assertNoMessages(messageSet);
    }

    @Test
    public void testValidateMissingGroup() throws Exception
    {
        final MockControl mockGroupManagerControl = MockControl.createStrictControl(GroupManager.class);
        final GroupManager mockGroupManager = (GroupManager) mockGroupManagerControl.getMock();
        mockGroupManager.groupExists("goodies");
        mockGroupManagerControl.setReturnValue(true);
        mockGroupManager.groupExists("baddies");
        mockGroupManagerControl.setReturnValue(false);
        mockGroupManagerControl.replay();

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(null, mockGroupManager);

        final ProjectRoleActorMapper projectRoleActorMapper = projectImportMapper.getProjectRoleActorMapper();
        projectRoleActorMapper.flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "atlassian-group-role-actor", "goodies"));
        projectRoleActorMapper.flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "atlassian-group-role-actor", "baddies"));

        projectImportMapper.getProjectRoleMapper().registerOldValue("12", "Dudes");

        //        final UserMapper userMapper = new UserMapper(mockUserUtil);

        MockApplicationProperties mockApplicationProperties = new MockApplicationProperties();

        ProjectRoleActorMapperValidatorImpl validator = new ProjectRoleActorMapperValidatorImpl(null);
        MessageSet messageSet = validator.validateProjectRoleActors(new MockI18nBean(), projectImportMapper, new ProjectImportOptionsImpl("", "", true));
        MessageSetAssert.assert1Warning(messageSet, "Project role 'Dudes' contains a group 'baddies' that doesn't exist in the current system. This group will not be added to the project role membership.");

        mockGroupManagerControl.verify();
    }

    @Test
    public void testValidateMissingUsersExternalUserManagement() throws Exception
    {
        final MockControl mockUserUtilControl = MockControl.createStrictControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();
        mockUserUtil.userExists("peter");
        mockUserUtilControl.setReturnValue(true);
        mockUserUtil.userExists("paul");
        mockUserUtilControl.setReturnValue(false);
        mockUserUtilControl.replay();

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(mockUserUtil, null);

        final ProjectRoleActorMapper projectRoleActorMapper = projectImportMapper.getProjectRoleActorMapper();
        projectRoleActorMapper.flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "atlassian-user-role-actor", "peter"));
        projectRoleActorMapper.flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "atlassian-user-role-actor", "paul"));

        projectImportMapper.getProjectRoleMapper().registerOldValue("12", "Dudes");

        final MockUserManager userManager = new MockUserManager();
        ProjectRoleActorMapperValidatorImpl validator = new ProjectRoleActorMapperValidatorImpl(userManager);
        MessageSet messageSet = validator.validateProjectRoleActors(new MockI18nBean(), projectImportMapper, new ProjectImportOptionsImpl("", "", true));
        MessageSetAssert.assert1Warning(messageSet, "Project role 'Dudes' contains a user 'paul' that doesn't exist in the current system. This user will not be added to the project role membership.");

        mockUserUtilControl.verify();
    }

    @Test
    public void testValidateMissingUsersJiraUserManagement() throws Exception
    {
        final MockControl mockUserUtilControl = MockControl.createControl(UserUtil.class);
        final UserUtil mockUserUtil = (UserUtil) mockUserUtilControl.getMock();
        mockUserUtil.userExists("peter");
        mockUserUtilControl.setReturnValue(true);
        mockUserUtil.userExists("paul");
        mockUserUtilControl.setReturnValue(false);
        mockUserUtil.userExists("mary");
        mockUserUtilControl.setReturnValue(false);
        mockUserUtilControl.replay();

        ProjectImportMapper projectImportMapper = new ProjectImportMapperImpl(mockUserUtil, null);

        final ProjectRoleActorMapper projectRoleActorMapper = projectImportMapper.getProjectRoleActorMapper();
        projectRoleActorMapper.flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "atlassian-user-role-actor", "peter"));
        projectRoleActorMapper.flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "atlassian-user-role-actor", "paul"));
        projectRoleActorMapper.flagValueActorAsInUse(new ExternalProjectRoleActor("1", "2", "12", "atlassian-user-role-actor", "mary"));

        projectImportMapper.getProjectRoleMapper().registerOldValue("12", "Dudes");

        // Make it that paul can be auto-created.
        final ExternalUser externalUser = new ExternalUser();
        externalUser.setName("paul");
        projectImportMapper.getUserMapper().registerOldValue(externalUser);

        ProjectRoleActorMapperValidatorImpl validator = new ProjectRoleActorMapperValidatorImpl(new MockUserManager());
        MessageSet messageSet = validator.validateProjectRoleActors(new MockI18nBean(), projectImportMapper, new ProjectImportOptionsImpl("", "", true));
        MessageSetAssert.assert1Warning(messageSet, "Project role 'Dudes' contains a user 'mary' that doesn't exist in the current system. This user will not be added to the project role membership.");

        mockUserUtilControl.verify();
    }
}
