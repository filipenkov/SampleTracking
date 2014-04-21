package com.atlassian.jira.jql.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.atlassian.jira.matchers.IterableMatchers.hasItems;
import static com.atlassian.jira.matchers.IterableMatchers.iterableWithSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test for {@link JqlIssueSupportImpl}.
 *
 * @since v4.0
 */
public class TestJqlIssueSupportImpl extends MockControllerTestCase
{
    @Test
    public void testGetIssuesEmptyKey() throws Exception
    {
        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);

        assertTrue(keySupport.getIssues(null, null).isEmpty());
        assertTrue(keySupport.getIssues("", null).isEmpty());

        verify();
    }

    @Test
    public void testGetIssuesDoesNotExistKey() throws Exception
    {
        final String key = "key";
        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(key)).andReturn(null);
        expect(issueManager.getIssueObject(key.toUpperCase(Locale.ENGLISH))).andReturn(null);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertTrue(keySupport.getIssues(key, null).isEmpty());

        verify();
    }

    @Test
    public void testGetIssuesDoesNotExistKeyUpper() throws Exception
    {
        final String key = "UPPER";
        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(key)).andReturn(null);
        expect(issueManager.getIssueObject(key.toLowerCase(Locale.ENGLISH))).andReturn(null);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertTrue(keySupport.getIssues(key, null).isEmpty());

        verify();
    }

    @Test
    public void testGetIssuesDoesNotExistKeySharp() throws Exception
    {
        final String key = "Stra\u00dfe";
        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(key)).andReturn(null);
        expect(issueManager.getIssueObject(key.toUpperCase(Locale.ENGLISH))).andReturn(null);
        expect(issueManager.getIssueObject(key.toLowerCase(Locale.ENGLISH))).andReturn(null);
        expect(issueManager.getIssueObject(key.toUpperCase(Locale.ENGLISH).toLowerCase(Locale.ENGLISH))).andReturn(null);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertTrue(keySupport.getIssues(key, null).isEmpty());

        verify();
    }

    @Test
    public void testGetIssuesDoesNotExistKeySharpLower() throws Exception
    {
        final String key = "stra\u00dfe";
        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(key)).andReturn(null);
        expect(issueManager.getIssueObject(key.toUpperCase(Locale.ENGLISH))).andReturn(null);
        expect(issueManager.getIssueObject(key.toUpperCase(Locale.ENGLISH).toLowerCase(Locale.ENGLISH))).andReturn(null);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertTrue(keySupport.getIssues(key, null).isEmpty());

        verify();
    }

    @Test
    public void testGetIssuesNoPermissionsKey() throws Exception
    {
        final String key = "key";
        final MockIssue issue = new MockIssue(89);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(key)).andReturn(issue);
        expect(issueManager.getIssueObject(key.toUpperCase(Locale.ENGLISH))).andReturn(issue);

        final PermissionManager permissionManager = mockController.getNiceMock(PermissionManager.class);
        expect(permissionManager.hasPermission(Permissions.BROWSE, issue, (User) null)).andReturn(false).times(2);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertTrue(keySupport.getIssues(key, null).isEmpty());

        verify();
    }

    @Test
    public void testGetIssuesSkipCheck() throws Exception
    {
        final String key = "key";
        final MockIssue issue = new MockIssue(89);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(key)).andReturn(issue);
        expect(issueManager.getIssueObject(key.toUpperCase(Locale.ENGLISH))).andReturn(issue);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertEquals(Collections.<Issue>singletonList(issue), keySupport.getIssues(key));

        verify();
    }

    @Test
    public void testGetIssuesHappyPathKey() throws Exception
    {
        final User user = new MockUser("test");
        final String key = "key";
        final MockIssue issue = new MockIssue(89);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(key)).andReturn(issue);
        expect(issueManager.getIssueObject(key.toUpperCase(Locale.ENGLISH))).andReturn(issue);

        final PermissionManager permissionManager = mockController.getNiceMock(PermissionManager.class);
        expect(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).andReturn(true);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertEquals(Collections.<Issue>singletonList(issue), keySupport.getIssues(key, user));

        verify();
    }

    @Test
    public void testGetIssuesHappyPathKeyUpper() throws Exception
    {
        final User user = new MockUser("test");
        final String key = "key";
        final MockIssue issue = new MockIssue(89);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(key)).andReturn(null);
        expect(issueManager.getIssueObject(key.toUpperCase(Locale.ENGLISH))).andReturn(issue);

        final PermissionManager permissionManager = mockController.getNiceMock(PermissionManager.class);
        expect(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).andReturn(true);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertEquals(Collections.<Issue>singletonList(issue), keySupport.getIssues(key, user));

        verify();
    }

    @Test
    public void testGetIssuesHappyPathKeySharp() throws Exception
    {
        final String key = "stra\u00dfe";
        final MockIssue issue = new MockIssue(89);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(key)).andReturn(null);
        expect(issueManager.getIssueObject(key.toUpperCase(Locale.ENGLISH))).andReturn(null);
        expect(issueManager.getIssueObject(key.toUpperCase(Locale.ENGLISH).toLowerCase(Locale.ENGLISH))).andReturn(issue);

        final PermissionManager permissionManager = mockController.getNiceMock(PermissionManager.class);
        expect(permissionManager.hasPermission(Permissions.BROWSE, issue, (User) null)).andReturn(true);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertEquals(Collections.<Issue>singletonList(issue), keySupport.getIssues(key, null));

        verify();
    }

    @Test
    public void testGetIssuesHappyPathKeyLower() throws Exception
    {
        final User user = new MockUser("test");
        final String key = "Key";
        final MockIssue issue = new MockIssue(89);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(key)).andReturn(null);
        expect(issueManager.getIssueObject(key.toUpperCase(Locale.ENGLISH))).andReturn(null);
        expect(issueManager.getIssueObject(key.toLowerCase(Locale.ENGLISH))).andReturn(issue);

        final PermissionManager permissionManager = mockController.getNiceMock(PermissionManager.class);
        expect(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).andReturn(true);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertEquals(Collections.<Issue>singletonList(issue), keySupport.getIssues(key, user));

        verify();
    }

    @Test
    public void testGetIssuesHappyPathMultiple() throws Exception
    {
        final String key = "Stra\u00dfe";
        final User user = new MockUser("test");
        final MockIssue issue = new MockIssue(89);
        final MockIssue issue1 = new MockIssue(92);
        final MockIssue issue2 = new MockIssue(90);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(key)).andReturn(issue);
        expect(issueManager.getIssueObject(key.toUpperCase(Locale.ENGLISH))).andReturn(issue);
        expect(issueManager.getIssueObject(key.toLowerCase(Locale.ENGLISH))).andReturn(issue2);
        expect(issueManager.getIssueObject(key.toUpperCase(Locale.ENGLISH).toLowerCase(Locale.ENGLISH))).andReturn(issue1);

        final PermissionManager permissionManager = mockController.getNiceMock(PermissionManager.class);
        expect(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).andReturn(true);
        expect(permissionManager.hasPermission(Permissions.BROWSE, issue2, user)).andReturn(true);
        expect(permissionManager.hasPermission(Permissions.BROWSE, issue1, user)).andReturn(false);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        final List<Issue> actualIssues = keySupport.getIssues(key, user);

        assertThat(actualIssues, iterableWithSize(2, Issue.class));
        assertThat(actualIssues, hasItems(Issue.class, issue, issue2));

        verify();
    }

    @Test
    public void testGetIssueDoesNotExistId() throws Exception
    {
        final long id = 10;
        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(id)).andReturn(null);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertNull(keySupport.getIssue(id, null));

        verify();
    }

    @Test
    public void testGetIssueNoPermissionsId() throws Exception
    {
        final long id = 11;
        final MockIssue issue = new MockIssue(id);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(id)).andReturn(issue);

        final PermissionManager permissionManager = mockController.getNiceMock(PermissionManager.class);
        expect(permissionManager.hasPermission(Permissions.BROWSE, issue, (User) null)).andReturn(false);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertNull(keySupport.getIssue(id, null));

        verify();
    }

    @Test
    public void testGetIssueSkipCheck() throws Exception
    {
        final long id = 12;
        final MockIssue issue = new MockIssue(id);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(id)).andReturn(issue);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertSame(issue, keySupport.getIssue(id));

        verify();
    }

    @Test
    public void testGetIssueHappyPathId() throws Exception
    {
        final User user = new MockUser("test");
        final long id = 12;
        final MockIssue issue = new MockIssue(id);

        final IssueManager issueManager = mockController.getNiceMock(IssueManager.class);
        expect(issueManager.getIssueObject(id)).andReturn(issue);

        final PermissionManager permissionManager = mockController.getNiceMock(PermissionManager.class);
        expect(permissionManager.hasPermission(Permissions.BROWSE, issue, user)).andReturn(true);

        final JqlIssueSupportImpl keySupport = mockController.instantiate(JqlIssueSupportImpl.class);
        assertSame(issue, keySupport.getIssue(id, user));

        verify();
    }
}
