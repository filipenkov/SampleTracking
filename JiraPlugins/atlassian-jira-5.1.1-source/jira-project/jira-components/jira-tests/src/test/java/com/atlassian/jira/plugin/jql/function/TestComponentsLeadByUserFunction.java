package com.atlassian.jira.plugin.jql.function;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.project.component.MockProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryCreationContextImpl;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.plugin.jql.operand.MockJqlFunctionModuleDescriptor;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
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
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.2
 */
public class TestComponentsLeadByUserFunction extends MockControllerTestCase
{
    private User theUser;
    private QueryCreationContext queryCreationContext;
    private TerminalClause terminalClause = null;
    private List<ProjectComponent> componentsList1 = new ArrayList<ProjectComponent>();
    private List<ProjectComponent> componentsList2 = new ArrayList<ProjectComponent>();
    private Project project1;
    private Project project2;
    private Project project3;
    private ProjectComponent component1;
    private ProjectComponent component2;
    private ProjectComponent component3;
    private ProjectComponent component4;

    @Before
    public void setUp() throws Exception
    {
        theUser = new MockUser("fred");
        queryCreationContext = new QueryCreationContextImpl(theUser);

        component1 = new MockProjectComponent(21l, "c1", 21l);
        component2 = new MockProjectComponent(22l, "c2", 22l);
        component3 = new MockProjectComponent(23l, "c3", 23l);
        component4 = new MockProjectComponent(24l, "c4", 23l);

        project1 = new MockProject(21l, "p1");
        project2 = new MockProject(22l, "p2");
        project3 = new MockProject(23l, "p3");

        componentsList1.add(component1);
        componentsList1.add(component2);
        componentsList1.add(component3);
        componentsList1.add(component4);

        componentsList2.add(component1);
        componentsList2.add(component2);
    }

    @Test
    public void testDataType() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectComponentManager projectComponentManager = EasyMock.createMock(ProjectComponentManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ComponentsLeadByUserFunction componentsLeadByUserFunction = new ComponentsLeadByUserFunction(permissionManager, projectComponentManager, projectManager, userUtil);

        assertEquals(JiraDataTypes.COMPONENT, componentsLeadByUserFunction.getDataType());
    }

    @Test
    public void testValidateWrongArgs() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectComponentManager projectComponentManager = EasyMock.createMock(ProjectComponentManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ComponentsLeadByUserFunction componentsLeadByUserFunction = new ComponentsLeadByUserFunction(permissionManager, projectComponentManager, projectManager, userUtil);
        componentsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("componentsLeadByUser", true));

        MessageSet messageSet = componentsLeadByUserFunction.validate(new MockUser("bob"), new FunctionOperand("componentsLeadByUser", "badArg1", "badArg2"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'componentsLeadByUser' expected between '0' and '1' arguments but received '2'.", messageSet.getErrorMessages().iterator().next());

        EasyMock.expect(userUtil.getUserObject("badUser")).andReturn(null);
        replay(userUtil);
        messageSet = componentsLeadByUserFunction.validate(new MockUser("bob"), new FunctionOperand("componentsLeadByUser", "badUser"), terminalClause);
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'componentsLeadByUser' can not generate a list of components for user 'badUser'; the user does not exist.", messageSet.getErrorMessages().iterator().next());

    }

    @Test
    public void testValidateHappyPath() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectComponentManager projectComponentManager = EasyMock.createMock(ProjectComponentManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ComponentsLeadByUserFunction componentsLeadByUserFunction = new ComponentsLeadByUserFunction(permissionManager, projectComponentManager, projectManager, userUtil);
        componentsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("componentsLeadByUser", true));

        // No user name supplied
        MessageSet messageSet = componentsLeadByUserFunction.validate(new MockUser("bob"), new FunctionOperand("componentsLeadByUser"), terminalClause);
        assertFalse(messageSet.hasAnyErrors());

        // One valid user name supplied
        EasyMock.expect(userUtil.getUserObject("fred")).andReturn(theUser);
        replay(userUtil);
        messageSet = componentsLeadByUserFunction.validate(new MockUser("bob"), new FunctionOperand("componentsLeadByUser", "fred"), terminalClause);
        assertFalse(messageSet.hasAnyErrors());
    }

