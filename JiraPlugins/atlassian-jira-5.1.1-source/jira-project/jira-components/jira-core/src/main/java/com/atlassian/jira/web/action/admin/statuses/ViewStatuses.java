package com.atlassian.jira.web.action.admin.statuses;

import com.atlassian.jira.config.StatusManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.constants.AbstractViewConstants;
import com.atlassian.jira.web.action.admin.translation.TranslationManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Sets.newHashSet;

@WebSudoRequired
public class ViewStatuses extends AbstractViewConstants
{
    public static final String STATUS_ENTITY_NAME = "Status";
    private static final String NEW_STATUS_DEFAULT_ICON = "/images/icons/status_generic.gif";
    private final StatusManager statusManager;
    private final WorkflowManager workflowManager;
    private List<JiraWorkflow> allWorkflows;

    public ViewStatuses(final TranslationManager translationManager, final StatusManager statusManager,
            final WorkflowManager workflowManager)
    {
        super(translationManager);
        this.statusManager = statusManager;
        this.workflowManager = workflowManager;
        setIconurl(NEW_STATUS_DEFAULT_ICON);
    }

    protected String getConstantEntityName()
    {
        return STATUS_ENTITY_NAME;
    }

    protected String getNiceConstantName()
    {
        return "status";
    }

    protected String getIssueConstantField()
    {
        return getText("admin.issue.constant.status.lowercase");
    }

    protected GenericValue getConstant(String id)
    {
        return getConstantsManager().getStatus(id);
    }

    protected String getRedirectPage()
    {
        return "ViewStatuses.jspa";
    }

    protected Collection<GenericValue> getConstants()
    {
        return getConstantsManager().getStatuses();
    }

    protected void clearCaches()
    {
        getConstantsManager().refreshStatuses();
    }

    @RequiresXsrfCheck
    public String doAddStatus() throws Exception
    {
        if (!TextUtils.stringSet(getIconurl()))
        {
            addError("iconurl", getText("admin.errors.must.specify.url.for.icon.of.status"));
        }

        return super.doAddConstant();
    }

    public Collection<String> getAssociatedWorkflows(GenericValue statusGV)
    {
        List<JiraWorkflow> existingWorkflows = getWorkflowsIncludingDrafts();

        // We use a set here, because workflows and any associated drafts have the same name,
        // and we only want to record the workflow once.
        Collection<String> associatedWorkflows = newHashSet();

        for (JiraWorkflow workflow : existingWorkflows)
        {
            Collection linkStatuses = workflow.getLinkedStatuses();
            if (linkStatuses.contains(statusGV))
            {
                associatedWorkflows.add(workflow.getName());
            }
        }
        return associatedWorkflows;
    }

    private List<JiraWorkflow> getWorkflowsIncludingDrafts()
    {
        // Expensive call, so we cache the results.
        if (allWorkflows == null)
        {
            allWorkflows = workflowManager.getWorkflowsIncludingDrafts();
        }
        return allWorkflows;
    }

    protected String redirectToView()
    {
        return getRedirect("ViewStatuses.jspa");
    }

    protected String getDefaultPropertyName()
    {
        return APKeys.JIRA_CONSTANT_DEFAULT_STATUS;
    }

    @Override
    protected GenericValue addConstant() throws GenericEntityException
    {
        Status status = statusManager.createStatus(name, description, iconurl);
        return status.getGenericValue();
    }

}
