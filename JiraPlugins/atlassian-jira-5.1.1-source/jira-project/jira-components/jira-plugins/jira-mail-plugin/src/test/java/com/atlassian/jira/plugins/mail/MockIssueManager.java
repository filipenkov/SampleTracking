/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.mock.issue.MockIssue;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class MockIssueManager implements IssueManager
{
    private final Map<Long, Issue> issues = Maps.newHashMap();
    private boolean editable;

    public GenericValue getIssue(Long id) throws DataAccessException
    {
        Issue issue = getIssueObject(id);
        if (issue == null)
        {
            return null;
        }
        else
        {
            return issue.getGenericValue();
        }
    }

    public GenericValue getIssue(final String key) throws GenericEntityException
    {
        for (Issue issue : issues.values())
        {
            if (StringUtils.equals(key, issue.getKey()))
            {
                return issue.getGenericValue();
            }
        }
        return null;
    }

    public List<GenericValue> getIssues(final Collection ids)
    {
        return Lists.newArrayList(Iterables.transform(Iterables.filter(issues.values(), new Predicate<Issue>()
        {
            @Override
            public boolean apply(Issue input)
            {
                return ids.contains(input.getId());
            }
        }), new Function<Issue, GenericValue>()
        {
            @Override
            public GenericValue apply(Issue from)
            {
                return from.getGenericValue();
            }
        }));
    }

    public Collection<Long> getIssueIdsForProject(Long projectId) throws GenericEntityException
    {
        List<Long> ids = Lists.newArrayList();
        for (Issue issue : issues.values())
        {
            if (projectId.equals(issue.getProjectObject().getId()))
            {
                ids.add(issue.getId());
            }
        }
        return ids;
    }

    public GenericValue getIssueByWorkflow(Long wfid) throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    public MutableIssue getIssueObjectByWorkflow(Long workflowId) throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    private MutableIssue getIssueObject(final GenericValue issueGV)
    {
        final MockIssue mockIssue = new MockIssue();
        mockIssue.setGenericValue(issueGV);
        mockIssue.setUpdated(issueGV.getTimestamp("updated"));
        return mockIssue;
    }

    public MutableIssue getIssueObject(Long id) throws DataAccessException
    {
        Issue issue = issues.get(id);
        // By contract we must return a new instance each time.
        return issue == null ? null : getIssueObject(issue.getGenericValue());
    }

    public MutableIssue getIssueObject(String key) throws DataAccessException
    {
        try
        {
            // By contract we must return a new instance each time.
            return getIssue(key) == null ? null : getIssueObject(getIssue(key));
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Issue> getIssueObjects(Collection<Long> ids)
    {
        throw new UnsupportedOperationException();
    }

    public List<GenericValue> getEntitiesByIssue(String relationName, GenericValue issue) throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    public List<GenericValue> getEntitiesByIssueObject(String relationName, Issue issue) throws GenericEntityException
    {
        return Lists.newArrayList();
    }

    public List<GenericValue> getIssuesByEntity(String relationName, GenericValue entity) throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Issue> getIssueObjectsByEntity(String relationName, GenericValue entity)
            throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Issue createIssueObject(String remoteUserName, Map<String, Object> fields) throws CreateException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Issue createIssueObject(User remoteUser, Map<String, Object> fields) throws CreateException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public GenericValue createIssue(String remoteUserName, Map fields)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public GenericValue createIssue(User remoteUser, Map fields) throws CreateException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public GenericValue createIssue(User remoteUser, Issue issue) throws CreateException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Issue createIssueObject(User remoteUser, Issue issue) throws CreateException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Issue updateIssue(User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteIssue(User user, Issue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteIssue(User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteIssueNoEvent(Issue issue) throws RemoveException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteIssueNoEvent(MutableIssue issue) throws RemoveException
    {
        throw new UnsupportedOperationException();
    }

    public List<GenericValue> getProjectIssues(GenericValue project) throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    public boolean isEditable(Issue issue)
    {
        return editable;
    }

    public void setEditable(boolean editable)
    {
        this.editable = editable;
    }

    @Override
    public boolean isEditable(final Issue issue, final User user)
    {
        return editable;
    }

    public long getIssueCountForProject(Long projectId)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public boolean hasUnassignedIssues()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public long getUnassignedIssueCount()
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public List<Issue> getVotedIssues(User user) throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Issue> getVotedIssuesOverrideSecurity(final User user) throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<User> getWatchers(Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Issue> getWatchedIssues(User user)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Issue> getWatchedIssuesOverrideSecurity(final User user)
    {
        throw new UnsupportedOperationException();
    }

    public void addIssue(GenericValue issueGV)
    {
        MockIssue issue = new MockIssue();
        issue.setGenericValue(issueGV);
        issue.setUpdated(issueGV.getTimestamp(IssueFieldConstants.UPDATED));

        addIssue(issue);
    }

    public void addIssue(MutableIssue issue)
    {
        issues.put(issue.getId(), issue);
    }

    public void addVersion(GenericValue version) throws GenericEntityException
    {
//        versions.put(version.getLong("id"), version);
//
//        GenericValue issue = getIssue(version.getLong("issue"));
//        // TODO: What is this trying to do?
//        getEntitiesByIssue(IssueRelationConstants.VERSION, issue).add(version);
        throw new UnsupportedOperationException();
    }

    public List execute(SearchRequest searchRequest, User searcher) throws SearchException
    {
        throw new UnsupportedOperationException();
    }

    public void refresh()
    {
    }

    public void refresh(GenericValue issue)
    {
    }

    public long getCacheHitsCount()
    {
        return 0;
    }

    public long getCacheMissCount()
    {
        return 0;
    }

    public void resetCacheStats()
    {
    }

    public long getCacheMaxSize()
    {
        return 0;
    }

    public void setCacheMaxSize(long maxSize)
    {
    }

    public long getCacheSize()
    {
        return 0;
    }

    public void refreshParents(String associationName, GenericValue child) throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }
}
