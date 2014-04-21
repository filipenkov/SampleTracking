package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.plugin.jql.operand.MockJqlFunctionModuleDescriptor;
import com.atlassian.jira.permission.Permission;
import com.atlassian.jira.permission.PermissionImpl;
import com.atlassian.jira.permission.SchemePermissions;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.FunctionOperand;
import org.easymock.classextension.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.0
 */
public class TestProjectsWhereUserHasPermissionFunction extends MockControllerTestCase
{
    private User theUser;
    private QueryCreationContext queryCreationContext;
    private TerminalClause terminalClause = null;
    private List<Project> projectsList1 = new ArrayList<Project>();
    private List<Project> projectsList2 = new ArrayList<Project>();
    final SchemePermissions schemePermissions = new MockSchemePermissions();

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");
        queryCreationContext = new QueryCreationContextImpl(theUser);

        Project project1 = new MockProject(21l, "c1");
        Project project2 = new MockProject(22l, "c2");
        Project project3 = new MockProject(23l, "c3");
        Project project4 = new MockProject(24l, "c4");

        projectsList1.add(project1);
        projectsList1.add(project2);
        projectsList1.add(project3);
        projectsList1.add(project4);

        projectsList2.add(project1);
        projectsList2.add(project2);
    }

    @Test
    public void testDataType() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);


        ProjectsWhereUserHasPermissionFunction projectsWhereUserHasPermissionFunction = new ProjectsWhereUserHasPermissionFunction(permissionManager, schemePermissions, userUtil);

        assertEquals(JiraDataTypes.PROJECT, projectsWhereUserHasPermissionFunction.getDataType());
    }

    @Test
    public void testValidateWrongArgs() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsWhereUserHasPermissionFunction projectsWhereUserHasPermissionFunction = new ProjectsWhereUserHasPermissionFunction(permissionManager, schemePermissions, userUtil);
        projectsWhereUserHasPermissionFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasPermission", true));

        MessageSet messageSet = projectsWhereUserHasPermissionFunction.validate(new MockUser("bob"), new FunctionOperand("projectsWhereUserHasPermission"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsWhereUserHasPermission' expected '1' arguments but received '0'.", messageSet.getErrorMessages().iterator().next());

        messageSet = projectsWhereUserHasPermissionFunction.validate(new MockUser("bob"), new FunctionOperand("projectsWhereUserHasPermission", "badArg1", "badArg2", "badArg3"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsWhereUserHasPermission' expected '1' arguments but received '3'.", messageSet.getErrorMessages().iterator().next());

        replay(userUtil);

        messageSet = projectsWhereUserHasPermissionFunction.validate(new MockUser("bob"), new FunctionOperand("projectsWhereUserHasPermission", "BadPermission"), terminalClause);
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsWhereUserHasPermission' can not generate a list of projects for permission 'BadPermission'; the permission does not exist.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testValidateHappyPath() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsWhereUserHasPermissionFunction projectsWhereUserHasPermissionFunction = new ProjectsWhereUserHasPermissionFunction(permissionManager, schemePermissions, userUtil);
        projectsWhereUserHasPermissionFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasPermission", true));

        // No user name supplied
        MessageSet messageSet = projectsWhereUserHasPermissionFunction.validate(new MockUser("bob"), new FunctionOperand("projectsWhereUserHasPermission", "Permission1"), terminalClause);
        assertFalse(messageSet.hasAnyErrors());
    }

    @Test
    public void testValidateAnonymous()
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsWhereUserHasPermissionFunction projectsWhereUserHasPermissionFunction = new ProjectsWhereUserHasPermissionFunction(permissionManager, schemePermissions, userUtil);
        projectsWhereUserHasPermissionFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasPermission", true));

        final MessageSet messageSet = projectsWhereUserHasPermissionFunction.validate(null, new FunctionOperand("projectsWhereUserHasPermission", "Permission1"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'projectsWhereUserHasPermission' cannot be called as anonymous user.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testGetValuesHappyPath() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsWhereUserHasPermissionFunction projectsWhereUserHasPermissionFunction = new ProjectsWhereUserHasPermissionFunction(permissionManager, schemePermissions, userUtil);
        projectsWhereUserHasPermissionFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasPermission", true));

        EasyMock.expect(userUtil.getUserObject("fred")).andReturn(theUser);
        EasyMock.expect(permissionManager.getProjectObjects(1, theUser)).andReturn(projectsList1);
        for (Project project : projectsList1)
        {
            EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, theUser)).andReturn(true);
        }
        replay(userUtil, permissionManager);

        List<QueryLiteral> list = projectsWhereUserHasPermissionFunction.getValues(queryCreationContext, new FunctionOperand("projectsWhereUserHasPermission", "Permission1"), terminalClause);
        assertEquals(4, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(22), list.get(1).getLongValue());
        assertEquals(new Long(23), list.get(2).getLongValue());
        assertEquals(new Long(24), list.get(3).getLongValue());
        verify();

        // No permissions on projects 22 & 23
        reset(userUtil, permissionManager);
        EasyMock.expect(userUtil.getUserObject("fred")).andReturn(theUser);
        EasyMock.expect(permissionManager.getProjectObjects(1, theUser)).andReturn(projectsList1);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(0), theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(1), theUser)).andReturn(false);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(2), theUser)).andReturn(false);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, projectsList1.get(3), theUser)).andReturn(true);
        replay(userUtil, permissionManager);

        list = projectsWhereUserHasPermissionFunction.getValues(queryCreationContext, new FunctionOperand("projectsWhereUserHasPermission", "Permission1"), terminalClause);
        assertEquals(2, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(24), list.get(1).getLongValue());
        verify();
    }

    @Test
    public void testGetValuesAnonymous()
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsWhereUserHasPermissionFunction projectsWhereUserHasPermissionFunction = new ProjectsWhereUserHasPermissionFunction(permissionManager, schemePermissions, userUtil);
        projectsWhereUserHasPermissionFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasPermission", true));

        for (Project project : projectsList2)
        {
            EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, null)).andReturn(true);
        }
        replay(userUtil, permissionManager);

        List<QueryLiteral> list = projectsWhereUserHasPermissionFunction.getValues(new QueryCreationContextImpl(null), new FunctionOperand("projectsWhereUserHasPermission", "Permission1"), terminalClause);
        assertTrue(list.isEmpty());

        verify();

        // No permission for anonymous user
        reset(userUtil, permissionManager);
        for (Project project : projectsList2)
        {
            EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project, null)).andReturn(false);
        }
        replay(userUtil, permissionManager);

        list = projectsWhereUserHasPermissionFunction.getValues(new QueryCreationContextImpl(null), new FunctionOperand("projectsWhereUserHasPermission", "Permission1"), terminalClause);
        assertTrue(list.isEmpty());
        verify();
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ProjectsWhereUserHasPermissionFunction projectsWhereUserHasPermissionFunction = new ProjectsWhereUserHasPermissionFunction(permissionManager, schemePermissions, userUtil);
        projectsWhereUserHasPermissionFunction.init(MockJqlFunctionModuleDescriptor.create("projectsWhereUserHasPermission", true));

        assertEquals(1, projectsWhereUserHasPermissionFunction.getMinimumNumberOfExpectedArguments());
    }

    private class MockSchemePermissions extends SchemePermissions
    {
        @Override
        public Map<Integer, Permission> getSchemePermissions()
        {
            Map<Integer, Permission> permissions = new LinkedHashMap<Integer, Permission>();
            permissions.put(Integer.valueOf(1), new PermissionImpl("1", "Permission1", "Permission 1 description.", "I18.key.1", "I18.key.1.desc"));
            permissions.put(Integer.valueOf(2), new PermissionImpl("2", "Permission2", "Permission 2 description.", "I18.key.2", "I18.key.2.desc"));
            permissions.put(Integer.valueOf(3), new PermissionImpl("3", "Permission3", "Permission 3 description.", "I18.key.3", "I18.key.3.desc"));
            permissions.put(Integer.valueOf(4), new PermissionImpl("4", "Permission4", "Permission 4 description.", "I18.key.4", "I18.key.4.desc"));
            permissions.put(Integer.valueOf(5), new PermissionImpl("5", "Permission5", "Permission 5 description.", "I18.key.5", "I18.key.5.desc"));
            return permissions;
        }
    }
}
