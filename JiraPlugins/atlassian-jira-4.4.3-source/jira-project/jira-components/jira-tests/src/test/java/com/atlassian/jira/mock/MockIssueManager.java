/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.exception.UpdateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.MockIssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MockIssueManager implements IssueManager
{
    Map issues;
    Map versions;
    Map issueVersions;
    boolean editable;

    public MockIssueManager()
    {
        this.issues = new HashMap();
        this.versions = new HashMap();
        this.issueVersions = new HashMap();
    }

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

    public GenericValue getIssue(String key) throws GenericEntityException
    {
        for (Iterator iterator = issues.values().iterator(); iterator.hasNext();)
        {
            Issue issue = (Issue) iterator.next();
            if (key.equals(issue.getKey()))
            {
                return issue.getGenericValue();
            }
        }
        return null;
    }

    public List getIssues(Collection ids)
    {
        List issues = new ArrayList();
        for (Iterator iterator = ids.iterator(); iterator.hasNext();)
        {
            Long id = (Long) iterator.next();
            issues.add(getIssue(id));


        }
        return issues;
    }

    public GenericValue getIssueByWorkflow(Long wfid) throws GenericEntityException
    {
        List values = CoreFactory.getGenericDelegator().findByAnd("Issue", EasyMap.build("workflowId", wfid));
        return EntityUtil.getOnly(values);
    }

    public MutableIssue getIssueObjectByWorkflow(Long workflowId) throws GenericEntityException
    {
        return getIssueObject(getIssueByWorkflow(workflowId));
    }

    private MutableIssue getIssueObject(final GenericValue issueGV)
    {
        return new IssueImpl(issueGV, this, MockIssueFactory.getProjectManager(),
                MockIssueFactory.getVersionManager(), MockIssueFactory.getIssueSecurityLevelManager(),
                MockIssueFactory.getConstantsManager(), MockIssueFactory.getSubTaskManager(),
                MockIssueFactory.getAttachmentManager(), MockIssueFactory.getLabelManager(),
                MockIssueFactory.getProjectComponentManager(), MockIssueFactory.getUserManager());
    }

    public MutableIssue getIssueObject(Long id) throws DataAccessException
    {
        Issue issue = (Issue) issues.get(id);
        if (issue == null)
        {
            return null;
        }
        // By contract we must return a new instance each time.
        return getIssueObject(issue.getGenericValue());
    }

    public MutableIssue getIssueObject(String key) throws DataAccessException
    {
        Issue issue = (Issue) issues.get(key);
        if (issue == null)
        {
            return null;
        }
        // By contract we must return a new instance each time.
        return new IssueImpl(issue.getGenericValue(), this, MockIssueFactory.getProjectManager(),
                MockIssueFactory.getVersionManager(), MockIssueFactory.getIssueSecurityLevelManager(),
                MockIssueFactory.getConstantsManager(), MockIssueFactory.getSubTaskManager(),
                MockIssueFactory.getAttachmentManager(), MockIssueFactory.getLabelManager(),
                MockIssueFactory.getProjectComponentManager(), MockIssueFactory.getUserManager());
    }

    @Override
    public List<Issue> getVotedIssues(com.opensymphony.user.User user) throws GenericEntityException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public List getEntitiesByIssue(String relationName, GenericValue issue) throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    public List getEntitiesByIssueObject(String relationName, Issue issue) throws GenericEntityException
    {
        return new ArrayList();
    }

    public List getIssuesByEntity(String relationName, GenericValue entity) throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public GenericValue createIssue(com.opensymphony.user.User remoteUser, Map<String, Object> fields)
            throws CreateException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public GenericValue createIssue(com.opensymphony.user.User remoteUser, Issue issue) throws CreateException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public GenericValue createIssue(String remoteUserName, Map fields)
    {
        throw new UnsupportedOperationException();
    }

    public GenericValue createIssue(User remoteUser, Map fields) throws CreateException
    {
        throw new UnsupportedOperationException();
    }

    public GenericValue createIssue(User remoteUser, Issue issue) throws CreateException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Issue updateIssue(com.opensymphony.user.User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws UpdateException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public Issue updateIssue(User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteIssue(com.opensymphony.user.User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void deleteIssue(User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException
    {
        throw new UnsupportedOperationException();
    }

    public List getProjectIssues(GenericValue project) throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    public boolean isEditable(Issue issue)
    {
        return editable;
    }

    @Override
    public boolean isEditable(Issue issue, com.opensymphony.user.User user)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void setEditable(boolean editable)
    {
        this.editable = editable;
    }

    public boolean isEditable(final Issue issue, final User user)
    {
        return editable;
    }

    public Collection getIssueIdsForProject(Long projectId) throws GenericEntityException
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public long getIssueCountForProject(Long projectId)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public List getVotedIssues(User user) throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Issue> getVotedIssuesOverrideSecurity(com.opensymphony.user.User user) throws GenericEntityException
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public List<Issue> getVotedIssuesOverrideSecurity(final User user) throws GenericEntityException
    {
        throw new UnsupportedOperationException();
    }

    public List<com.opensymphony.user.User> getIssueWatchers(GenericValue issue)
    {
        throw new UnsupportedOperationException();
    }

    public List<com.opensymphony.user.User> getIssueWatchers(Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<User> getWatchers(Issue issue)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Issue> getWatchedIssues(com.opensymphony.user.User user)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public List<Issue> getWatchedIssues(User user)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<Issue> getWatchedIssuesOverrideSecurity(com.opensymphony.user.User user)
    {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public List<Issue> getWatchedIssuesOverrideSecurity(final User user)
    {
        throw new UnsupportedOperationException();
    }

    public void addIssue(GenericValue issueGV)
    {
        addIssue(new IssueImpl(issueGV, this, null, null, null, null, null, null, null, null, null));
    }

    public void addIssue(MutableIssue issue)
    {
        issues.put(issue.getId(), issue);
    }

    public void addVersion(GenericValue version) throws GenericEntityException
    {
        versions.put(version.getLong("id"), version);

        GenericValue issue = getIssue(version.getLong("issue"));
        // TODO: What is this trying to do?
        getEntitiesByIssue(IssueRelationConstants.VERSION, issue).add(version);
    }

    /**
     * Takes a search request object and returns a list of issues that match the search request
     *
     * @param searchRequest object to be used to search for
     * @return A List of Issues that match the search request
     * @throws com.atlassian.jira.issue.search.SearchException
     *
     */
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
