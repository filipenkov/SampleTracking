package com.atlassian.jira.workflow;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import org.apache.commons.lang.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Provides a caching implementation of the {@link DraftWorkflowStore}.  This
 * implementation ensures cache consistency by using a {@link ConcurrentMap}
 * for the store, and a {@link ReentrantReadWriteLock} to ensure
 * that any updates of the database (and cache) are atomic.  The lock is a ReadWriteLock, to speed up performance for
 * gets which will be the most common operation.
 *
 * @since v3.13
 */
@EventComponent
public class CachingDraftWorkflowStore implements DraftWorkflowStore
{
    private final DraftWorkflowStore delegate;
    private WorkflowManager workflowManager;
    private final ConcurrentMap<String, String> draftWorkflowCache = new ConcurrentHashMap<String, String>();
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    private static final String NO_DRAFT = "NO_DRAFT";

    public CachingDraftWorkflowStore(final DraftWorkflowStore delegate)
    {
        this.delegate = delegate;
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        try
        {
            cacheLock.writeLock().lock();
            draftWorkflowCache.clear();
        }
        finally
        {
            cacheLock.writeLock().unlock();
        }
    }

    public JiraWorkflow getDraftWorkflow(final String parentWorkflowName) throws DataAccessException
    {
        if (StringUtils.isEmpty(parentWorkflowName))
        {
            throw new IllegalArgumentException("Can not get a draft workflow for a parent workflow name of null.");
        }
        String workflowDescriptorXML;
        //gets are protected by a read lock. Even though gets may put a value into the cache, we don't really care
        //if this happens multiple times as long as the gets don't happen while a delete is taking place.
        cacheLock.readLock().lock();
        try
        {
            workflowDescriptorXML = draftWorkflowCache.get(parentWorkflowName);
            if (workflowDescriptorXML == null || workflowDescriptorXML.equals(NO_DRAFT))
            {
                //if a draft workflow is not in the cache, try to get it from the DB.
                final JiraWorkflow draftWorkflow = delegate.getDraftWorkflow(parentWorkflowName);
                if (draftWorkflow == null)
                {
                    // Lets cache the fact that there is no Draft
                    draftWorkflowCache.put(parentWorkflowName, NO_DRAFT);
                    workflowDescriptorXML = NO_DRAFT;
                }
                else
                {
                    //lets lazy load the cache with the draft Workflow.
                    draftWorkflowCache.put(parentWorkflowName, convertDescriptorToXML(draftWorkflow.getDescriptor()));
                    workflowDescriptorXML = draftWorkflowCache.get(parentWorkflowName);
                }
            }
        }
        finally
        {
            cacheLock.readLock().unlock();
        }

        if (workflowDescriptorXML.equals(NO_DRAFT))
        {
            return null;
        }
        else
        {
            return getJiraDraftWorkflow(parentWorkflowName, workflowDescriptorXML);
        }
    }

    public JiraWorkflow createDraftWorkflow(final String authorName, final JiraWorkflow parentWorkflow) throws DataAccessException, IllegalStateException, IllegalArgumentException
    {
        //no explicity locking required here, since get will lookup values in the db anyway, if
        // nothing is found in the cache.
        final JiraWorkflow draftWorkflow = delegate.createDraftWorkflow(authorName, parentWorkflow);
        draftWorkflowCache.put(parentWorkflow.getName(), convertDescriptorToXML(draftWorkflow.getDescriptor()));

        return draftWorkflow;
    }

    public boolean deleteDraftWorkflow(final String parentWorkflowName) throws DataAccessException, IllegalArgumentException
    {
        final boolean deleted;
        //protecting deletes with a write lock here to ensure no thread will ever see an inconsistent state.  That is a get
        //may never see a cached value, which is particularly bad, because users may edit an draft workflow
        //that's already been deleted from the db.
        cacheLock.writeLock().lock();
        try
        {
            deleted = delegate.deleteDraftWorkflow(parentWorkflowName);

            // Only delete from the cache if we have successfully removed the value from the DB.
            if (deleted)
            {
                draftWorkflowCache.put(parentWorkflowName, NO_DRAFT);
            }
        }
        finally
        {
            cacheLock.writeLock().unlock();
        }

        return deleted;
    }

    public JiraWorkflow updateDraftWorkflow(final String username, final String parentWorkflowName, final JiraWorkflow workflow) throws DataAccessException
    {
        //updates also don't need explicit locking, since we don't really care if a thread is updating a potentially
        //outdated cached copy.  Whoever gets in first, will simply update the value in the db.
        final JiraWorkflow updatedWorkflow = delegate.updateDraftWorkflow(username, parentWorkflowName, workflow);
        draftWorkflowCache.put(parentWorkflowName, convertDescriptorToXML(updatedWorkflow.getDescriptor()));
        return updatedWorkflow;
    }

    public JiraWorkflow updateDraftWorkflowWithoutAudit(final String parentWorkflowName, final JiraWorkflow workflow)
            throws DataAccessException
    {
        //updates also don't need explicit locking, since we don't really care if a thread is updating a potentially
        //outdated cached copy.  Whoever gets in first, will simply update the value in the db.
        final JiraWorkflow updatedWorkflow = delegate.updateDraftWorkflowWithoutAudit(parentWorkflowName, workflow);
        draftWorkflowCache.put(parentWorkflowName, convertDescriptorToXML(updatedWorkflow.getDescriptor()));
        return updatedWorkflow;
    }

    WorkflowManager getWorkflowManager()
    {
        if (workflowManager == null)
        {
            workflowManager = ComponentAccessor.getWorkflowManager();
        }
        return workflowManager;
    }

    WorkflowDescriptor convertXMLtoWorkflowDescriptor(final String parentWorkflowXML) throws FactoryException
    {
        return WorkflowUtil.convertXMLtoWorkflowDescriptor(parentWorkflowXML);
    }

    String convertDescriptorToXML(final WorkflowDescriptor descriptor)
    {
        return WorkflowUtil.convertDescriptorToXML(descriptor);
    }

    private JiraWorkflow getJiraDraftWorkflow(final String name, final String workflowDescriptorXML)
    {
        try
        {
            return new JiraDraftWorkflow(name, getWorkflowManager(), convertXMLtoWorkflowDescriptor(workflowDescriptorXML));
        }
        catch (final FactoryException e)
        {
            throw new RuntimeException(e);
        }
    }
}