    @Test
    public void testValidateAnonymous()
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectComponentManager projectComponentManager = EasyMock.createMock(ProjectComponentManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ComponentsLeadByUserFunction componentsLeadByUserFunction = new ComponentsLeadByUserFunction(permissionManager, projectComponentManager, projectManager, userUtil);
        componentsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("componentsLeadByUser", true));

        final MessageSet messageSet = componentsLeadByUserFunction.validate(null, new FunctionOperand("componentsLeadByUser"), terminalClause);
        assertTrue(messageSet.hasAnyErrors());
        assertEquals(1, messageSet.getErrorMessages().size());
        assertEquals("Function 'componentsLeadByUser' cannot be called as anonymous user.", messageSet.getErrorMessages().iterator().next());
    }

    @Test
    public void testGetValuesHappyPath() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectComponentManager projectComponentManager = EasyMock.createMock(ProjectComponentManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ComponentsLeadByUserFunction componentsLeadByUserFunction = new ComponentsLeadByUserFunction(permissionManager, projectComponentManager, projectManager, userUtil);
        componentsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("componentsLeadByUser", true));

        EasyMock.expect(userUtil.getUserObject("fred")).andReturn(theUser);
        EasyMock.expect(projectComponentManager.findComponentsByLead("fred")).andReturn(componentsList1);
        EasyMock.expect(projectManager.getProjectObj(component1.getProjectId())).andReturn(project1);
        EasyMock.expect(projectManager.getProjectObj(component2.getProjectId())).andReturn(project2);
        EasyMock.expect(projectManager.getProjectObj(component3.getProjectId())).andReturn(project3);
        EasyMock.expect(projectManager.getProjectObj(component4.getProjectId())).andReturn(project3);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project1, theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project2, theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project3, theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project3, theUser)).andReturn(true);

        User bill = new MockUser("bill");
        EasyMock.expect(userUtil.getUserObject("bill")).andReturn(bill);
        EasyMock.expect(projectComponentManager.findComponentsByLead("bill")).andReturn(componentsList2);
        EasyMock.expect(projectManager.getProjectObj(component1.getProjectId())).andReturn(project1);
        EasyMock.expect(projectManager.getProjectObj(component2.getProjectId())).andReturn(project2);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project1, theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project2, theUser)).andReturn(true);
        replay(userUtil, projectComponentManager, projectManager, permissionManager);

        List<QueryLiteral> list = componentsLeadByUserFunction.getValues(queryCreationContext, new FunctionOperand("componentsLeadByUser"), terminalClause);
        assertEquals(4, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(22), list.get(1).getLongValue());
        assertEquals(new Long(23), list.get(2).getLongValue());
        assertEquals(new Long(24), list.get(3).getLongValue());

        list = componentsLeadByUserFunction.getValues(queryCreationContext, new FunctionOperand("componentsLeadByUser", "bill"), terminalClause);
        assertEquals(2, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(22), list.get(1).getLongValue());
        verify();

        // No permissions on projects 22 & 23
        reset(userUtil, projectComponentManager, projectManager, permissionManager);
        EasyMock.expect(userUtil.getUserObject("fred")).andReturn(theUser);
        EasyMock.expect(projectComponentManager.findComponentsByLead("fred")).andReturn(componentsList1);
        EasyMock.expect(projectManager.getProjectObj(component1.getProjectId())).andReturn(project1);
        EasyMock.expect(projectManager.getProjectObj(component2.getProjectId())).andReturn(project2);
        EasyMock.expect(projectManager.getProjectObj(component3.getProjectId())).andReturn(project3);
        EasyMock.expect(projectManager.getProjectObj(component4.getProjectId())).andReturn(project3);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project1, theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project2, theUser)).andReturn(false);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project3, theUser)).andReturn(false);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project3, theUser)).andReturn(false);

        bill = new MockUser("bill");
        EasyMock.expect(userUtil.getUserObject("bill")).andReturn(bill);
        EasyMock.expect(projectComponentManager.findComponentsByLead("bill")).andReturn(componentsList2);
        EasyMock.expect(projectManager.getProjectObj(component1.getProjectId())).andReturn(project1);
        EasyMock.expect(projectManager.getProjectObj(component2.getProjectId())).andReturn(project2);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project1, theUser)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project2, theUser)).andReturn(false);
        replay(userUtil, projectComponentManager, projectManager, permissionManager);

        list = componentsLeadByUserFunction.getValues(queryCreationContext, new FunctionOperand("componentsLeadByUser"), terminalClause);
        assertEquals(1, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());

        list = componentsLeadByUserFunction.getValues(queryCreationContext, new FunctionOperand("componentsLeadByUser", "bill"), terminalClause);
        assertEquals(1, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        verify();
    }

    @Test
    public void testGetValuesAnonymous()
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectComponentManager projectComponentManager = EasyMock.createMock(ProjectComponentManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ComponentsLeadByUserFunction componentsLeadByUserFunction = new ComponentsLeadByUserFunction(permissionManager, projectComponentManager, projectManager, userUtil);
        componentsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("componentsLeadByUser", true));

        User bill = new MockUser("bill");
        EasyMock.expect(userUtil.getUserObject("bill")).andReturn(bill);
        EasyMock.expect(projectComponentManager.findComponentsByLead("bill")).andReturn(componentsList2);
        EasyMock.expect(projectManager.getProjectObj(component1.getProjectId())).andReturn(project1);
        EasyMock.expect(projectManager.getProjectObj(component2.getProjectId())).andReturn(project2);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null)).andReturn(true);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project2, (User) null)).andReturn(true);
        replay(userUtil, projectComponentManager, projectManager, permissionManager);

        List<QueryLiteral> list = componentsLeadByUserFunction.getValues(new QueryCreationContextImpl(null), new FunctionOperand("componentsLeadByUser"), terminalClause);
        assertTrue(list.isEmpty());

        list = componentsLeadByUserFunction.getValues(new QueryCreationContextImpl(null), new FunctionOperand("componentsLeadByUser", "bill"), terminalClause);
        assertEquals(2, list.size());
        assertEquals(new Long(21), list.get(0).getLongValue());
        assertEquals(new Long(22), list.get(1).getLongValue());
        verify();

        // No permissions for anonymous user.
        reset(userUtil, projectComponentManager, projectManager, permissionManager);
        EasyMock.expect(userUtil.getUserObject("bill")).andReturn(bill);
        EasyMock.expect(projectComponentManager.findComponentsByLead("bill")).andReturn(componentsList2);
        EasyMock.expect(projectManager.getProjectObj(component1.getProjectId())).andReturn(project1);
        EasyMock.expect(projectManager.getProjectObj(component2.getProjectId())).andReturn(project2);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project1, (User) null)).andReturn(false);
        EasyMock.expect(permissionManager.hasPermission(Permissions.BROWSE, project2, (User) null)).andReturn(false);
        replay(userUtil, projectComponentManager, projectManager, permissionManager);

        list = componentsLeadByUserFunction.getValues(new QueryCreationContextImpl(null), new FunctionOperand("componentsLeadByUser"), terminalClause);
        assertTrue(list.isEmpty());

        list = componentsLeadByUserFunction.getValues(new QueryCreationContextImpl(null), new FunctionOperand("componentsLeadByUser", "bill"), terminalClause);
        assertTrue(list.isEmpty());
        verify();
    }

    @Test
    public void testGetMinimumNumberOfExpectedArguments() throws Exception
    {
        final UserUtil userUtil = EasyMock.createMock(UserUtil.class);
        final ProjectComponentManager projectComponentManager = EasyMock.createMock(ProjectComponentManager.class);
        final ProjectManager projectManager = EasyMock.createMock(ProjectManager.class);
        final PermissionManager permissionManager = EasyMock.createMock(PermissionManager.class);

        ComponentsLeadByUserFunction componentsLeadByUserFunction = new ComponentsLeadByUserFunction(permissionManager, projectComponentManager, projectManager, userUtil);
        componentsLeadByUserFunction.init(MockJqlFunctionModuleDescriptor.create("componentsLeadByUser", true));

        assertEquals(0, componentsLeadByUserFunction.getMinimumNumberOfExpectedArguments());
    }

}
