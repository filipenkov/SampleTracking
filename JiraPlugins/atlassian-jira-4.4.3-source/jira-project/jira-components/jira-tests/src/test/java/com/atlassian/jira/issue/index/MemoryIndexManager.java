/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.index;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.IndexingConfiguration;
import com.atlassian.jira.config.util.MockIndexPathManager;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.exception.UpdateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.searchers.MockSearcherFactory;
import org.apache.lucene.store.Directory;
import org.easymock.EasyMock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A based index manager to allow indexing to be done in memory rather than on the FS.
 * <p/>
 * It can be used in test cases which involves the validity of data being indexed/deindexed/reindexed
 */
public class MemoryIndexManager extends DefaultIndexManager
{
    static private final IndexPathManager indexPath = new MockIndexPathManager();

    public MemoryIndexManager()
    {
        this(new DelayedDelegatingIssueManager());
    }

    public MemoryIndexManager(final IssueManager issueManager)
    {
        this(new Function<IndexDirectoryFactory.Name, Directory>()
        {
            public Directory get(final IndexDirectoryFactory.Name type)
            {
                return MockSearcherFactory.getCleanRAMDirectory();
            }
        }, issueManager);
    }

    public MemoryIndexManager(final Function<IndexDirectoryFactory.Name, Directory> directoryFactory, final IssueManager issueManager)
    {
        super(new DelayedDelegatingIndexingConfiguration(), new MemoryIssueIndexer(directoryFactory, issueManager), indexPath, EasyMock.createMock(ReindexMessageManager.class));
    }
    
    private static class DelayedDelegatingIndexingConfiguration implements IndexingConfiguration
    {

        public int getIndexLockWaitTime()
        {
            return getIndexingConfiguration().getIndexLockWaitTime();
        }

        private IndexingConfiguration getIndexingConfiguration() {return ComponentManager.getComponentInstanceOfType(IndexingConfiguration.class);}

        public int getMaxReindexes()
        {
            return getIndexingConfiguration().getMaxReindexes();
        }

        public int getIssuesToForceOptimize()
        {
            return getIndexingConfiguration().getIssuesToForceOptimize();
        }

        public boolean isIndexingEnabled()
        {
            return getIndexingConfiguration().isIndexingEnabled();
        }

        public void disableIndexing()
        {
            getIndexingConfiguration().disableIndexing();
        }

        public void enableIndexing()
        {
            getIndexingConfiguration().enableIndexing();
        }

    }

    private static class DelayedDelegatingIssueManager implements IssueManager
    {

        public GenericValue getIssue(final Long id) throws DataAccessException
        {
            return getIssueManager().getIssue(id);
        }

        private IssueManager getIssueManager()
        {
            return ComponentManager.getComponentInstanceOfType(IssueManager.class);
        }

        public GenericValue getIssue(final String key) throws GenericEntityException
        {
            return getIssueManager().getIssue(key);
        }

        public GenericValue getIssueByWorkflow(final Long wfid) throws GenericEntityException
        {
            return getIssueManager().getIssueByWorkflow(wfid);
        }

        public MutableIssue getIssueObjectByWorkflow(final Long workflowId) throws GenericEntityException
        {
            return getIssueManager().getIssueObjectByWorkflow(workflowId);
        }

        public MutableIssue getIssueObject(final Long id) throws DataAccessException
        {
            return getIssueManager().getIssueObject(id);
        }

        public MutableIssue getIssueObject(final String key) throws DataAccessException
        {
            return getIssueManager().getIssueObject(key);
        }

        public List<GenericValue> getIssues(final Collection<Long> ids)
        {
            return getIssueManager().getIssues(ids);
        }

