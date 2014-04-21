package com.sysbliss.jira.plugins.workflow.event;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.DraftWorkflowCreatedEvent;
import com.atlassian.jira.event.DraftWorkflowPublishedEvent;
import com.atlassian.jira.event.WorkflowCopiedEvent;
import com.atlassian.jira.event.WorkflowDeletedEvent;
import com.atlassian.sal.api.lifecycle.LifecycleAware;
import com.sysbliss.jira.plugins.workflow.manager.CachingWorkflowImageManagerImpl;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowAnnotationManager;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowImageManager;
import com.sysbliss.jira.plugins.workflow.manager.WorkflowLayoutManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

/**
 * Author: jdoklovic
 */
public class WorkflowLayoutHousekeeperImpl implements WorkflowLayoutHousekeeper, LifecycleAware, DisposableBean
{

    public static Logger log = Logger.getLogger(WorkflowLayoutHousekeeperImpl.class);
    private final EventPublisher eventPublisher;
    private final WorkflowLayoutManager workflowLayoutManager;
    private final WorkflowAnnotationManager workflowAnnotationManager;
    private final WorkflowImageManager workflowImageManager;

    public WorkflowLayoutHousekeeperImpl(EventPublisher eventPublisher, WorkflowLayoutManager workflowLayoutManager, WorkflowImageManager workflowImageManager, WorkflowAnnotationManager workflowAnnotationManager) {
        this.eventPublisher = eventPublisher;
        this.workflowLayoutManager = workflowLayoutManager;
        this.workflowImageManager = workflowImageManager;
        this.workflowAnnotationManager = workflowAnnotationManager;
    }

    @EventListener
    public void onDraftWorkflowCreated(DraftWorkflowCreatedEvent event) throws Exception {
        workflowLayoutManager.copyLayoutForDraftWorkflow(event.getWorkflow().getName());
        workflowAnnotationManager.copyAnnotationsForDraftWorkflow(event.getWorkflow().getName());
    }

    @EventListener
    public void onDraftWorkflowPublished(DraftWorkflowPublishedEvent event) throws Exception {
        workflowLayoutManager.publishDraftLayout(event.getWorkflow().getName());
        clearImageCache(event.getWorkflow().getName());
        workflowAnnotationManager.publishDraftAnnotations(event.getWorkflow().getName());
    }

    @EventListener
    public void onWorkflowCopied(WorkflowCopiedEvent event) throws Exception {
        workflowLayoutManager.copyActiveLayout(event.getOriginalWorkflow().getName(), event.getNewWorkflow().getName());
        workflowAnnotationManager.copyActiveAnnotations(event.getOriginalWorkflow().getName(), event.getNewWorkflow().getName());
    }

    @EventListener
    public void onWorkflowDeleted(WorkflowDeletedEvent event) {
        workflowLayoutManager.removeActiveLayout(event.getWorkflow().getName());
    }

    @EventListener
    public void onDraftWorkflowDeleted(WorkflowDeletedEvent event) {
        workflowLayoutManager.removeDraftLayout(event.getWorkflow().getName());
    }

    protected void clearImageCache(String workflowName) {
        if (workflowImageManager instanceof CachingWorkflowImageManagerImpl) {
            ((CachingWorkflowImageManagerImpl) workflowImageManager).clearCacheForWorkflow(workflowName);
        }
    }

    public void onStart()
    {
        eventPublisher.register(this);
    }

    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }
}
