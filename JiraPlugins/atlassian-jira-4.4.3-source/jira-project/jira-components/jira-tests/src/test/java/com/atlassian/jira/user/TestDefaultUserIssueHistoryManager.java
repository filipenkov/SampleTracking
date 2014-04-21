package com.atlassian.jira.user;

import com.atlassian.jira.local.MockControllerTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.collect.CollectionBuilder;
import org.easymock.AbstractMatcher;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of the UserHistoryManager.
 *
 * @since v4.0
 */
public class TestDefaultUserIssueHistoryManager extends MockControllerTestCase
{

    private UserHistoryManager historyManager;
    private IssueManager issueManager;
    private PermissionManager permissionManager;
    private ApplicationProperties applicationProperties;

    private Issue issue;

    private UserIssueHistoryManager issueHistoryManager;
    private User user;

    private static final AbstractMatcher IGNORE_TIMESTAMP_ARGUMENTS_MATCHER = new AbstractMatcher()
    {
        @Override
        protected boolean argumentMatches(Object o, Object o1)
        {
            if (o instanceof UserHistoryItem && o1 instanceof UserHistoryItem)
            {
                final UserHistoryItem item1 = (UserHistoryItem) o;
                final UserHistoryItem item2 = (UserHistoryItem) o;

                return item1.getType().equals(item2.getType()) && item1.getEntityId().equals(item2.getEntityId());
            }
            return super.argumentMatches(o, o1);
        }
    };


    @Before
    public void setUp() throws Exception
    {

        user = new MockUser("admin");
        historyManager = mockController.getMock(UserHistoryManager.class);
        issueManager = mockController.getMock(IssueManager.class);
        permissionManager = mockController.getMock(PermissionManager.class);
        applicationProperties = mockController.getMock(ApplicationProperties.class);
        issue = mockController.getMock(Issue.class);

        issueHistoryManager = new DefaultUserIssueHistoryManager(historyManager, permissionManager, issueManager, applicationProperties);
    }

    @After
    public void tearDown() throws Exception
    {
        user = null;
        historyManager = null;
        issueManager = null;
        permissionManager = null;
        applicationProperties = null;
        issueHistoryManager = null;
        issue = null;

    }

    @Test
    public void testAddIssueToHistoryNoissue()
    {
        mockController.replay();

        try
        {
            issueHistoryManager.addIssueToHistory(user, null);
            fail("issue can not be null");

        }
        catch (IllegalArgumentException e)
        {
            // ignore
        }

        mockController.verify();
    }

    @Test
    public void testAddIssueToHistoryNoUser()
    {
        issue.getId();
        mockController.setReturnValue(123L);

        historyManager.addItemToHistory(UserHistoryItem.ISSUE, (User) null, "123");

        mockController.replay();

        issueHistoryManager.addIssueToHistory(null, issue);

        mockController.verify();
    }


    @Test
    public void testAddIssueToHistory()
    {
        issue.getId();
        mockController.setReturnValue(123L);

        historyManager.addItemToHistory(UserHistoryItem.ISSUE, user, "123");

        mockController.replay();

        issueHistoryManager.addIssueToHistory(user, issue);

        mockController.verify();
    }

    @Test
    public void testGetIssueHistoryNoPermission()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        Issue issue2 = new MockIssue(1234L);
        Issue issue3 = new MockIssue(1235L);
        Issue issue4 = new MockIssue(1236L);

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();

        historyManager.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        issueManager.getIssueObject(123L);
        mockController.setReturnValue(null);

        issueManager.getIssueObject(1234L);
        mockController.setReturnValue(issue2);

        permissionManager.hasPermission(Permissions.BROWSE, issue2, user);
        mockController.setReturnValue(false);

        issueManager.getIssueObject(1235L);
        mockController.setReturnValue(issue3);

        permissionManager.hasPermission(Permissions.BROWSE, issue3, user);
        mockController.setReturnValue(false);

        issueManager.getIssueObject(1236L);
        mockController.setReturnValue(issue4);

        permissionManager.hasPermission(Permissions.BROWSE, issue4, user);
        mockController.setReturnValue(false);


        mockController.replay();

        assertFalse(issueHistoryManager.hasIssueHistory(user));