        @Override
        public List<Issue> getVotedIssues(com.opensymphony.user.User user) throws GenericEntityException
        {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public List<Issue> getVotedIssues(final User user) throws GenericEntityException
        {
            return getIssueManager().getVotedIssues(user);
        }

        @Override
        public List<Issue> getVotedIssuesOverrideSecurity(com.opensymphony.user.User user) throws GenericEntityException
        {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public List<Issue> getVotedIssuesOverrideSecurity(final User user) throws GenericEntityException
        {
            return getIssueManager().getVotedIssuesOverrideSecurity(user);
        }

        public List<com.opensymphony.user.User> getIssueWatchers(final GenericValue issue)
        {
            return getIssueManager().getIssueWatchers(issue);
        }

        public List<com.opensymphony.user.User> getIssueWatchers(final Issue issue)
        {
            return getIssueWatchers(issue);
        }

        @Override
        public List<Issue> getWatchedIssues(com.opensymphony.user.User user)
        {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public List<Issue> getWatchedIssues(final User user)
        {
            return getWatchedIssues(user);
        }

        @Override
        public List<User> getWatchers(Issue issue)
        {
            return getIssueManager().getWatchers(issue);
        }

        @Override
        public List<Issue> getWatchedIssuesOverrideSecurity(com.opensymphony.user.User user)
        {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public List<Issue> getWatchedIssuesOverrideSecurity(final User user)
        {
            return getWatchedIssuesOverrideSecurity(user);
        }

        public List<GenericValue> getEntitiesByIssue(final String relationName, final GenericValue issue)
                throws GenericEntityException
        {
            return getIssueManager().getEntitiesByIssue(relationName, issue);
        }

        public List<GenericValue> getEntitiesByIssueObject(final String relationName, final Issue issue)
                throws GenericEntityException
        {
            return getIssueManager().getEntitiesByIssueObject(relationName, issue);
        }

        public List<GenericValue> getIssuesByEntity(final String relationName, final GenericValue entity)
                throws GenericEntityException
        {
            return getIssueManager().getIssuesByEntity(relationName, entity);
        }

        public GenericValue createIssue(final String remoteUserName, final Map<String, Object> fields)
                throws CreateException
        {
            return getIssueManager().createIssue(remoteUserName, fields);
        }

        @Override
        public GenericValue createIssue(com.opensymphony.user.User remoteUser, Map<String, Object> fields)
                throws CreateException
        {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public GenericValue createIssue(final User remoteUser, final Map<String, Object> fields) throws CreateException
        {
            return getIssueManager().createIssue(remoteUser, fields);
        }

        @Override
        public GenericValue createIssue(com.opensymphony.user.User remoteUser, Issue issue) throws CreateException
        {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public GenericValue createIssue(final User remoteUser, final Issue issue) throws CreateException
        {
            return getIssueManager().createIssue(remoteUser, issue);
        }

        @Override
        public Issue updateIssue(com.opensymphony.user.User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
                throws UpdateException
        {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public Issue updateIssue(final User user, final MutableIssue issue, final EventDispatchOption eventDispatchOption, final boolean sendMail) throws UpdateException
        {
            return getIssueManager().updateIssue(user, issue, eventDispatchOption, sendMail);
        }

        @Override
        public void deleteIssue(com.opensymphony.user.User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
                throws RemoveException
        {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public void deleteIssue(final User user, final MutableIssue issue, final EventDispatchOption eventDispatchOption, final boolean sendMail)
                throws RemoveException
        {
            getIssueManager().deleteIssue(user, issue, eventDispatchOption, sendMail);
        }

        public List<GenericValue> getProjectIssues(final GenericValue project) throws GenericEntityException
        {
            return getIssueManager().getProjectIssues(project);
        }

        public boolean isEditable(final Issue issue)
        {
            return getIssueManager().isEditable(issue);
        }

        @Override
        public boolean isEditable(Issue issue, com.opensymphony.user.User user)
        {
            throw new UnsupportedOperationException("Not implemented yet.");
        }

        public boolean isEditable(final Issue issue, final User user)
        {
            return getIssueManager().isEditable(issue, user);
        }

        public Collection<Long> getIssueIdsForProject(final Long projectId) throws GenericEntityException
        {
            return getIssueManager().getIssueIdsForProject(projectId);
        }

        public long getIssueCountForProject(final Long projectId)
        {
            return getIssueManager().getIssueCountForProject(projectId);
        }
    }
}
