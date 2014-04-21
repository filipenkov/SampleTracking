package com.atlassian.jira.workflow;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.OperationContextImpl;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderer;
import com.atlassian.jira.issue.operation.WorkflowIssueOperationImpl;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.util.ErrorCollection;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.IsNull;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: amazkovoi
 * Date: 10/09/2004
 * Time: 10:55:46
 * To change this template use File | Settings | File Templates.
 */
public class TestWorkflowTransitionUtilImpl extends AbstractUsersIndexingTestCase
{
    private GenericValue issue;
    private Version version1;
    private Version version2;
    private GenericValue resolution;
    private User u;
    private Group g;
    private String GROUP_NAME = "group";
    private PermissionSchemeManager permissionSchemeManager;
    private static final String ORIGINAL_ASSIGNEE = "anton";
    private GenericValue project;
    private Mock versionManagerMock;
    private Mock mockAuthenticationContext;
    private Mock mockProjectManager;
    private Mock mockWorkflowManager;

    public TestWorkflowTransitionUtilImpl(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        if (permissionSchemeManager == null)
        {
            permissionSchemeManager = ManagerFactory.getPermissionSchemeManager();
        }

        //Try using another xml file for the BasicWorkflow so that the entire get stored in the memory database
        u = createMockUser("owen");
        g = createMockGroup(GROUP_NAME);
        addUserToGroup(u, g);

        User origUser = createMockUser(ORIGINAL_ASSIGNEE);
        addUserToGroup(origUser, g);

        // Create a project
        project = UtilsForTests.getTestEntity("Project", EasyMap.build("name", "Project A", "counter", new Long(0), "key", "ABC"));

        // Create permissions
        permissionSchemeManager.createDefaultScheme();
        permissionSchemeManager.addDefaultSchemeToProject(project);
        ManagerFactory.getPermissionManager().addPermission(Permissions.BROWSE, permissionSchemeManager.getDefaultScheme(), GROUP_NAME, GroupDropdown.DESC);
        ManagerFactory.getPermissionManager().addPermission(Permissions.CREATE_ISSUE, permissionSchemeManager.getDefaultScheme(), GROUP_NAME, GroupDropdown.DESC);
        ManagerFactory.getPermissionManager().addPermission(Permissions.RESOLVE_ISSUE, permissionSchemeManager.getDefaultScheme(), GROUP_NAME, GroupDropdown.DESC);
        ManagerFactory.getPermissionManager().addPermission(Permissions.CLOSE_ISSUE, permissionSchemeManager.getDefaultScheme(), GROUP_NAME, GroupDropdown.DESC);

        // Create an issue to test with
        GenericValue issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "An Issue Type"));
