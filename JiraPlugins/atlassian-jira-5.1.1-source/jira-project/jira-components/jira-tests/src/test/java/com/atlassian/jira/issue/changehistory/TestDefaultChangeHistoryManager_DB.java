package com.atlassian.jira.issue.changehistory;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.UserWithAttributes;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.order.SortOrder;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;


public class TestDefaultChangeHistoryManager_DB extends LegacyJiraMockTestCase
{
    OfBizDelegator ofBizDelegator;
    IssueManager issueManager;
    MockController mockController;
    MockUser admin;
    long currentTime = System.currentTimeMillis();
    GenericValue issueGV1, issueGV2, issueGV3;

    protected void setUp() throws Exception
    {
        super.setUp();
        EasyMockAnnotations.initMocks(this);

        ofBizDelegator = ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        issueManager = ComponentManager.getComponentInstanceOfType(IssueManager.class);
        mockController = new MockController();

        admin = new MockUser("admin");

        issueGV1 = createIssue("ABC-1", currentTime - 50000, new ChangeGroup("fred", currentTime), new ChangeGroup("john", currentTime - 10000));
        issueGV2 = createIssue("ABC-2", currentTime - 60000, new ChangeGroup("admin", currentTime - 20000), new Comment("admin", currentTime - 16000));
        issueGV3 = createIssue("ABC-3", currentTime - 70000, new Comment("fred", currentTime - 15000));
    }

    public void testDoFindUserHistory() throws GenericEntityException, SQLException, SearchException
    {
        ComponentLocator mockComponentLocator = mockSearch(issueManager, mockController, admin, issueGV1, issueGV3);
        DefaultChangeHistoryManager changeHistoryManager = new DefaultChangeHistoryManager(issueManager, ofBizDelegator, null, mockComponentLocator);

        EasyMockAnnotations.replayMocks(this);
        Collection<Issue> issues = changeHistoryManager.doFindUserHistory(admin, CollectionBuilder.newBuilder("fred", "john").asList(),
                CollectionBuilder.newBuilder(10010L, 10011L).asList(), 20);

        assertEquals(2, issues.size());
        Iterator<Issue> iterator = issues.iterator();
        assertEquals(issueGV1.getLong("id"), iterator.next().getId());
        assertEquals(issueGV3.getLong("id"), iterator.next().getId());

        mockController.verify();
    }

    public void testDoFindUserHistoryForAllUsers() throws GenericEntityException, SQLException, SearchException
    {
        ComponentLocator mockComponentLocator = mockSearch(issueManager, mockController, admin, issueGV1, issueGV3, issueGV2);
        DefaultChangeHistoryManager changeHistoryManager = new DefaultChangeHistoryManager(issueManager, ofBizDelegator, null, mockComponentLocator);

        EasyMockAnnotations.replayMocks(this);
        Collection<Issue> issues = changeHistoryManager.doFindUserHistory(admin, null, CollectionBuilder.newBuilder(10010L, 10011L).asList(), 20);

        assertEquals(3, issues.size());
        Iterator<Issue> iterator = issues.iterator();
        assertEquals(issueGV1.getLong("id"), iterator.next().getId());
        assertEquals(issueGV3.getLong("id"), iterator.next().getId());
        assertEquals(issueGV2.getLong("id"), iterator.next().getId());

        mockController.verify();
    }

    private static GenericValue createIssue(String key, long createdTime, Change... changes) throws GenericEntityException, SQLException, SearchException
    {
        long updatedTime = createdTime;
        for (Change change : changes)
        {
            updatedTime = Math.max(updatedTime, change.time);
        }

        GenericValue issue = EntityUtils.createValue("Issue", EasyMap.build(
                "key", key,
                "type", 1L,
                "created", new Timestamp(createdTime),
                "updated", new Timestamp(updatedTime),
                "resolution", "resolved",
                "project", 10010L
        ));

        for (Change change : changes)
        {
            change.create(issue.getLong("id"));
        }

        return issue;
    }

    private static ComponentLocator mockSearch(final IssueManager issueManager, MockController mockController, UserWithAttributes admin, GenericValue... issueGVs) throws SearchException
    {
        List<Long> issueIds = Lists.transform(Lists.newArrayList(issueGVs), new Function<GenericValue, Long>()
        {
            @Override
            public Long apply(@Nullable GenericValue issue) {
                return issue.getLong("id");
            }
        });
        List<Issue> issues = Lists.transform(issueIds, new Function<Long, Issue>()
        {
            @Override
            public Issue apply(@Nullable Long issueId) {
                return issueManager.getIssueObject(issueId);
            }
        });

        final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().defaultAnd();
        builder.issue().in(issueIds.toArray(new Long[issueIds.size()])).endWhere().orderBy().createdDate(SortOrder.DESC);
        final Query query = builder.buildQuery();
        final ComponentLocator mockComponentLocator = mockController.getMock(ComponentLocator.class);
        final SearchProvider mockSearchProvider = mockController.getMock(SearchProvider.class);
        mockComponentLocator.getComponentInstanceOfType(SearchProvider.class);
        mockController.setReturnValue(mockSearchProvider);

        mockSearchProvider.search(eq(query), eq(admin), isA(PagerFilter.class));
        mockController.setReturnValue(new SearchResults(issues, PagerFilter.getUnlimitedFilter()));

        mockController.replay();
        return mockComponentLocator;
    }


    static abstract class Change
    {
        final String authorName;
        final long time;

        public Change(String authorName, long time)
        {
            this.authorName = authorName;
            this.time = time;
        }

        abstract void create(long issueId);
    }

    static class ChangeGroup extends Change
    {
        ChangeGroup(String authorName, long time)
        {
            super(authorName, time);
        }

        @Override
        void create(long issueId)
        {
            EntityUtils.createValue("ChangeGroup", EasyMap.build("issue", issueId, "author", authorName, "created", new Timestamp(time)));
        }
    }

    static class Comment extends Change
    {
        Comment(String authorName, long time)
        {
            super(authorName, time);
        }

        @Override
        void create(long issueId)
        {
            EntityUtils.createValue("Action", EasyMap.build("issue", issueId, "author", authorName, "created", new Timestamp(time)));
        }
    }
}
