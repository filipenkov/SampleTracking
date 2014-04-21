package com.atlassian.jira.config;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.exception.StoreException;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MockIssueFactory;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManagerImpl;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkImpl;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeImpl;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.CollectionReorderer;
import com.mockobjects.constraint.Constraint;
import com.mockobjects.constraint.IsAnything;
import com.mockobjects.constraint.IsEqual;
import com.mockobjects.constraint.IsNull;
import com.mockobjects.dynamic.Mock;
import com.mockobjects.dynamic.P;
import org.easymock.MockControl;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.MockClassControl;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilMisc;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.easymock.EasyMock.createMock;
import static org.easymock.classextension.EasyMock.verify;

public class TestDefaultSubTaskManager extends AbstractUsersTestCase
{
    private DefaultSubTaskManager defaultSubTaskManager;
    private Mock mockConstantsManager;
    private Mock mockIssueLinkManager;
    private Mock mockIssueLinkTypeManager;
    private Mock mockPermissionManager;
    private ApplicationProperties applicationProperties;
    private GenericValue issue;
    private MockGenericValue issueLinkTypeGV;

    private IssueTypeSchemeManager mockIssueTypeSchemeManager;
    private IssueManager mockIssueManager;
    private MockControl ctrlIssueTypeSchemeManager;

    public TestDefaultSubTaskManager(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        mockConstantsManager = new Mock(ConstantsManager.class);
        mockConstantsManager.setStrict(true);
        mockIssueLinkManager = new Mock(IssueLinkManager.class);
        mockIssueLinkManager.setStrict(true);
        mockIssueLinkTypeManager = new Mock(IssueLinkTypeManager.class);
        mockIssueLinkTypeManager.setStrict(true);
        applicationProperties = ComponentAccessor.getApplicationProperties();
        mockPermissionManager = new Mock(PermissionManager.class);
        mockPermissionManager.setStrict(true);

        issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test summary"));

        ctrlIssueTypeSchemeManager = MockClassControl.createControl(IssueTypeSchemeManagerImpl.class);
        mockIssueTypeSchemeManager = (IssueTypeSchemeManager) ctrlIssueTypeSchemeManager.getMock();


        defaultSubTaskManager = getDefaultSubTaskManager();
    }

    private DefaultSubTaskManager getDefaultSubTaskManager()
    {
        return getDefaultSubTaskManager(applicationProperties);
    }

    private DefaultSubTaskManager getDefaultSubTaskManager(final ApplicationProperties applicationProperties)
    {
        return new DefaultSubTaskManager((ConstantsManager) mockConstantsManager.proxy(),
                (IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), (IssueLinkManager) mockIssueLinkManager.proxy(),
                (PermissionManager) mockPermissionManager.proxy(), applicationProperties, new CollectionReorderer(),
                mockIssueTypeSchemeManager, mockIssueManager);
    }

    public void testCreateSubTaskIssueType() throws CreateException
    {
        String name = "sub-task issue type name";
        Long sequence = new Long(1);
        String description = "sub-task issue type description";
        String iconurl = "sub-task issue type icon url";

        mockConstantsManager.expectAndReturn("constantExists", P.args(new IsEqual("IssueType"), new IsEqual(name)), Boolean.FALSE);
        final MockGenericValue mockGv = new MockGenericValue("IssueType", EasyMap.build("id", "1", "name", name));
        mockConstantsManager.expectAndReturn("createIssueType", new Constraint[] { new IsEqual(name), new IsEqual(sequence), new IsEqual(SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE), new IsEqual(description), new IsEqual(iconurl) }, mockGv);

        mockIssueTypeSchemeManager.addOptionToDefault("1");
        ctrlIssueTypeSchemeManager.replay();


        defaultSubTaskManager.createSubTaskIssueType(name, sequence, description, iconurl);
        verifyMocks();
    }

    public void testUpdateSubTaskIssueType() throws StoreException
    {
        String id = "1";
        String name = "sub-task issue type name";
        Long sequence = new Long(1);
        String description = "sub-task issue type description";
        String iconurl = "sub-task issue type icon url";

        mockConstantsManager.expectVoid("updateIssueType", new Constraint[] { new IsEqual(id), new IsEqual(name), new IsEqual(sequence), new IsEqual(SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE), new IsEqual(description), new IsEqual(iconurl) });
        defaultSubTaskManager.updateSubTaskIssueType(id, name, sequence, description, iconurl);
        verifyMocks();
    }