//        issue = ManagerFactory.getIssueManager().createIssue(u, EasyMap.build("projectId", project.getLong("id"), IssueFieldConstants.SUMMARY, "Issue Summary", IssueFieldConstants.ISSUE_TYPE, issueType.getString("id"), IssueFieldConstants.ISSUE_KEY, "ABC-1", IssueFieldConstants.ASSIGNEE, ORIGINAL_ASSIGNEE));

        versionManagerMock = new Mock(VersionManager.class);
        versionManagerMock.setStrict(true);

        mockAuthenticationContext = new Mock(JiraAuthenticationContext.class);
        mockAuthenticationContext.setStrict(true);

        mockProjectManager = new Mock(ProjectManager.class);
        mockProjectManager.setStrict(true);

        mockWorkflowManager = new Mock(WorkflowManager.class);
        mockWorkflowManager.setStrict(true);
    }

    public void testGetAdditionalInputs() throws Exception
    {
        mockAuthenticationContext.expectAndReturn("getLoggedInUser", u);

        WorkflowTransitionUtilImpl wu = makeAction();

        Map params = new HashMap();

        wu.setAction(1);

        String commentString = "Comment";
        params.put(WorkflowTransitionUtil.FIELD_COMMENT, commentString);
        wu.setParams(params);

        Map inputs = wu.getAdditionalInputs();
        assertEquals(commentString, inputs.get(WorkflowTransitionUtil.FIELD_COMMENT));
        assertNull(inputs.get(WorkflowTransitionUtil.FIELD_COMMENT_LEVEL));
        assertEquals(u.getName(), inputs.get("username"));

        verifyMocks();
    }

    private void verifyMocks()
    {
        mockAuthenticationContext.verify();
        versionManagerMock.verify();
        mockProjectManager.verify();
    }

    private WorkflowTransitionUtilImpl makeAction()
    {
        WorkflowTransitionUtilImpl wu = new WorkflowTransitionUtilImpl((JiraAuthenticationContext) mockAuthenticationContext.proxy(), (WorkflowManager) mockWorkflowManager.proxy(), ManagerFactory.getPermissionManager(), ComponentAccessor.getFieldScreenRendererFactory(), null);

        return wu;
    }

    public void testValidateNoCommentPermission() throws Exception
    {
        mockAuthenticationContext.expectAndReturn("getLoggedInUser", u);
        mockAuthenticationContext.expectAndReturn("getI18nHelper", P.ANY_ARGS, ComponentAccessor.getJiraAuthenticationContext().getI18nHelper());
        
        WorkflowTransitionUtilImpl wu = makeAction();

        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
        mockIssue.expectAndReturn("getProject", project);

        Map params = new HashMap();
        params.put("comment", "some comment");
        wu.setParams(params);
        wu.setIssue((MutableIssue) mockIssue.proxy());

        ErrorCollection errorCollection = wu.validate();
        assertTrue(errorCollection.hasAnyErrors());
        checkSingleElementCollection(errorCollection.getErrorMessages(), "The user '" + u.getName() + "' does not have permission to comment on issues in this project.");


        mockIssue.verify();
        verifyMocks();
    }

    /**
     * JRA-16112 and JRA-16915 
     *
     * @throws Exception if stuff goes wrong
     */
    public void testValidateResolutionField() throws Exception
    {
        mockAuthenticationContext.expectAndReturn("getLoggedInUser", u);
        mockAuthenticationContext.expectAndReturn("getI18nHelper", null);

        Map params = new HashMap();

        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
        MutableIssue issue = (MutableIssue) mockIssue.proxy();

        Mock mockOrderableField = new Mock(OrderableField.class);
        mockOrderableField.setStrict(true);


        Mock mockFieldScreenRenderLayoutItem1 = new Mock(FieldScreenRenderLayoutItem.class);
        mockFieldScreenRenderLayoutItem1.setStrict(true);
        mockFieldScreenRenderLayoutItem1.expectAndReturn("isShow", P.args(new IsEqual(issue)), Boolean.TRUE);
        mockFieldScreenRenderLayoutItem1.expectAndReturn("getOrderableField", mockOrderableField.proxy());

        FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem1 = (FieldScreenRenderLayoutItem) mockFieldScreenRenderLayoutItem1.proxy();

        int actionId = 5;
        ActionDescriptor ad = DescriptorFactory.getFactory().createActionDescriptor();
        ad.setId(actionId);

        OperationContext expectedOperationContext = new OperationContextImpl(new WorkflowIssueOperationImpl(ad), params);

        mockOrderableField.expectAndReturn("getId", P.ANY_ARGS, IssueFieldConstants.RESOLUTION);
        mockFieldScreenRenderLayoutItem1.expectAndReturn("getFieldScreenLayoutItem", null);
        mockFieldScreenRenderLayoutItem1.expectAndReturn("getFieldLayoutItem", null);
        mockOrderableField.expectVoid("validateParams", new Constraint[]{new IsEqual(expectedOperationContext), new IsAnything(), new IsNull(), new IsEqual(issue), new IsAnything()});

        WorkflowManager workflowManager = (WorkflowManager) mockWorkflowManager.proxy();

        WorkflowDescriptor wd = new WorkflowDescriptor();
        wd.addGlobalAction(ad);

        Mock mockWorkflow = new Mock(JiraWorkflow.class);
        mockWorkflow.setStrict(true);
        mockWorkflow.expectAndReturn("getDescriptor", wd);
        JiraWorkflow workflow = (JiraWorkflow) mockWorkflow.proxy();

        mockWorkflowManager.expectAndReturn("getWorkflow", P.args(new IsEqual(issue)), workflow);

        Mock mockFieldScreenRenderLayoutItem2 = new Mock(FieldScreenRenderLayoutItem.class);
        mockFieldScreenRenderLayoutItem2.setStrict(true);
        mockFieldScreenRenderLayoutItem2.expectAndReturn("isShow", P.args(new IsEqual(issue)), Boolean.FALSE);
        FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem2 = (FieldScreenRenderLayoutItem) mockFieldScreenRenderLayoutItem2.proxy();

        Mock mockFieldScreenRenderTab = new Mock(FieldScreenRenderTab.class);
        mockFieldScreenRenderTab.setStrict(true);
        mockFieldScreenRenderTab.expectAndReturn("getFieldScreenRenderLayoutItemsForProcessing", EasyList.build(fieldScreenRenderLayoutItem1, fieldScreenRenderLayoutItem2));

        final Mock mockFieldScreenRenderer = new Mock(FieldScreenRenderer.class);
        mockFieldScreenRenderer.setStrict(true);
        mockFieldScreenRenderer.expectAndReturn("getFieldScreenRenderTabs", EasyList.build(mockFieldScreenRenderTab.proxy()));



        WorkflowTransitionUtilImpl wu = new WorkflowTransitionUtilImpl((JiraAuthenticationContext) mockAuthenticationContext.proxy(), workflowManager, ComponentAccessor.getPermissionManager(), ComponentAccessor.getFieldScreenRendererFactory(), null)
        {
            public FieldScreenRenderer getFieldScreenRenderer()
            {
                return (FieldScreenRenderer) mockFieldScreenRenderer.proxy();
            }
        };


        wu.setAction(actionId);
        wu.setParams(params);
        wu.setIssue(issue);

        ErrorCollection errorCollection = wu.validate();
        assertFalse(errorCollection.hasAnyErrors());

        mockFieldScreenRenderer.verify();
        mockOrderableField.verify();
        mockFieldScreenRenderLayoutItem1.verify();
        mockFieldScreenRenderLayoutItem2.verify();
        mockIssue.verify();
        mockWorkflow.verify();
        verifyMocks();
    }

    public void testValidate() throws Exception
    {
        mockAuthenticationContext.expectAndReturn("getLoggedInUser", u);
        mockAuthenticationContext.expectAndReturn("getI18nHelper", null);

        Map params = new HashMap();

        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
        MutableIssue issue = (MutableIssue) mockIssue.proxy();

        Mock mockOrderableField = new Mock(OrderableField.class);
        mockOrderableField.setStrict(true);


        Mock mockFieldScreenRenderLayoutItem1 = new Mock(FieldScreenRenderLayoutItem.class);
        mockFieldScreenRenderLayoutItem1.setStrict(true);
        mockFieldScreenRenderLayoutItem1.expectAndReturn("isShow", P.args(new IsEqual(issue)), Boolean.TRUE);
        mockFieldScreenRenderLayoutItem1.expectAndReturn("getOrderableField", mockOrderableField.proxy());

        FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem1 = (FieldScreenRenderLayoutItem) mockFieldScreenRenderLayoutItem1.proxy();

        int actionId = 5;
        ActionDescriptor ad = DescriptorFactory.getFactory().createActionDescriptor();
        ad.setId(actionId);

        OperationContext expectedOperationContext = new OperationContextImpl(new WorkflowIssueOperationImpl(ad), params);

        mockOrderableField.expectAndReturn("getId", P.ANY_ARGS, "blah");
        mockOrderableField.expectVoid("validateParams", new Constraint[]{new IsEqual(expectedOperationContext), new IsAnything(), new IsNull(), new IsEqual(issue), new IsEqual(fieldScreenRenderLayoutItem1)});

        WorkflowManager workflowManager = (WorkflowManager) mockWorkflowManager.proxy();

        WorkflowDescriptor wd = new WorkflowDescriptor();
        wd.addGlobalAction(ad);

        Mock mockWorkflow = new Mock(JiraWorkflow.class);
        mockWorkflow.setStrict(true);
        mockWorkflow.expectAndReturn("getDescriptor", wd);
        JiraWorkflow workflow = (JiraWorkflow) mockWorkflow.proxy();

        mockWorkflowManager.expectAndReturn("getWorkflow", P.args(new IsEqual(issue)), workflow);

        Mock mockFieldScreenRenderLayoutItem2 = new Mock(FieldScreenRenderLayoutItem.class);
        mockFieldScreenRenderLayoutItem2.setStrict(true);
        mockFieldScreenRenderLayoutItem2.expectAndReturn("isShow", P.args(new IsEqual(issue)), Boolean.FALSE);
        FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem2 = (FieldScreenRenderLayoutItem) mockFieldScreenRenderLayoutItem2.proxy();

        Mock mockFieldScreenRenderTab = new Mock(FieldScreenRenderTab.class);
        mockFieldScreenRenderTab.setStrict(true);
        mockFieldScreenRenderTab.expectAndReturn("getFieldScreenRenderLayoutItemsForProcessing", EasyList.build(fieldScreenRenderLayoutItem1, fieldScreenRenderLayoutItem2));

        final Mock mockFieldScreenRenderer = new Mock(FieldScreenRenderer.class);
        mockFieldScreenRenderer.setStrict(true);
        mockFieldScreenRenderer.expectAndReturn("getFieldScreenRenderTabs", EasyList.build(mockFieldScreenRenderTab.proxy()));



        WorkflowTransitionUtilImpl wu = new WorkflowTransitionUtilImpl((JiraAuthenticationContext) mockAuthenticationContext.proxy(), workflowManager, ComponentAccessor.getPermissionManager(), ComponentAccessor.getFieldScreenRendererFactory(), null)
        {
            public FieldScreenRenderer getFieldScreenRenderer()
            {
                return (FieldScreenRenderer) mockFieldScreenRenderer.proxy();
            }
        };


        wu.setAction(actionId);
        wu.setParams(params);
        wu.setIssue(issue);

        ErrorCollection errorCollection = wu.validate();
        assertFalse(errorCollection.hasAnyErrors());

        mockFieldScreenRenderer.verify();
        mockOrderableField.verify();
        mockFieldScreenRenderLayoutItem1.verify();
        mockFieldScreenRenderLayoutItem2.verify();
        mockIssue.verify();
        mockWorkflow.verify();
        verifyMocks();
    }

    public void testProgress()
    {
        mockAuthenticationContext.expectAndReturn("getLoggedInUser", u);
        
        Map params = new HashMap();

        Mock mockIssue = new Mock(MutableIssue.class);
        mockIssue.setStrict(true);
        Issue issue = (Issue) mockIssue.proxy();

        Mock mockOrderableField = new Mock(OrderableField.class);
        mockOrderableField.setStrict(true);

        Mock mockFieldScreenRenderLayoutItem1 = new Mock(FieldScreenRenderLayoutItem.class);
        mockFieldScreenRenderLayoutItem1.setStrict(true);
        mockFieldScreenRenderLayoutItem1.expectAndReturn("isShow", P.args(new IsEqual(issue)), Boolean.TRUE);
        mockFieldScreenRenderLayoutItem1.expectAndReturn("getOrderableField", mockOrderableField.proxy());
        mockFieldScreenRenderLayoutItem1.expectAndReturn("getFieldLayoutItem", null);

        FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem1 = (FieldScreenRenderLayoutItem) mockFieldScreenRenderLayoutItem1.proxy();
        mockOrderableField.expectVoid("updateIssue", P.args(new IsAnything(), new IsEqual(issue), new IsEqual(params)));

        Mock mockFieldScreenRenderLayoutItem2 = new Mock(FieldScreenRenderLayoutItem.class);
        mockFieldScreenRenderLayoutItem2.setStrict(true);
        mockFieldScreenRenderLayoutItem2.expectAndReturn("isShow", P.args(new IsEqual(issue)), Boolean.FALSE);
        FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem2 = (FieldScreenRenderLayoutItem) mockFieldScreenRenderLayoutItem2.proxy();

        Mock mockFieldScreenRenderTab = new Mock(FieldScreenRenderTab.class);
        mockFieldScreenRenderTab.setStrict(true);
        mockFieldScreenRenderTab.expectAndReturn("getFieldScreenRenderLayoutItemsForProcessing", EasyList.build(fieldScreenRenderLayoutItem1, fieldScreenRenderLayoutItem2));

        final Mock mockFieldScreenRenderer = new Mock(FieldScreenRenderer.class);
        mockFieldScreenRenderer.setStrict(true);
        mockFieldScreenRenderer.expectAndReturn("getFieldScreenRenderTabs", EasyList.build(mockFieldScreenRenderTab.proxy()));

        int actionId = 1;
        setupMockWorkflowManager(issue, actionId);

        WorkflowTransitionUtilImpl wu = new WorkflowTransitionUtilImpl((JiraAuthenticationContext) mockAuthenticationContext.proxy(), (WorkflowManager) mockWorkflowManager.proxy(), ComponentAccessor.getPermissionManager(), ComponentAccessor.getFieldScreenRendererFactory(), null)
        {
            public FieldScreenRenderer getFieldScreenRenderer()
            {
                return (FieldScreenRenderer) mockFieldScreenRenderer.proxy();
            }
        };

        mockWorkflowManager.expectVoid("doWorkflowAction", P.args(new IsEqual(wu)));

        wu.setAction(actionId);
        wu.setParams(params);
        wu.setIssue((MutableIssue) mockIssue.proxy());

        ErrorCollection errorCollection = wu.progress();
        assertFalse(errorCollection.hasAnyErrors());

        mockFieldScreenRenderer.verify();
        mockOrderableField.verify();
        mockFieldScreenRenderLayoutItem1.verify();
        mockFieldScreenRenderLayoutItem2.verify();
        mockIssue.verify();
        verifyMocks();
    }


    private void setupMockWorkflowManager(Issue issue, int actionId)
    {
        Mock mockWorkflow = new Mock(JiraWorkflow.class);
        mockWorkflow.setStrict(true);
        WorkflowDescriptor workflowDescriptor = new WorkflowDescriptor();
        ActionDescriptor actionDescriptor = DescriptorFactory.getFactory().createActionDescriptor();
        actionDescriptor.setId(actionId);
        actionDescriptor.setView(WorkflowTransitionUtil.VIEW_RESOLVE);
        workflowDescriptor.addGlobalAction(actionDescriptor);
        mockWorkflow.expectAndReturn("getDescriptor", workflowDescriptor);

        mockWorkflowManager.expectAndReturn("getWorkflow", P.args(new IsEqual(issue)), mockWorkflow.proxy());
    }
}
