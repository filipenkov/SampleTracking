package com.atlassian.jira.config;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.issue.status.StatusImpl;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.google.common.collect.Lists;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v5.0
 */
public class DefaultStatusManager extends AbstractIssueConstantsManager<Status> implements StatusManager
{
    private final TranslationManager translationManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final WorkflowManager workflowManager;
    // New statuses are given ids starting from 10000 - avoids conflict with future system statuses.
    private static final Long NEW_STATUS_START_ID = 10000L;

    public DefaultStatusManager(ConstantsManager constantsManager, OfBizDelegator ofBizDelegator, IssueIndexManager issueIndexManager, TranslationManager translationManager, JiraAuthenticationContext jiraAuthenticationContext, WorkflowManager workflowManager)
    {
        super(constantsManager, ofBizDelegator, issueIndexManager);
        this.translationManager = translationManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.workflowManager = workflowManager;
    }

    @Override
    public synchronized Status createStatus(String name, String description, String iconUrl)
    {
        Assertions.notBlank("name", name);
        Assertions.notBlank("iconUrl", iconUrl);
        for (Status status : constantsManager.getStatusObjects())
        {
            if (name.trim().equalsIgnoreCase(status.getName()))
            {
                throw new DataAccessException("A status with the name '" + name + "' already exists.");
            }
        }
        try
        {
            Map<String, Object> fields = new HashMap<String, Object>();
            fields.put("name", name);
            fields.put("description", description);
            fields.put("iconurl", iconUrl);
            fields.put("sequence", new Long(getMaxSequenceNo() + 1));
            String nextStringId = getNextStringId();
            Long nextId = Long.valueOf(nextStringId);
            if (nextId < NEW_STATUS_START_ID)
            {
                fields.put("id", NEW_STATUS_START_ID.toString());
            }
            else
            {
                fields.put("id", nextStringId);
            }
            GenericValue statusGv = createConstant(fields);
            return new StatusImpl(statusGv, translationManager, jiraAuthenticationContext);
        }
        catch (GenericEntityException ex)
        {
            throw new DataAccessException("Failed to create new status with name '" + name + "'", ex);
        }
        finally
        {
            clearCaches();
        }
    }

    @Override
    public void editStatus(Status status, String name, String description, String iconUrl)
    {
        Assertions.notNull("status", status);
        Assertions.notBlank("name", name);
        Assertions.notBlank("iconUrl", iconUrl);
        for (Status st : getStatuses())
        {
            if (name.equalsIgnoreCase(st.getName()) && !status.getId().equals(st.getId()))
            {
                throw new IllegalStateException("Cannot rename status. A status with the name '" + name + "' exists already.");
            }
        }
        try
        {
            status.setName(name);
            status.setIconUrl(iconUrl);
            status.setDescription(description);
            status.getGenericValue().store();
            clearCaches();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Failed to update status '" + status.getName() + "'", e);
        }
    }

    @Override
    public Collection<Status> getStatuses()
    {
        return constantsManager.getStatusObjects();
    }

    @Override
    public void removeStatus(final String id)
    {
        Status status = getStatus(id);
        if (status == null)
        {
            throw new IllegalArgumentException("A status with id '" + id + "' does not exist.");
        }
        final List<JiraWorkflow> existingWorkflows = workflowManager.getWorkflowsIncludingDrafts();
        for (JiraWorkflow workflow : existingWorkflows)
        {
            Collection linkStatuses = workflow.getLinkedStatuses();
            if (linkStatuses.contains(status.getGenericValue()))
            {
                throw new IllegalStateException("Cannot delete a status which is associated with a workflow. Status is associated with workflow " + workflow.getName());
            }
        }
        try
        {
            removeConstant(getIssueConstantField(), status, null);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException("Failed to remove status with id '" + id + "'", e);
        }
        catch (IndexException e)
        {
            throw new DataAccessException("Failed to remove status with id '" + id + "'", e);
        }
    }

    @Override
    public Status getStatus(String id)
    {
        Assertions.notBlank("id", id);
        return constantsManager.getStatusObject(id);
    }

    @Override
    protected void postProcess(Status constant)
    {
    }

    @Override
    protected void clearCaches()
    {
        constantsManager.refreshStatuses();
    }

    @Override
    protected String getIssueConstantField()
    {
        return ConstantsManager.STATUS_CONSTANT_TYPE;
    }

    @Override
    protected List<Status> getAllValues()
    {
        return Lists.newArrayList(getStatuses());
    }
}