    public void testCreateSubTaskIssueTypeAlreadyExists()
    {
        String name = "sub-task issue type name";
        Long sequence = new Long(1);
        String description = "sub-task issue type description";
        String iconurl = "sub-task issue type icon url";

        mockConstantsManager.expectAndReturn("constantExists", P.args(new IsEqual("IssueType"), new IsEqual(name)), Boolean.TRUE);

        try
        {
            defaultSubTaskManager.createSubTaskIssueType(name, sequence, description, iconurl);
            fail("Create exceoption should have been thrown.");
        }
        catch (CreateException e)
        {
            assertEquals("Issue Type with name '" + name + "' already exists.", e.getMessage());
        }

        verifyMocks();
    }

    public void testRemoveSubTaskIssueTypeDoesNotExist()
    {
        String name = "sub-task issue type name";

        mockConstantsManager.expectAndReturn("getIssueConstantByName", P.args(new IsEqual("IssueType"), new IsEqual(name)), null);

        try
        {
            defaultSubTaskManager.removeSubTaskIssueType(name);
            fail("Remove exception should have been thrown.");
        }
        catch (RemoveException e)
        {
            assertEquals("Issue Type with name '" + name + "' does not exist.", e.getMessage());
        }

        verifyMocks();
    }

    public void testRemoveSubTaskIssueTypeNotASubTask()
    {
        String name = "Bug";

        final IssueConstant issueType = new MockIssueType("Bug", name, false);

        mockConstantsManager.expectAndReturn("getIssueConstantByName", P.args(new IsEqual("IssueType"), new IsEqual(name)), issueType);

        try
        {
            defaultSubTaskManager.removeSubTaskIssueType(name);
            fail("Remove exception should have been thrown.");
        }
        catch (RemoveException e)
        {
            assertEquals("Issue Type with name '" + name + "' is not a sub-task issue type.", e.getMessage());
        }

        verifyMocks();
    }

    public void testRemoveSubTaskIssueType() throws RemoveException
    {
        String name = "sub-task issue type name";
        String id = "test-id";

        final IssueConstant issueType = new MockIssueType(id, name, true);

        mockConstantsManager.expectAndReturn("getIssueConstantByName", P.args(new IsEqual("IssueType"), new IsEqual(name)), issueType);
        mockConstantsManager.expectVoid("removeIssueType", P.args(new IsEqual(id)));

        defaultSubTaskManager.removeSubTaskIssueType(name);
        verifyMocks();
    }

    public void testEnableSubTasks() throws CreateException, GenericEntityException, StoreException
    {
        mockConstantsManager.expectAndReturn("getSubTaskIssueTypeObjects", Collections.EMPTY_LIST);
        mockConstantsManager.expectAndReturn("constantExists", P.args(new IsEqual("IssueType"), new IsEqual("Sub-task")), Boolean.FALSE);
        mockConstantsManager.expectAndReturn("createIssueType", new Constraint[] { new IsEqual("Sub-task"), new IsEqual(new Long(0)), new IsEqual(SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE), new IsEqual("The sub-task of the issue"), new IsEqual("/images/icons/issue_subtask.gif") }, new MockGenericValue("IssueType", EasyMap.build("id", "1")));
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkTypesByStyle", P.args(new IsEqual("jira_subtask")), Collections.EMPTY_LIST);
        mockIssueLinkTypeManager.expectVoid("createIssueLinkType", new Constraint[] { new IsEqual("jira_subtask_link"), new IsEqual("jira_subtask_outward"), new IsEqual("jira_subtask_inward"), new IsEqual("jira_subtask") });

        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS, true);
        EasyMock.expectLastCall();
        EasyMock.replay(mockApplicationProperties);

        defaultSubTaskManager = getDefaultSubTaskManager(mockApplicationProperties);

        defaultSubTaskManager.enableSubTasks();

