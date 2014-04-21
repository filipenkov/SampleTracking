package com.atlassian.jira.issue.managers;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.ofbiz.test.mock.MockAtlassianServletRequest;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.local.AbstractUsersIndexingTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.google.common.collect.Lists;
import com.mockobjects.servlet.MockHttpServletRequest;
import org.easymock.classextension.EasyMock;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ServletActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestDefaultIssueManager extends AbstractUsersIndexingTestCase
{
    private GenericValue issue1;
    private GenericValue issue2;
    private GenericValue issue3;
    private User u1;

    IssueSearcherManager issueSearcherManager;
    private FieldVisibilityBean origFieldVisibilityBean;

    SearchSortUtil searchSortUtil;

    public TestDefaultIssueManager(String s)
    {
        super(s);
    }

    public void setup() throws Exception
    {
        super.setUp();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        ManagerFactory.addService(FieldVisibilityBean.class, origFieldVisibilityBean);
    }

    public void testIssueLong()
            throws Exception, CreateException
    {
        setUpTests();
        ComponentAccessor.getIssueManager().getIssue(new Long(1));
    }

    public void testIssueString()
            throws Exception, CreateException
    {
        setUpTests();
        ComponentAccessor.getIssueManager().getIssue("ABC-7348");
    }

    public void testIssueWorkflow()
            throws Exception, CreateException
    {
        setUpTests();
        ComponentAccessor.getIssueManager().getIssueByWorkflow(new Long(1000));
    }

    public void testEntitiesByIssue()
            throws Exception, CreateException
    {
        setUpTests();

        GenericValue value = ComponentAccessor.getOfBizDelegator().findByPrimaryKey("Issue", EasyMap.build("id", new Long(2)));
        List issues = ComponentAccessor.getIssueManager().getEntitiesByIssue(IssueRelationConstants.VERSION, value);
        issues = EntityUtil.orderBy(issues, EasyList.build("id asc"));
        assertTrue(!issues.isEmpty());
        assertEquals(3, issues.size());
        assertEquals(new Long(1000), ((GenericValue) issues.get(0)).getLong("id"));
        assertEquals(new Long(1001), ((GenericValue) issues.get(1)).getLong("id"));
        assertEquals(new Long(1002), ((GenericValue) issues.get(2)).getLong("id"));

        assertEquals(0, ComponentAccessor.getIssueManager().getEntitiesByIssue("foobar", null).size());
    }

    public void testVotedIssues()
            throws Exception, CreateException
    {
        setUpTests();

        List votedIssues = ComponentAccessor.getIssueManager().getVotedIssues(new MockUser("Owen Fellows"));
        assertTrue(!votedIssues.isEmpty());
        assertEquals(2, votedIssues.size());
        assertTrue(1 == ((Issue) votedIssues.get(0)).getLong("id") || 3 == ((Issue) votedIssues.get(0)).getLong("id"));
        assertTrue(1 == ((Issue) votedIssues.get(1)).getLong("id") || 3 == ((Issue) votedIssues.get(1)).getLong("id"));
    }

    public void testIssueWatchers()
            throws Exception, GenericEntityException, CreateException
    {
        setUpTests();

        //GenericValue issue = ComponentAccessor.getOfBizDelegator().findByPrimaryKey("Issue", EasyMap.build("id", new Long(1)));
        Issue issue = new MockIssue(1);
        List issuesWatched = ComponentAccessor.getIssueManager().getWatchers(issue);
        assertTrue(!issuesWatched.isEmpty());
        assertEquals(2, issuesWatched.size());
        assertTrue("Owen Fellows".equals(((User) issuesWatched.get(0)).getName()) || "Watcher 1".equals(((User) issuesWatched.get(0)).getName()));
        assertTrue("Owen Fellows".equals(((User) issuesWatched.get(1)).getName()) || "Watcher 1".equals(((User) issuesWatched.get(1)).getName()));
    }

    public void testWatchedIssues()
            throws Exception, CreateException
    {
        setUpTests();

        List votedIssues = ComponentAccessor.getIssueManager().getWatchedIssues(new MockUser("Owen Fellows"));
        assertTrue(!votedIssues.isEmpty());
        assertEquals(2, votedIssues.size());
        assertTrue(1 == ((Issue) votedIssues.get(0)).getLong("id").longValue() || 2 == ((Issue) votedIssues.get(0)).getLong("id").longValue());
        assertTrue(1 == ((Issue) votedIssues.get(1)).getLong("id").longValue() || 2 == ((Issue) votedIssues.get(1)).getLong("id").longValue());
    }

    public void testIssueIdsForProject() throws Exception
    {
        setUpTests();

        // Ensure that the correct issue ids are returned
        IssueManager issueManager = ComponentAccessor.getIssueManager();

        Collection issueIds = issueManager.getIssueIdsForProject(new Long(10));
        assertNotNull(issueIds);
        assertEquals(3, issueIds.size());

        assertTrue(issueIds.contains(new Long(1)));
        assertTrue(issueIds.contains(new Long(2)));
        assertTrue(issueIds.contains(new Long(3)));

        try
        {
            // Ensure that a null pointer exception is thrown
            issueManager.getIssueIdsForProject(null);
            fail("NullPointerException should have been thrown.");
        }
        catch (NullPointerException e)
        {
            assertEquals("Project Id cannot be null.", e.getMessage());
        }

        // Ensure that the empty collection is returned for a non-existent project
        issueIds = issueManager.getIssueIdsForProject(new Long(1));
        assertNotNull(issueIds);
        assertTrue(issueIds.isEmpty());

        // Ensure that the empty collection is returned for a non-existent project
        issueIds = issueManager.getIssueIdsForProject(new Long(4));
        assertNotNull(issueIds);
        assertTrue(issueIds.isEmpty());
    }


    public void testGetComments() throws GenericEntityException
    {
        ManagerFactory.addService(IssueManager.class, new DefaultIssueManager(null, null, null, null, null, null));

        GenericValue issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "a Summary"));
        GenericValue comment = UtilsForTests.getTestEntity("Action", EasyMap.build("issue", issue.getLong("id"), "type", ActionConstants.TYPE_COMMENT));
        List comments = ComponentAccessor.getIssueManager().getEntitiesByIssue(IssueRelationConstants.COMMENTS, issue);
        assertEquals(1, comments.size());
        assertEquals(comment, comments.get(0));
        ComponentAccessor.getOfBizDelegator().removeAll(EasyList.build(comment));

        List allComments = ComponentAccessor.getOfBizDelegator().findAll("Action");
        assertTrue(allComments.isEmpty());

        List noComments = ComponentAccessor.getIssueManager().getEntitiesByIssue(IssueRelationConstants.COMMENTS, issue);
        assertTrue(noComments.isEmpty());
    }

    public void testGetIssueObjectWithNoIssueReturnsNull()
    {
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        assertNull(issueManager.getIssueObject(33333L));
        assertNull(issueManager.getIssueObject("BS-1"));
    }

    public void testGetIssueCountForProject() throws Exception
    {
        setUpTests();
        // Create 1 issue that is not part of the project so we know the count is constrained by project
        UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(4), "key", "ABC-7351", "workflowId", new Long(1003), "priority", "Z", "project", new Long(11)));
        IssueManager issueManager = ComponentAccessor.getIssueManager();
        assertEquals(3, issueManager.getIssueCountForProject(10L));
    }

    public void testGetIssueObjects() throws Exception
    {
        setUpTests();
        ArrayList<Long> issueIds = Lists.newArrayList(new Long(1), new Long(2));
        List<Issue> issues = ComponentAccessor.getIssueManager().getIssueObjects(issueIds);
        assertEquals(2, issues.size());
        assertEquals("ABC-7348", issues.get(0).getKey());
        assertEquals("ABC-7349", issues.get(1).getKey());
    }

    public void testGetIssueObjectsByEntity() throws Exception
    {
        setUpTests();
        GenericValue version = ComponentAccessor.getOfBizDelegator().findByPrimaryKey("Version", EasyMap.build("id", new Long(1002)));
        List<Issue> issues = ComponentAccessor.getIssueManager().getIssueObjectsByEntity(IssueRelationConstants.VERSION, version);
        assertTrue(!issues.isEmpty());
        assertEquals(3, issues.size());
        assertEquals("ABC-7348", issues.get(0).getKey());
        assertEquals("ABC-7349", issues.get(1).getKey());
        assertEquals("ABC-7350", issues.get(2).getKey());
    }

    private void setUpTests() throws Exception
    {
        issueSearcherManager = (IssueSearcherManager) ComponentManager.getComponentInstanceOfType(IssueSearcherManager.class);

        origFieldVisibilityBean = ComponentManager.getComponentInstanceOfType(FieldVisibilityBean.class);
        final FieldVisibilityBean visibilityBean = EasyMock.createMock(FieldVisibilityBean.class);
        EasyMock.expect(visibilityBean.isFieldHidden((String)EasyMock.anyObject(), (Issue)EasyMock.anyObject())).andReturn(false).anyTimes();
        EasyMock.replay(visibilityBean);
        ManagerFactory.addService(FieldVisibilityBean.class, visibilityBean);

        CrowdService crowdService = ComponentManager.getComponentInstanceOfType(CrowdService.class);
        u1 = createMockUser("Owen Fellows");

        Group g1 = createMockGroup("Test Group");
        addUserToGroup(u1, g1);

        User u2 = createMockUser("Watcher 1");

        MockHttpServletRequest request = new MockAtlassianServletRequest();
        ServletActionContext.setRequest(request);

        JiraTestUtil.loginUser(u1);
//        HttpSession session = new MockHttpSession();
//        request.setSession(session);
//        session.setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, u1);
//        ActionContext.setSession(EasyMap.build(DefaultAuthenticator.LOGGED_IN_KEY, u1));

        GenericValue project = UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(10)));
        GenericValue scheme = JiraTestUtil.setupAndAssociateDefaultPermissionScheme(project);
        ManagerFactory.getPermissionManager().addPermission(Permissions.BROWSE, scheme, "Test Group", GroupDropdown.DESC);

        issue1 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(1), "key", "ABC-7348", "workflowId", new Long(1000), "priority", "C", "project", new Long(10)));
        issue2 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(2), "key", "ABC-7349", "workflowId", new Long(1001), "priority", "B", "project", new Long(10)));
        issue3 = UtilsForTests.getTestEntity("Issue", EasyMap.build("id", new Long(3), "key", "ABC-7350", "workflowId", new Long(1002), "priority", "A", "project", new Long(10)));

        GenericValue version1 = UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(1000)));
        GenericValue version2 = UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(1001)));
        GenericValue version3 = UtilsForTests.getTestEntity("Version", EasyMap.build("id", new Long(1002)));

        ComponentAccessor.getComponent(NodeAssociationStore.class).createAssociation(issue2, version1, IssueRelationConstants.VERSION);
        ComponentAccessor.getComponent(NodeAssociationStore.class).createAssociation(issue2, version2, IssueRelationConstants.VERSION);
        ComponentAccessor.getComponent(NodeAssociationStore.class).createAssociation(issue2, version3, IssueRelationConstants.VERSION);

        ComponentAccessor.getComponent(NodeAssociationStore.class).createAssociation(issue1, version3, IssueRelationConstants.VERSION);
        ComponentAccessor.getComponent(NodeAssociationStore.class).createAssociation(issue3, version3, IssueRelationConstants.VERSION);

        ComponentAccessor.getComponent(UserAssociationStore.class).createAssociation("VoteIssue", u1, issue1);
        ComponentAccessor.getComponent(UserAssociationStore.class).createAssociation("VoteIssue", u1, issue3);

        ComponentAccessor.getComponent(UserAssociationStore.class).createAssociation("WatchIssue", u1, issue1);
        ComponentAccessor.getComponent(UserAssociationStore.class).createAssociation("WatchIssue", u1, issue2);

        ComponentAccessor.getComponent(UserAssociationStore.class).createAssociation("WatchIssue", u2, issue1);

        ManagerFactory.getIndexManager().reIndexAll();
    }
}