        mockController.verify();

    }

    @Test
    public void testGetIssueHistoryNoPermissionNoUser()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        Issue issue2 = new MockIssue(1234L);
        Issue issue3 = new MockIssue(1235L);
        Issue issue4 = new MockIssue(1236L);

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();

        historyManager.getHistory(UserHistoryItem.ISSUE, (User) null);
        mockController.setReturnValue(history);

        issueManager.getIssueObject(123L);
        mockController.setReturnValue(null);

        issueManager.getIssueObject(1234L);
        mockController.setReturnValue(issue2);

        permissionManager.hasPermission(Permissions.BROWSE, issue2, (User) null);
        mockController.setReturnValue(false);

        issueManager.getIssueObject(1235L);
        mockController.setReturnValue(issue3);

        permissionManager.hasPermission(Permissions.BROWSE, issue3, (User) null);
        mockController.setReturnValue(false);

        issueManager.getIssueObject(1236L);
        mockController.setReturnValue(issue4);

        permissionManager.hasPermission(Permissions.BROWSE, issue4, (User) null);
        mockController.setReturnValue(false);


        mockController.replay();

        assertFalse(issueHistoryManager.hasIssueHistory(null));

        mockController.verify();

    }

    @Test
    public void testGetIssueHistoryHasPermission()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        Issue issue2 = new MockIssue(1234L);

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();

        historyManager.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        issueManager.getIssueObject(123L);
        mockController.setReturnValue(null);

        issueManager.getIssueObject(1234L);
        mockController.setReturnValue(issue2);

        permissionManager.hasPermission(Permissions.BROWSE, issue2, user);
        mockController.setReturnValue(true);

        mockController.replay();

        assertTrue(issueHistoryManager.hasIssueHistory(user));

        mockController.verify();

    }

    @Test
    public void testGetIssueHistoryNoHistory()
    {
        historyManager.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(null);

        mockController.replay();

        assertFalse(issueHistoryManager.hasIssueHistory(user));

        mockController.verify();

    }

    @Test
    public void testGetIssueHistoryEmptyHistory()
    {
        historyManager.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(new ArrayList<UserHistoryItem>());

        mockController.replay();

        assertFalse(issueHistoryManager.hasIssueHistory(user));

        mockController.verify();

    }

    @Test
    public void testFullHistoryNoChecks()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();

        historyManager.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        mockController.replay();

        assertEquals(history, issueHistoryManager.getFullIssueHistoryWithoutPermissionChecks(user));

        mockController.verify();
    }

    @Test
    public void testFullHistoryNoChecksNoHistory()
    {

        historyManager.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(null);

        mockController.replay();

        assertEquals(null, issueHistoryManager.getFullIssueHistoryWithoutPermissionChecks(user));

        mockController.verify();
    }

    @Test
    public void testFullHistoryWithChecks()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        Issue issue2 = new MockIssue(1234L);
        Issue issue3 = new MockIssue(1235L);
        Issue issue4 = new MockIssue(1236L);

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();
        final List<UserHistoryItem> expectedHistory = CollectionBuilder.newBuilder(item2, item4).asMutableList();

        historyManager.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        issueManager.getIssueObject(123L);
        mockController.setReturnValue(null);

        issueManager.getIssueObject(1234L);
        mockController.setReturnValue(issue2);

        permissionManager.hasPermission(Permissions.BROWSE, issue2, user);
        mockController.setReturnValue(true);

        issueManager.getIssueObject(1235L);
        mockController.setReturnValue(issue3);

        permissionManager.hasPermission(Permissions.BROWSE, issue3, user);
        mockController.setReturnValue(false);

        issueManager.getIssueObject(1236L);
        mockController.setReturnValue(issue4);

        permissionManager.hasPermission(Permissions.BROWSE, issue4, user);
        mockController.setReturnValue(true);

        mockController.replay();

        assertEquals(expectedHistory, issueHistoryManager.getFullIssueHistoryWithPermissionChecks(user));

        mockController.verify();
    }

    @Test
    public void testFullHistoryWithChecksNoPerms()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        Issue issue2 = new MockIssue(1234L);
        Issue issue3 = new MockIssue(1235L);
        Issue issue4 = new MockIssue(1236L);

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();
        final List<UserHistoryItem> expectedHistory = CollectionBuilder.<UserHistoryItem>newBuilder().asMutableList();

        historyManager.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        issueManager.getIssueObject(123L);
        mockController.setReturnValue(null);

        issueManager.getIssueObject(1234L);
        mockController.setReturnValue(issue2);

        permissionManager.hasPermission(Permissions.BROWSE, issue2, user);
        mockController.setReturnValue(false);

        issueManager.getIssueObject(1235L);
        mockController.setReturnValue(issue3);

        permissionManager.hasPermission(Permissions.BROWSE, issue3, user);
        mockController.setReturnValue(false);

        issueManager.getIssueObject(1236L);
        mockController.setReturnValue(issue4);

        permissionManager.hasPermission(Permissions.BROWSE, issue4, user);
        mockController.setReturnValue(false);

        mockController.replay();

        assertEquals(expectedHistory, issueHistoryManager.getFullIssueHistoryWithPermissionChecks(user));

        mockController.verify();
    }

    @Test
    public void testFullHistoryWithChecksNoHistory()
    {
        final List<UserHistoryItem> expectedHistory = CollectionBuilder.<UserHistoryItem>newBuilder().asMutableList();

        historyManager.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(null);

        mockController.replay();

        assertEquals(expectedHistory, issueHistoryManager.getFullIssueHistoryWithPermissionChecks(user));

        mockController.verify();
    }

    @Test
    public void testShortHistoryWithChecks()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        Issue issue2 = new MockIssue(1234L);
        Issue issue3 = new MockIssue(1235L);
        Issue issue4 = new MockIssue(1236L);

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();
        final List<Issue> expectedIssues = CollectionBuilder.newBuilder(issue2, issue4).asMutableList();

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS);
        mockController.setReturnValue("2");

        historyManager.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        issueManager.getIssueObject(123L);
        mockController.setReturnValue(null);

        issueManager.getIssueObject(1234L);
        mockController.setReturnValue(issue2);

        permissionManager.hasPermission(Permissions.BROWSE, issue2, user);
        mockController.setReturnValue(true);

        issueManager.getIssueObject(1235L);
        mockController.setReturnValue(issue3);

        permissionManager.hasPermission(Permissions.BROWSE, issue3, user);
        mockController.setReturnValue(false);

        issueManager.getIssueObject(1236L);
        mockController.setReturnValue(issue4);

        permissionManager.hasPermission(Permissions.BROWSE, issue4, user);
        mockController.setReturnValue(true);

        mockController.replay();

        assertEquals(expectedIssues, issueHistoryManager.getShortIssueHistory(user));

        mockController.verify();
    }

    @Test
    public void testShortHistoryWithChecksTooMany()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        Issue issue = new MockIssue(123L);
        Issue issue2 = new MockIssue(1234L);
        Issue issue3 = new MockIssue(1235L);
        Issue issue4 = new MockIssue(1236L);

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();
        final List<Issue> expectedIssues = CollectionBuilder.newBuilder(issue, issue2).asMutableList();

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS);
        mockController.setReturnValue("2");

        historyManager.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        issueManager.getIssueObject(123L);
        mockController.setReturnValue(issue);

        permissionManager.hasPermission(Permissions.BROWSE, issue, user);
        mockController.setReturnValue(true);

        issueManager.getIssueObject(1234L);
        mockController.setReturnValue(issue2);

        permissionManager.hasPermission(Permissions.BROWSE, issue2, user);
        mockController.setReturnValue(true);


        mockController.replay();

        assertEquals(expectedIssues, issueHistoryManager.getShortIssueHistory(user));

        mockController.verify();
    }

    @Test
    public void testShortHistoryWithChecksNotEnough()
    {
        UserHistoryItem item = new UserHistoryItem(UserHistoryItem.ISSUE, "123");
        UserHistoryItem item2 = new UserHistoryItem(UserHistoryItem.ISSUE, "1234");
        UserHistoryItem item3 = new UserHistoryItem(UserHistoryItem.ISSUE, "1235");
        UserHistoryItem item4 = new UserHistoryItem(UserHistoryItem.ISSUE, "1236");

        Issue issue = new MockIssue(123L);
        Issue issue2 = new MockIssue(1234L);
        Issue issue3 = new MockIssue(1235L);
        Issue issue4 = new MockIssue(1236L);

        final List<UserHistoryItem> history = CollectionBuilder.newBuilder(item, item2, item3, item4).asMutableList();
        final List<Issue> expectedIssues = CollectionBuilder.newBuilder(issue, issue2, issue3, issue4).asMutableList();

        applicationProperties.getDefaultBackedString(APKeys.JIRA_MAX_ISSUE_HISTORY_DROPDOWN_ITEMS);
        mockController.setReturnValue("6");

        historyManager.getHistory(UserHistoryItem.ISSUE, user);
        mockController.setReturnValue(history);

        issueManager.getIssueObject(123L);
        mockController.setReturnValue(issue);

        permissionManager.hasPermission(Permissions.BROWSE, issue, user);
        mockController.setReturnValue(true);

        issueManager.getIssueObject(1234L);
        mockController.setReturnValue(issue2);

        permissionManager.hasPermission(Permissions.BROWSE, issue2, user);
        mockController.setReturnValue(true);

        issueManager.getIssueObject(1235L);
        mockController.setReturnValue(issue3);

        permissionManager.hasPermission(Permissions.BROWSE, issue3, user);
        mockController.setReturnValue(true);

        issueManager.getIssueObject(1236L);
        mockController.setReturnValue(issue4);

        permissionManager.hasPermission(Permissions.BROWSE, issue4, user);
        mockController.setReturnValue(true);


        mockController.replay();

        assertEquals(expectedIssues, issueHistoryManager.getShortIssueHistory(user));

        mockController.verify();
    }

}
