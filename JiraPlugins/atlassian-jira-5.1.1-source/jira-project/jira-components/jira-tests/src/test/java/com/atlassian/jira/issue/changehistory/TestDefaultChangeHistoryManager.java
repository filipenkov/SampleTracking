package com.atlassian.jira.issue.changehistory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MockIssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.easymock.EasyMock;
import org.junit.Test;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.easymock.EasyMockMatcherUtils.any;
import static com.atlassian.jira.easymock.EasyMockMatcherUtils.anyList;
import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class TestDefaultChangeHistoryManager extends MockControllerTestCase
{
    @Test
    public void testGetPreviousIssueKeysWithInvalidOriginalKey()
    {
        final IssueManager mockIssueManager = createMock(IssueManager.class);
        expect(mockIssueManager.getIssueObject(anyLong())).andStubReturn(null);
        expect(mockIssueManager.getIssueObject(anyString())).andStubReturn(null);

        final DefaultChangeHistoryManager changeHistoryManager = instantiate(DefaultChangeHistoryManager.class);

        try
        {
            changeHistoryManager.getPreviousIssueKeys((Long) null);
            fail("Null's not allowed!");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("issueId should not be null!", e.getMessage());
        }

        try
        {
            changeHistoryManager.getPreviousIssueKeys((String) null);
            fail("Null's not allowed!");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("issueKey should not be null!", e.getMessage());
        }

        Collection previousKeys = changeHistoryManager.getPreviousIssueKeys("");
        assertTrue(previousKeys.isEmpty());

        previousKeys = changeHistoryManager.getPreviousIssueKeys("INVALIDKEY");
        assertTrue(previousKeys.isEmpty());
    }

    @Test
    public void testGetPreviousIssueKeys()
    {
        final IssueManager mockIssueManager = createMock(IssueManager.class);

        expect(mockIssueManager.getIssueObject("HSP-25")).andStubReturn(MockIssueFactory.createIssue(10023L));

        final OfBizDelegator mockOfBizDelegator = createMock(OfBizDelegator.class);

        expect(
                mockOfBizDelegator.findByCondition
                        (
                                eq("ChangeGroupChangeItemView"),
                                any(EntityCondition.class),
                                eq(ImmutableList.of("group", "oldstring", "newstring")),
                                eq(ImmutableList.of("group desc"))
                        )
        ).andStubReturn(getMockChangeItemGVs());

        final DefaultChangeHistoryManager changeHistoryManager = instantiate(DefaultChangeHistoryManager.class);

        final Collection<String> previousKeys = changeHistoryManager.getPreviousIssueKeys("HSP-25");

        assertFalse(previousKeys.isEmpty());
        assertEquals(3, previousKeys.size());
        assertTrue(previousKeys.containsAll(ImmutableList.of("MKY-12", "STUFF-23", "BLAH-2")));
    }

    @Test
    public void testGetPreviousIssueKeysWithId()
    {
        final OfBizDelegator mockOfBizDelegator = createMock(OfBizDelegator.class);

        expect(
                mockOfBizDelegator.findByCondition
                        (
                                eq("ChangeGroupChangeItemView"),
                                any(EntityCondition.class),
                                eq(ImmutableList.of("group", "oldstring", "newstring")),
                                eq(ImmutableList.of("group desc"))
                        )
        ).andStubReturn(getMockChangeItemGVs());

        final DefaultChangeHistoryManager changeHistoryManager = instantiate(DefaultChangeHistoryManager.class);

        final Collection<String> previousKeys = changeHistoryManager.getPreviousIssueKeys(10023L);

        assertFalse(previousKeys.isEmpty());
        assertEquals(3, previousKeys.size());
        assertTrue(previousKeys.containsAll(ImmutableList.of("MKY-12", "STUFF-23", "BLAH-2")));
    }

    @Test
    public void testGetChangeItemsForField()
    {
        final MutableIssue issue = MockIssueFactory.createIssue(10000);

        final OfBizDelegator mockDelegator = createMock(OfBizDelegator.class);
        expect
                (
                        mockDelegator.findByAnd(
                                "ChangeGroupChangeItemView",
                                ImmutableMap.of("issue", 10000L, "field", "somefield"),
                                ImmutableList.of("created ASC", "changeitemid ASC"))
                ).
                andStubReturn(ImmutableList.of(getMockChangeItemGV(1), getMockChangeItemGV(2), getMockChangeItemGV(3)));

        final DefaultChangeHistoryManager defaultChangeHistoryManager = instantiate(DefaultChangeHistoryManager.class);

        final List<ChangeItemBean> changeItems = defaultChangeHistoryManager.getChangeItemsForField(issue, "somefield");

        assertEquals(3, changeItems.size());

        assertEquals("jira", changeItems.get(0).getFieldType());
        assertEquals("resolution", changeItems.get(0).getField());
        assertEquals("1", changeItems.get(0).getFrom());
        assertEquals("Open", changeItems.get(0).getFromString());
        assertEquals("5", changeItems.get(0).getTo());
        assertEquals("Resolved", changeItems.get(0).getToString());
        assertEquals(new Timestamp(1), changeItems.get(0).getCreated());

        assertEquals("jira", changeItems.get(1).getFieldType());
        assertEquals("resolution", changeItems.get(1).getField());
        assertEquals("1", changeItems.get(1).getFrom());
        assertEquals("Open", changeItems.get(1).getFromString());
        assertEquals("5", changeItems.get(1).getTo());
        assertEquals("Resolved", changeItems.get(1).getToString());
        assertEquals(new Timestamp(2), changeItems.get(1).getCreated());

        assertEquals("jira", changeItems.get(2).getFieldType());
        assertEquals("resolution", changeItems.get(2).getField());
        assertEquals("1", changeItems.get(2).getFrom());
        assertEquals("Open", changeItems.get(2).getFromString());
        assertEquals("5", changeItems.get(2).getTo());
        assertEquals("Resolved", changeItems.get(2).getToString());
        assertEquals(new Timestamp(3), changeItems.get(2).getCreated());
    }

    @Test
    public void testGetChangeItemsForFieldNoItems()
    {
        final MutableIssue issue = MockIssueFactory.createIssue(10000);

        final OfBizDelegator mockDelegator = createMock(OfBizDelegator.class);
        expect
                (
                        mockDelegator.findByAnd(
                                eq("ChangeGroupChangeItemView"),
                                eq(ImmutableMap.of("issue", 10000L, "field", "somefield")),
                                anyList(String.class))
                ).
                andStubReturn(Collections.<GenericValue>emptyList());

        final DefaultChangeHistoryManager defaultChangeHistoryManager = mockController.instantiate(DefaultChangeHistoryManager.class);

        final List<ChangeItemBean> changeItems = defaultChangeHistoryManager.getChangeItemsForField(issue, "somefield");
        assertEquals(0, changeItems.size());
    }

    @Test
    public void testFindUserHistory()
    {
        final User admin = new MockUser("admin");

        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);
        mockPermissionManager.getProjectObjects(Permissions.BROWSE, admin);
        mockController.setReturnValue(ImmutableList.of(new MockProject(10000L, "MKY"), new MockProject(10001L, "HSP")));
        mockController.replay();

        DefaultChangeHistoryManager changeHistoryManager = new DefaultChangeHistoryManager(null, null, mockPermissionManager, null)
        {
            @Override
            Collection<Issue> doFindUserHistory(final User remoteUser, final Collection<String> usernames, final Collection<Long> projects, final int maxResults)
            {
                assertEquals(admin, remoteUser);
                assertEquals(ImmutableList.of(10000L, 10001L), projects);
                return Collections.emptyList();
            }
        };
        Collection<Issue> issues = changeHistoryManager.findUserHistory(admin, ImmutableList.of("fred", "john"), 10);
        assertEquals(0, issues.size());
    }

    @Test
    public void testFindUserHistoryWithProjects()
    {
        final User admin = new MockUser("admin");

        final Project monkey = new MockProject(10000L, "MKY");
        final Project homosapien = new MockProject(10001L, "HSP");

        final PermissionManager mockPermissionManager = mockController.getMock(PermissionManager.class);
        mockPermissionManager.hasPermission(Permissions.BROWSE, monkey, admin);
        mockController.setReturnValue(true);
        mockPermissionManager.hasPermission(Permissions.BROWSE, homosapien, admin);
        mockController.setReturnValue(false);
        mockController.replay();

        final DefaultChangeHistoryManager changeHistoryManager = new DefaultChangeHistoryManager(null, null, mockPermissionManager, null)
        {
            @Override
            Collection<Issue> doFindUserHistory(final User remoteUser, final Collection<String> usernames, final Collection<Long> projects, final int maxResults)
            {
                assertEquals(admin, remoteUser);
                assertEquals(ImmutableList.of(10000L), projects);
                return Collections.emptyList();
            }
        };
        Collection<Issue> issues = changeHistoryManager.findUserHistory(admin, ImmutableList.of("fred", "john"), ImmutableList.of(monkey, homosapien), 10);
        assertEquals(0, issues.size());
    }

    @Test
    public void testDoFindUserHistoryNoProjects()
    {
        final User admin = new MockUser("admin");

        final DefaultChangeHistoryManager changeHistoryManager = new DefaultChangeHistoryManager(null, null, null, null);
        Collection<Issue> issues = changeHistoryManager.doFindUserHistory(admin, ImmutableList.of("fred", "john"), Collections.<Long>emptyList(), 20);
        assertEquals(0, issues.size());
    }

    @Test
    public void testDeleteAllChangeHistoryItems()
    {
        final Issue issue = EasyMock.createMock(Issue.class);
        expect(issue.getId()).andReturn(10L);
        OfBizDelegator mockDelegator = EasyMock.createMock(OfBizDelegator.class);
        expect(mockDelegator.findByAnd(eq("ChangeGroup"), eq(ImmutableMap.of("issue", 10L)))).andReturn(groupList(1L, 5L, 10L));
        // ae change items for returned groups must be removed
        expect(mockDelegator.removeByAnd(eq("ChangeItem"), eq(ImmutableMap.of("group", 1L)))).andReturn(1);
        expect(mockDelegator.removeByAnd(eq("ChangeItem"), eq(ImmutableMap.of("group", 5L)))).andReturn(1);
        expect(mockDelegator.removeByAnd(eq("ChangeItem"), eq(ImmutableMap.of("group", 10L)))).andReturn(1);
        // groups themselves must be removed
        expect(mockDelegator.removeByAnd(eq("ChangeGroup"), eq(ImmutableMap.of("issue", 10L)))).andReturn(3);
    }

    private List<GenericValue> groupList(Long... ids)
    {
        return transform(asList(ids), new Function<Long, GenericValue>()
        {
            @Override
            public GenericValue apply(@Nullable Long from)
            {
                return new MockGenericValue("ChangeGroup", from);
            }
        });
    }

    private GenericValue getMockChangeItemGV(int timestamp)
    {
        return new MockGenericValue
                (
                        "ChangeGroupChangeItemView",
                        ImmutableMap.<String, Object>builder()
                                .put("fieldtype", "jira")
                                .put("field", "resolution")
                                .put("oldvalue", "1")
                                .put("oldstring", "Open")
                                .put("newvalue", "5")
                                .put("newstring", "Resolved")
                                .put("created", new Timestamp(timestamp))
                                .build()
                );
    }

    private List<GenericValue> getMockChangeItemGVs()
    {
        return ImmutableList.<GenericValue>of(
                new MockGenericValue("ChangeGroupChangeItemView",
                        ImmutableMap.<String, Object>of("group", 5L, "newstring", "HSP-25", "oldstring", "MKY-12")),
                new MockGenericValue("ChangeGroupChangeItemView",
                        ImmutableMap.<String, Object>of("group", 4L, "newstring", "MKY-12", "oldstring", "STUFF-23")),
                new MockGenericValue("ChangeGroupChangeItemView",
                        ImmutableMap.<String, Object>of("group", 2L, "newstring", "STUFF-23", "oldstring", "BLAH-2")),
                new MockGenericValue("ChangeGroupChangeItemView",
                        ImmutableMap.<String, Object>of("group", 1L, "newstring", "BLAH-2", "oldstring", ""))
        );
    }
}