        verifyMocks();
        verify(mockApplicationProperties);
    }

    public void testEnableSubTasksSubTaskIssueTypeExists()
            throws CreateException, GenericEntityException, StoreException
    {
        mockConstantsManager.expectAndReturn("getSubTaskIssueTypeObjects", EasyList.build("something"));
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkTypesByStyle", P.args(new IsEqual("jira_subtask")), EasyList.build("something"));

        final ApplicationProperties mockApplicationProperties = createMock(ApplicationProperties.class);
        mockApplicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS, true);
        EasyMock.expectLastCall();
        EasyMock.replay(mockApplicationProperties);
        defaultSubTaskManager = getDefaultSubTaskManager(mockApplicationProperties);
        defaultSubTaskManager.enableSubTasks();
        verifyMocks();
        verify(mockApplicationProperties);
    }

    public void testDisableSubTasks()
    {
        final ApplicationProperties applicationProperties = ComponentAccessor.getApplicationProperties();
        defaultSubTaskManager.disableSubTasks();

        assertFalse(applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS));
        verifyMocks();
    }

    public void testIssueTypeExistsByIdNullId()
    {
        try
        {
            defaultSubTaskManager.issueTypeExistsById(null);
            fail("IllegalArgumentException should have been thrown");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Id cannot be null.", e.getMessage());
        }
        verifyMocks();
    }

    public void testIssueTypeExistsById()
    {
        final String id = "1";
        final GenericValue issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", id, "name", "some other name", "sequence", new Long(1), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));

        mockConstantsManager.expectAndReturn("getIssueType", P.args(new IsEqual(id)), issueType);
        assertTrue(defaultSubTaskManager.issueTypeExistsById(id));
        verifyMocks();

        mockConstantsManager.expectAndReturn("getIssueType", P.args(new IsEqual(id)), null);
        defaultSubTaskManager = getDefaultSubTaskManager();
        assertFalse(defaultSubTaskManager.issueTypeExistsById(id));
        verifyMocks();
    }

    public void testIssueTypeExistsByName()
    {
        String name = "test name";
        mockConstantsManager.expectAndReturn("constantExists", P.args(new IsEqual("IssueType"), new IsEqual(name)), Boolean.FALSE);
        assertFalse(defaultSubTaskManager.issueTypeExistsByName(name));
        verifyMocks();

        mockConstantsManager.expectAndReturn("constantExists", P.args(new IsEqual("IssueType"), new IsEqual(name)), Boolean.TRUE);
        defaultSubTaskManager = getDefaultSubTaskManager();
        assertTrue(defaultSubTaskManager.issueTypeExistsByName(name));
        verifyMocks();
    }

    public void testMoveSubTaskIssueTypeUpNullId() throws StoreException
    {
        try
        {
            defaultSubTaskManager.moveSubTaskIssueTypeUp(null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Id cannot be null.", e.getMessage());
        }
        verifyMocks();
    }

    public void testMoveSubTaskIssueTypeUp() throws StoreException
    {
        int i = 0;
        final GenericValue issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "some name", "sequence", new Long(i++), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));
        final int sequence2 = i++;
        final GenericValue issueType2 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "2", "name", "some other name", "sequence", new Long(sequence2), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));
        final int sequence3 = i++;
        final String id = "3";
        final GenericValue issueType3 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", id, "name", "yet another name", "sequence", new Long(sequence3), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));

        mockConstantsManager.expectAndReturn("getEditableSubTaskIssueTypes", EasyList.build(issueType, issueType2, issueType3));
        mockConstantsManager.expectAndReturn("getIssueType", P.args(new IsEqual(id)), issueType3);
        mockConstantsManager.expectVoid("storeIssueTypes", P.args(new IsEqual(EasyList.build(issueType, issueType3, issueType2))));

        defaultSubTaskManager.moveSubTaskIssueTypeUp(id);

        assertEquals(new Long(sequence2 + 1), issueType2.getLong("sequence"));
        assertEquals(new Long(sequence3 - 1), issueType3.getLong("sequence"));

        verifyMocks();
    }

    public void testMoveSubTaskIssueTypeUpOneIssueType() throws StoreException
    {
        final String id = "1";
        int i = 0;
        final GenericValue issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", id, "name", "some name", "sequence", new Long(i), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));

        mockConstantsManager.expectAndReturn("getEditableSubTaskIssueTypes", EasyList.build(issueType));
        mockConstantsManager.expectAndReturn("getIssueType", P.args(new IsEqual(id)), issueType);
        mockConstantsManager.expectVoid("storeIssueTypes", P.args(new IsEqual(EasyList.build(issueType))));

        defaultSubTaskManager.moveSubTaskIssueTypeUp(id);

        assertEquals(new Long(i), issueType.getLong("sequence"));

        verifyMocks();
    }

    public void testMoveSubTaskIssueTypeDownNullId() throws StoreException
    {
        try
        {
            defaultSubTaskManager.moveSubTaskIssueTypeDown(null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Id cannot be null.", e.getMessage());
        }
        verifyMocks();
    }

    public void testMoveSubTaskIssueTypeDown() throws StoreException
    {
        int i = 0;
        final GenericValue issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "1", "name", "some name", "sequence", new Long(i++)));
        final int sequence2 = i++;
        String id = "2";
        final GenericValue issueType2 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", id, "name", "some other name", "sequence", new Long(sequence2)));
        final int sequence3 = i++;
        final GenericValue issueType3 = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", "3", "name", "yet another name", "sequence", new Long(sequence3)));

        mockConstantsManager.expectAndReturn("getEditableSubTaskIssueTypes", EasyList.build(issueType, issueType2, issueType3));
        mockConstantsManager.expectAndReturn("getIssueType", P.args(new IsEqual(id)), issueType2);
        mockConstantsManager.expectVoid("storeIssueTypes", P.args(new IsEqual(EasyList.build(issueType, issueType3, issueType2))));

        defaultSubTaskManager.moveSubTaskIssueTypeDown(id);

        assertEquals(new Long(sequence2 + 1), issueType2.getLong("sequence"));
        assertEquals(new Long(sequence3 - 1), issueType3.getLong("sequence"));

        verifyMocks();
    }

    public void testMoveSubTaskIssueTypeDownOneIssueType() throws StoreException
    {
        final String id = "1";
        int i = 0;
        final GenericValue issueType = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", id, "name", "some name", "sequence", new Long(i)));

        mockConstantsManager.expectAndReturn("getEditableSubTaskIssueTypes", EasyList.build(issueType));
        mockConstantsManager.expectAndReturn("getIssueType", P.args(new IsEqual(id)), issueType);
        mockConstantsManager.expectVoid("storeIssueTypes", P.args(new IsEqual(EasyList.build(issueType))));

        defaultSubTaskManager.moveSubTaskIssueTypeDown(id);

        assertEquals(new Long(i), issueType.getLong("sequence"));

        verifyMocks();
    }

    public void testGetSubTaskIssueTypeByIdNullId()
    {
        try
        {
            defaultSubTaskManager.getSubTaskIssueTypeById(null);
            fail("IllegalArgumentException should have bben thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Id cannot be null.", e.getMessage());
        }
        verifyMocks();
    }

    public void testGetSubTaskIssueTypeByIdIssueTypeDoesNotExist()
    {
        final String id = "1";
        mockConstantsManager.expectAndReturn("getIssueType", P.args(new IsEqual(id)), null);
        final GenericValue subTaskIssueType = defaultSubTaskManager.getSubTaskIssueTypeById(id);
        assertNull(subTaskIssueType);
        verifyMocks();
    }

    public void testGetSubTaskIssueTypeByIdIssueTypeIsNotSubTaskIssueType()
    {
        // Test that the manager detects that the issue type is not an sub-task issue type
        final String id = "1";
        final GenericValue issueTypeGV = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", id, "name", "some name"));
        try
        {
            mockConstantsManager.expectAndReturn("getIssueType", P.args(new IsEqual(id)), issueTypeGV);
            defaultSubTaskManager.getSubTaskIssueTypeById(id);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("The issue type with id '" + id + "' is not a sub-task issue type.", e.getMessage());
        }

        verifyMocks();
    }

    public void testGetSubTaskIssueTypeById()
    {
        // Test that the manager detects that the issue type is not an sub-task issue type
        final String id = "1";
        final String name = "some name";
        final GenericValue issueTypeGV = UtilsForTests.getTestEntity("IssueType", EasyMap.build("id", id, "name", name, "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE, "sequence", new Long(0)));

        mockConstantsManager.expectAndReturn("getIssueType", P.args(new IsEqual(id)), issueTypeGV);
        final GenericValue subTaskIssueTypeGV = defaultSubTaskManager.getSubTaskIssueTypeById(id);
        assertNotNull(subTaskIssueTypeGV);
        assertEquals(id, subTaskIssueTypeGV.getString("id"));
        assertEquals(name, subTaskIssueTypeGV.getString("name"));
        assertEquals(SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE, subTaskIssueTypeGV.getString("style"));
        verifyMocks();
    }

    public void testGetSubTaskIssueTypeObjects()
    {
        final List expected = EasyList.build("test 1", "test 2");
        mockConstantsManager.expectAndReturn("getSubTaskIssueTypeObjects", expected);
        assertEquals(expected, defaultSubTaskManager.getSubTaskIssueTypeObjects());
        verifyMocks();
    }

    public void testIsSubtask()
    {
        // Test false
        mockIssueLinkManager.expectAndReturn("getInwardLinks", P.args(new IsEqual(issue.getLong("id"))), Collections.EMPTY_LIST);
        assertFalse(defaultSubTaskManager.isSubTask(issue));
        verifyMocks();

        // Test True
        MockGenericValue issueLinkTypeGV = new MockGenericValue("IssueLinkType", EasyMap.build("id", new Long(1), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkType", P.args(new IsEqual(issueLinkTypeGV.getLong("id"))), new IssueLinkTypeImpl(issueLinkTypeGV));
        MockGenericValue issueLinkGV = new MockGenericValue("IssueLink", EasyMap.build("linktype", issueLinkTypeGV.getLong("id"), "source", new Long(10000)));
        mockIssueLinkManager.expectAndReturn("getInwardLinks", P.args(new IsEqual(issue.getLong("id"))), EasyList.build(new IssueLinkImpl(issueLinkGV, (IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), null)));
        defaultSubTaskManager = getDefaultSubTaskManager();
        assertTrue(defaultSubTaskManager.isSubTask(issue));
        verifyMocks();
    }

    public void testIsSubTaskIssueType()
    {
        assertFalse(defaultSubTaskManager.isSubTaskIssueType(new MockGenericValue("IssueType")));
        assertFalse(defaultSubTaskManager.isSubTaskIssueType(new MockGenericValue("IssueType", UtilMisc.toMap("description", "no style"))));
        assertFalse(defaultSubTaskManager.isSubTaskIssueType(new MockGenericValue("IssueType", UtilMisc.toMap("style", null))));
        assertFalse(defaultSubTaskManager.isSubTaskIssueType(new MockGenericValue("IssueType", UtilMisc.toMap("style", ""))));
        assertFalse(defaultSubTaskManager.isSubTaskIssueType(new MockGenericValue("IssueType", UtilMisc.toMap("style", "invalid"))));

        assertTrue(defaultSubTaskManager.isSubTaskIssueType(new MockGenericValue("IssueType", UtilMisc.toMap("style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE))));
    }

    public void testGetParentIssueIdNoParrent()
    {
        mockIssueLinkManager.expectAndReturn("getInwardLinks", P.args(new IsEqual(issue.getLong("id"))), Collections.EMPTY_LIST);
        assertNull(defaultSubTaskManager.getParentIssueId(issue));
        verifyMocks();
    }

    public void testGetParentIssue()
    {
        MockGenericValue issueLinkTypeGV = new MockGenericValue("IssueLinkType", EasyMap.build("id", new Long(1), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkType", P.args(new IsEqual(issueLinkTypeGV.getLong("id"))), new IssueLinkTypeImpl(issueLinkTypeGV));
        final Long parentIssueId = new Long(10000);
        MockGenericValue issueLinkGV = new MockGenericValue("IssueLink", EasyMap.build("linktype", issueLinkTypeGV.getLong("id"), "source", parentIssueId));
        mockIssueLinkManager.expectAndReturn("getInwardLinks", P.args(new IsEqual(issue.getLong("id"))), EasyList.build(new IssueLinkImpl(issueLinkGV, (IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), null)));
        assertEquals(parentIssueId, defaultSubTaskManager.getParentIssueId(issue));
        verifyMocks();
    }

    public void testGetSubTaskBean()
    {
        setupSubTasks(issue);

        mockPermissionManager.expectAndReturn("hasPermission", P.args(new IsEqual(new Integer(Permissions.BROWSE)), new IsAnything(), new IsNull()), Boolean.TRUE);
        final SubTaskBean subTaskBean = defaultSubTaskManager.getSubTaskBean(issue, null);
        final Collection subTasks = subTaskBean.getSubTasks(SubTaskBean.SUB_TASK_VIEW_ALL);
        assertEquals(2, subTasks.size());

        verifyMocks();
    }

    private List setupSubTasks(final GenericValue issue)
    {
        issueLinkTypeGV = new MockGenericValue("IssueLinkType", EasyMap.build("id", new Long(1), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkType", P.args(new IsEqual(issueLinkTypeGV.getLong("id"))), new IssueLinkTypeImpl(issueLinkTypeGV));

        final GenericValue subTaskIssue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test summary"));
        final GenericValue subTaskIssue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "test summary"));

        MockGenericValue issueLinkGV1 = new MockGenericValue("IssueLink", EasyMap.build("linktype", issueLinkTypeGV.getLong("id"), "destination", subTaskIssue1.getLong("id")));
        MockGenericValue issueLinkGV2 = new MockGenericValue("IssueLink", EasyMap.build("linktype", issueLinkTypeGV.getLong("id"), "destination", subTaskIssue2.getLong("id")));

        MockIssueManager mockIssueManager = new MockIssueManager();
        mockIssueManager.addIssue(subTaskIssue1);
        mockIssueManager.addIssue(subTaskIssue2);

        final List issueLinks = EasyList.build(new IssueLinkImpl(issueLinkGV1, (IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), mockIssueManager), new IssueLinkImpl(issueLinkGV2, (IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), mockIssueManager));
        mockIssueLinkManager.expectAndReturn("getOutwardLinks", P.args(new IsEqual(issue.getLong("id"))), issueLinks);
        return issueLinks;
    }

    public void testMoveSubTask()
    {
        final List expectedIssueLinks = setupSubTasks(issue);

        Long currentSequence = new Long(0);
        Long sequence = new Long(1);
        mockIssueLinkManager.expectVoid("moveIssueLink", P.args(new IsEqual(expectedIssueLinks), new IsEqual(currentSequence), new IsEqual(sequence)));

        defaultSubTaskManager.moveSubTask(issue, currentSequence, sequence);

        verifyMocks();
    }

    public void testResetSequences()
    {
        final List subTaskIssueLinks = setupSubTasks(issue);
        mockIssueLinkManager.expectVoid("resetSequences", P.args(new IsEqual(subTaskIssueLinks)));

        defaultSubTaskManager.resetSequences(MockIssueFactory.createIssue(issue.getLong("id")));
        verifyMocks();
    }

    public void testCreateSubTaskIssueLinkWithNulls() throws CreateException
    {
        try
        {
            defaultSubTaskManager.createSubTaskIssueLink((GenericValue) null, null, null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Parent Issue cannot be null.", e.getMessage());
        }

        try
        {
            defaultSubTaskManager.createSubTaskIssueLink(issue, null, null);
            fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("Sub-Task Issue cannot be null.", e.getMessage());
        }

        verifyMocks();
    }

    public void testCreateSubTaskIssueLinkException()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final GenericValue subTaskIssue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "sub task test issue"));

        final Long subTaskIssueLinkTypeId = new Long(9879);
        MockGenericValue issueLinkTypeGV = new MockGenericValue("IssueLinkType", EasyMap.build("id", subTaskIssueLinkTypeId));
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkTypesByStyle", P.args(new IsEqual(SubTaskManager.SUB_TASK_LINK_TYPE_STYLE)), EasyList.build(new IssueLinkTypeImpl(issueLinkTypeGV)));
        mockIssueLinkManager.expectAndReturn("getOutwardLinks", P.args(new IsEqual(issue.getLong("id"))), Collections.EMPTY_LIST);
        User testUser = createMockUser("testUser");
        final String expectedMessage = "test exception";
        mockIssueLinkManager.expectAndThrow("createIssueLink", new Constraint[] { new IsEqual(issue.getLong("id")), new IsEqual(subTaskIssue.getLong("id")), new IsEqual(subTaskIssueLinkTypeId), new IsEqual(new Long(0)), new IsEqual(testUser) }, new CreateException(expectedMessage));

        try
        {
            defaultSubTaskManager.createSubTaskIssueLink(issue, subTaskIssue, testUser);
            fail("CreateException should have been thrown.");
        }
        catch (CreateException e)
        {
            assertEquals(expectedMessage, e.getMessage());
        }

        verifyMocks();
    }

    public void testCreateSubTaskIssueLinkFirstSubTaskLink()
            throws CreateException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final GenericValue subTaskIssue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "sub task test issue"));

        final Long subTaskIssueLinkTypeId = new Long(9879);
        issueLinkTypeGV = new MockGenericValue("IssueLinkType", EasyMap.build("id", subTaskIssueLinkTypeId));
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkTypesByStyle", P.args(new IsEqual(SubTaskManager.SUB_TASK_LINK_TYPE_STYLE)), EasyList.build(new IssueLinkTypeImpl(issueLinkTypeGV)));
        mockIssueLinkManager.expectAndReturn("getOutwardLinks", P.args(new IsEqual(issue.getLong("id"))), Collections.EMPTY_LIST);
        User testUser = createMockUser("testUser");
        mockIssueLinkManager.expectVoid("createIssueLink", new Constraint[] { new IsEqual(issue.getLong("id")), new IsEqual(subTaskIssue.getLong("id")), new IsEqual(subTaskIssueLinkTypeId), new IsEqual(new Long(0)), new IsEqual(testUser) });

        defaultSubTaskManager.createSubTaskIssueLink(issue, subTaskIssue, testUser);

        verifyMocks();
    }

    public void testCreateSubTaskIssueLink()
            throws CreateException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final GenericValue subTaskIssue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "sub task test issue"));

        final List subTaskIssueLinks = setupSubTasks(issue);

        User testUser = createMockUser("testUser");
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkTypesByStyle", P.args(new IsEqual(SubTaskManager.SUB_TASK_LINK_TYPE_STYLE)), EasyList.build(new IssueLinkTypeImpl(issueLinkTypeGV)));
        mockIssueLinkManager.expectAndReturn("getOutwardLinks", P.args(new IsEqual(issue.getLong("id"))), subTaskIssueLinks);
        mockIssueLinkManager.expectVoid("createIssueLink", new Constraint[] { new IsEqual(issue.getLong("id")), new IsEqual(subTaskIssue.getLong("id")), new IsEqual(issueLinkTypeGV.getLong("id")), new IsEqual(new Long(subTaskIssueLinks.size())), new IsEqual(testUser) });

        defaultSubTaskManager.createSubTaskIssueLink(issue, subTaskIssue, testUser);

        verifyMocks();
    }


    public void testGetAllSubTaskIssueIds()
    {
        final List issueLinks = setupSubTasks(issue);
        final GenericValue issue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "another issue"));
        issueLinks.addAll(setupSubTasks(issue2));

        final IssueLinkType issueLinkType = new IssueLinkTypeImpl(issueLinkTypeGV);
        mockIssueLinkTypeManager.expectAndReturn("getIssueLinkTypesByStyle", P.args(new IsEqual(SubTaskManager.SUB_TASK_LINK_TYPE_STYLE)), EasyList.build(issueLinkType));
        mockIssueLinkManager.expectAndReturn("getIssueLinks", P.args(new IsEqual(issueLinkType.getId())), issueLinks);
        final Collection result = defaultSubTaskManager.getAllSubTaskIssueIds();
        assertEquals(4, result.size());
        // As the order does not matter check for presence of ids
        for (Iterator iterator = issueLinks.iterator(); iterator.hasNext();)
        {
            IssueLink issueLink = (IssueLink) iterator.next();
            assertTrue(result.contains(issueLink.getDestinationId()));
        }
    }

    /**
     * This tests JRA-10546
     *
     * @throws CreateException
     * @throws RemoveException
     */
    public void testChangeParentUpdatesSecurityLevel() throws CreateException, RemoveException
    {
        JiraMockGenericValue subtaskIssueGv = new JiraMockGenericValue("Issue", EasyMap.build("key", "HSP-12", "security", null));
        Long securityLevelId = new Long(10000);
        final JiraMockGenericValue parentIssueGv = new JiraMockGenericValue("Issue", EasyMap.build("security", securityLevelId, "key", "HSP-11"));

        mockIssueLinkManager.expectAndReturn("getInwardLinks", P.ANY_ARGS, Collections.EMPTY_LIST);
        mockIssueLinkManager.expectVoid("resetSequences", P.ANY_ARGS);

        DefaultSubTaskManager ditm = new DefaultSubTaskManager((ConstantsManager) mockConstantsManager.proxy(),
                (IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), (IssueLinkManager) mockIssueLinkManager.proxy(),
                (PermissionManager) mockPermissionManager.proxy(), applicationProperties, new CollectionReorderer(),
                mockIssueTypeSchemeManager, mockIssueManager)
        {
            @Override
            public GenericValue getParentIssue(GenericValue subtask)
            {
                return parentIssueGv;
            }

            @Override
            public void createSubTaskIssueLink(GenericValue parentIssue, GenericValue subTaskIssue, User remoteUser)
                    throws CreateException
            {
                //do nothing
            }

            @Override
            public List<IssueLink> getSubTaskIssueLinks(final Long issueId)
            {
                return null;
            }

        };


        IssueUpdateBean issueUpdateBean = ditm.changeParent(subtaskIssueGv, parentIssueGv, null);
        assertEquals(securityLevelId, issueUpdateBean.getChangedIssue().get("security"));
    }

    /**
     * This tests JRA-10546
     *
     * @throws CreateException
     * @throws RemoveException
     */
    public void testChangeParentSetsSecurityLevelToNull() throws CreateException, RemoveException
    {
        Long securityLevelId = new Long(10000);
        JiraMockGenericValue subtaskIssueGv = new JiraMockGenericValue("Issue", EasyMap.build("key", "HSP-12", "security", securityLevelId));
        final JiraMockGenericValue parentIssueGv = new JiraMockGenericValue("Issue", EasyMap.build("key", "HSP-11"));

        mockIssueLinkManager.expectAndReturn("getInwardLinks", P.ANY_ARGS, Collections.EMPTY_LIST);
        mockIssueLinkManager.expectVoid("resetSequences", P.ANY_ARGS);

        DefaultSubTaskManager ditm = new DefaultSubTaskManager((ConstantsManager) mockConstantsManager.proxy(),
                (IssueLinkTypeManager) mockIssueLinkTypeManager.proxy(), (IssueLinkManager) mockIssueLinkManager.proxy(),
                (PermissionManager) mockPermissionManager.proxy(), applicationProperties, new CollectionReorderer(),
                mockIssueTypeSchemeManager, mockIssueManager)
        {
            public GenericValue getParentIssue(GenericValue subtask)
            {
                return parentIssueGv;
            }

            public void createSubTaskIssueLink(GenericValue parentIssue, GenericValue subTaskIssue, User remoteUser)
                    throws CreateException
            {
                //do nothing
            }

            @Override
            public List<IssueLink> getSubTaskIssueLinks(final Long issueId)
            {
                return null;
            }

        };


        IssueUpdateBean issueUpdateBean = ditm.changeParent(subtaskIssueGv, parentIssueGv, null);
        assertNull(issueUpdateBean.getChangedIssue().get("security"));
    }

    private void verifyMocks()
    {
        mockPermissionManager.verify();
        mockConstantsManager.verify();
        mockIssueLinkManager.verify();
        mockIssueLinkTypeManager.verify();
    }

    /**
     * This should really live in atlassian-ofbiz.
     */
    public class JiraMockGenericValue extends MockGenericValue
    {

        public JiraMockGenericValue(String entityName, Map fields)
        {
            super(entityName, fields);
        }

        public Set entrySet()
        {
            return getAllFields().entrySet();
        }

        public Set keySet()
        {
            return getAllFields().keySet();
        }

        public int size()
        {
            return getAllFields().size();
        }

        public boolean isEmpty()
        {
            return getAllFields().isEmpty();
        }

        public Collection values()
        {
            return getAllFields().values();
        }

        public Object clone()
        {
            return new JiraMockGenericValue(entityName, new HashMap(getAllFields()));
        }
    }
}
