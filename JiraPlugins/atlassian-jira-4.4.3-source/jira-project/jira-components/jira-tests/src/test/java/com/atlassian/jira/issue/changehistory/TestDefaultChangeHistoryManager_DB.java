package com.atlassian.jira.issue.changehistory;

import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.MockProviderAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.local.MockControllerTestCase;
import com.atlassian.jira.mock.controller.MockController;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.order.SortOrder;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.isA;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;


public class TestDefaultChangeHistoryManager_DB extends LegacyJiraMockTestCase
{

    public void testDoFindUserHistory() throws GenericEntityException, SQLException, SearchException
    {
        final MockProviderAccessor mockProviderAccessor = new MockProviderAccessor("Administrator", "admin@example.com");
        final MockUser admin = new MockUser("admin");

        final long currentTime = System.currentTimeMillis();

        final GenericValue issueGV = EntityUtils.createValue("Issue", EasyMap.build("key", "ABC-1", "type", 1L,
                "created", new Timestamp(currentTime - 50000), "resolution", "resolved", "project", 10010L));
        final GenericValue issueGV2 = EntityUtils.createValue("Issue", EasyMap.build("key", "ABC-2", "type", 1L,
                "created", new Timestamp(currentTime - 60000), "resolution", "resolved", "project", 10010L));
        final GenericValue issueGV3 = EntityUtils.createValue("Issue", EasyMap.build("key", "ABC-3", "type", 1L,
                "created", new Timestamp(currentTime - 70000), "resolution", "resolved", "project", 10010L));

        final GenericValue changeGroupGV = EntityUtils.createValue("ChangeGroup", EasyMap.build("issue", issueGV.getLong("id"), "author", "fred",
                "created", new Timestamp(currentTime)));
        //10 secs ago
        final GenericValue changeGroup1GV = EntityUtils.createValue("ChangeGroup", EasyMap.build("issue", issueGV.getLong("id"), "author", "john",
                "created", new Timestamp(currentTime - 10000)));
        //a changegroup for admin.  This issue shouldn't show up in the results
        final GenericValue changeGroup2GV = EntityUtils.createValue("ChangeGroup", EasyMap.build("issue", issueGV2.getLong("id"), "author", "admin",
                "created", new Timestamp(currentTime - 20000)));

        GenericValue commentGV = EntityUtils.createValue("Action", EasyMap.build("issue", issueGV3.getLong("id"), "author", "fred", "created", new Timestamp(currentTime - 15000)));
        GenericValue commentGV2 = EntityUtils.createValue("Action", EasyMap.build("issue", issueGV2.getLong("id"), "author", "admin", "created", new Timestamp(currentTime - 16000)));

        final OfBizDelegator ofBizDelegator = ComponentManager.getComponentInstanceOfType(OfBizDelegator.class);
        final IssueManager issueManager = ComponentManager.getComponentInstanceOfType(IssueManager.class);
        final MockController mockController = new MockController();
        final SearchProvider mockSearchProvider = mockController.getMock(SearchProvider.class);

        MutableIssue issue = issueManager.getIssueObject(issueGV.getLong("id"));
        MutableIssue issue3 = issueManager.getIssueObject(issueGV3.getLong("id"));

        final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().defaultAnd();
        builder.issue().in(issueGV.getLong("id"), issueGV3.getLong("id")).endWhere().orderBy().createdDate(SortOrder.DESC);
        final Query query = builder.buildQuery();
        final ComponentLocator mockComponentLocator = mockController.getMock(ComponentLocator.class);
        mockComponentLocator.getComponentInstanceOfType(SearchProvider.class);
        mockController.setReturnValue(mockSearchProvider);

        mockSearchProvider.search(eq(query), eq(admin), isA(PagerFilter.class));
        final SearchResults results = new SearchResults(CollectionBuilder.<Issue>newBuilder(issue, issue3).asList(), PagerFilter.getUnlimitedFilter());
        mockController.setReturnValue(results);

        mockController.replay();
        DefaultChangeHistoryManager changeHistoryManager = new DefaultChangeHistoryManager(issueManager, ofBizDelegator, null, mockComponentLocator);
        Collection<Issue> issues = changeHistoryManager.doFindUserHistory(admin, CollectionBuilder.newBuilder("fred", "john").asList(), CollectionBuilder.newBuilder(10010L, 10011L).asList(), 20);

        assertEquals(2, issues.size());
        Iterator<Issue> iterator = issues.iterator();
        assertEquals(issueGV.getLong("id"), iterator.next().getId());
        assertEquals(issueGV3.getLong("id"), iterator.next().getId());

        mockController.verify();
    }
}