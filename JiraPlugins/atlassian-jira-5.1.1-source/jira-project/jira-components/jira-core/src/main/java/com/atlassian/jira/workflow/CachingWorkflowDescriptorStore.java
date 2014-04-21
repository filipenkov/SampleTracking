package com.atlassian.jira.workflow;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import net.jcip.annotations.GuardedBy;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Used to cache workflowDescriptors in JIRA. This caches {@link ImmutableWorkflowDescriptor}s.
 * These objects are very heavy weight and ideally we would not cache them, but it is the only way to
 * quickly give JIRA access to workflow objects. This is because the safe thing to cache is the workflow XML string but
 * converting this to an object graph will be expensive.  Also please note that the implementation of
 * {@link ImmutableWorkflowDescriptor} cannot guarantee 100% immutability.
 * <p/>
 * This is essentially replacing the store in the {@link com.atlassian.jira.workflow.JiraWorkflowFactory}, but it adds
 * some more concurrency controls to ensure consistency with the underlying store (such as the
 * {@link com.atlassian.jira.workflow.OfBizWorkflowDescriptorStore})
 *
 * @since v3.13
 */
@EventComponent
public class CachingWorkflowDescriptorStore implements WorkflowDescriptorStore
{
    private final ConcurrentMap<String, ImmutableWorkflowDescriptor> workflowCache = new ConcurrentHashMap<String, ImmutableWorkflowDescriptor>();
    private final WorkflowDescriptorStore delegate;
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    public CachingWorkflowDescriptorStore(final WorkflowDescriptorStore delegate)
    {
        this.delegate = delegate;
        cacheLock.writeLock().lock();
        try
        {
            loadWorkflows();
        }
        finally
        {
            cacheLock.writeLock().unlock();
        }
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        cacheLock.writeLock().lock();
        try
        {
            loadWorkflows();
        }
        finally
        {
            cacheLock.writeLock().unlock();
        }
    }

    public ImmutableWorkflowDescriptor getWorkflow(final String name) throws FactoryException
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Workflow name cannot be null!");
        }
        cacheLock.readLock().lock();
        try
        {
            //cache is eagerly loaded, so no need to check with the delegate.
            return workflowCache.get(name);
        }
        finally
        {
            cacheLock.readLock().unlock();
        }
    }

    public boolean removeWorkflow(final String name)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("Workflow name cannot be null!");
        }

        boolean deleted;
        cacheLock.writeLock().lock();
        try
        {
            try
            {
                deleted = delegate.removeWorkflow(name);
            }
            catch (final RuntimeException e)
            {
                //we should reload all the workflows, just in case there were errors deleting a workflow
                //to make sure the cache is in a consistent state.
                loadWorkflows();
                throw e;
            }
            workflowCache.remove(name);
        }
        finally
        {
            cacheLock.writeLock().unlock();
        }
        return deleted;
    }

    public boolean saveWorkflow(final String name, final WorkflowDescriptor workflowDescriptor, final boolean replace) throws DataAccessException
    {
        if (name == null)
        {
            throw new IllegalArgumentException("name may not be null!");
        }
        if (workflowDescriptor == null)
        {
            throw new IllegalArgumentException("workflowDescriptor may not be null!");
        }
        cacheLock.writeLock().lock();
        boolean saved;
        try
        {
            saved = delegate.saveWorkflow(name, workflowDescriptor, replace);
            try
            {
                final ImmutableWorkflowDescriptor storedWorkflow = delegate.getWorkflow(name);
                workflowCache.put(name, storedWorkflow);
            }
            catch (final FactoryException e)
            {
                //this should never happen since we just converted the workflowdescriptor to XML.  So if the
                //reverse (loading descriptor from XML) fails immediately after that there's a big problem...
                throw new RuntimeException("Loading workflowdescriptor saved to db failed. ", e);
            }
        }
        finally
        {
            cacheLock.writeLock().unlock();
        }
        return saved;
    }

    public String[] getWorkflowNames()
    {
        cacheLock.readLock().lock();
        final Set<String> workflowNameSet;
        try
        {
            workflowNameSet = workflowCache.keySet();
        }
        finally
        {
            cacheLock.readLock().unlock();
        }
        return workflowNameSet.toArray(new String[workflowNameSet.size()]);
    }

    public List<JiraWorkflowDTO> getAllJiraWorkflowDTOs()
    {
        return delegate.getAllJiraWorkflowDTOs();
    }

    @GuardedBy("cacheLock.writeLock()")
    private void loadWorkflows()
    {
        workflowCache.clear();
        for (final JiraWorkflowDTO jiraWorkflowDTO : delegate.getAllJiraWorkflowDTOs())
        {
            workflowCache.put(jiraWorkflowDTO.getName(), jiraWorkflowDTO.getDescriptor());
        }
    }
}
