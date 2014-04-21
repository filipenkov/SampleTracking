package com.atlassian.jira.issue.util;

import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;

import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;

public class TestAllIssuesIterator extends AbstractTestIssueIterator<DatabaseIssuesIterable>
{
    MockOfBizDelegator delegator = new MockOfBizDelegator(Collections.EMPTY_LIST, Collections.EMPTY_LIST);
    IssueFactory issueFactory = new SimpleMockIssueFactory();

    @Override
    protected DatabaseIssuesIterable getIterable(final List<? extends GenericValue> issuesGVs)
    {
        final DatabaseIssuesIterable databaseIssuesIterator = new DatabaseIssuesIterable(delegator, issueFactory);
        delegator.setGenericValues(issuesGVs);
        return databaseIssuesIterator;
    }
}
