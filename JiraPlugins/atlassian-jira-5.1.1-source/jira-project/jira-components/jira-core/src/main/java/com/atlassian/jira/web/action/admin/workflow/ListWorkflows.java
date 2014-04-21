/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.datetime.DateTimeStyle;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.JiraUrlCodec;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

@WebSudoRequired
public class ListWorkflows extends JiraWebActionSupport
{
    private JiraWorkflow workflow;
    private final WorkflowManager workflowManager;
    private final ProjectManager projectManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private Collection<JiraWorkflow> workflows;
    private String description;
    private String newWorkflowName; // for doAddWorkflow
    private String workflowName; // for doDeleteWorkflow
    private String workflowMode; // for doDeleteWorkflow
    private boolean confirmedDelete; // doDeleteWorkflow
    private ImmutableList<JiraWorkflow> draftWorkflows;

    public ListWorkflows(final WorkflowManager workflowManager, final ProjectManager projectManager,
            final WorkflowSchemeManager workflowSchemeManager)
    {
        this.workflowManager = workflowManager;
        this.projectManager = projectManager;
        this.workflowSchemeManager = workflowSchemeManager;
    }

    protected String doExecute() throws Exception
    {
        return SUCCESS;
    }

    public String doAddNewWorkflow() throws Exception
    {
        return INPUT;
    }

    @RequiresXsrfCheck
    public String doAddWorkflow() throws Exception
    {
        if (isBlank(newWorkflowName))
        {
            addError("newWorkflowName", getText("admin.errors.you.must.specify.a.workflow.name"));
        }
        else if (!WorkflowUtil.isAcceptableName(newWorkflowName))
        {
            addError("newWorkflowName", getText("admin.errors.please.use.only.ascii.characters"));
        }
        else if (workflowManager.workflowExists(newWorkflowName))
        {
            addError("newWorkflowName", getText("admin.errors.a.workflow.with.this.name.already.exists"));
        }

        if (invalidInput())
        { return INPUT; }

        ConfigurableJiraWorkflow newWorkflow = new ConfigurableJiraWorkflow(newWorkflowName, workflowManager);
        newWorkflow.setDescription(description);
        workflowManager.createWorkflow(getLoggedInUser(), newWorkflow);

        UrlBuilder builder = new UrlBuilder("EditWorkflowDispatcher.jspa")
                .addParameter("wfName", newWorkflow.getName())
                .addParameter("atl_token", getXsrfToken());

        return returnCompleteWithInlineRedirect(builder.asUrlString());
    }

    // Note: Other workflow operation actions (eg. ViewWorkflowSteps) have the workflow passed in through the constructor with Pico magic
    // This action does not deal with any specific workflow, so the name needs to be passed in for doDeleteWorkflow (JT)
    public void setWorkflowName(String workflowName)
    {
        this.workflowName = workflowName;
    }

    @RequiresXsrfCheck
    public String doDeleteWorkflow() throws Exception
    {
        if (confirmedDelete)
        {
            if (JiraWorkflow.DRAFT.equals(workflowMode))
            {
                workflowManager.deleteDraftWorkflow(workflowName);
                UrlBuilder builder = new UrlBuilder("/secure/admin/workflows/ViewWorkflowSteps.jspa")
                        .addParameter("workflowMode", "live")
                        .addParameter("workflowName", workflowName);

                return returnCompleteWithInlineRedirectAndMsg(builder.asUrlString(),
                        getText("admin.workflows.draft.draftworkflow.was.deleted"), MessageType.SUCCESS, false, null);
            }

            JiraWorkflow workflow = workflowManager.getWorkflow(workflowName);
            if (workflow != null)
            {
                if (workflow.isEditable())
                {
                    // Ensure that the workflow is not associated with any schemes
                    Collection<GenericValue> workflowSchemes = workflowSchemeManager.getSchemesForWorkflow(workflow);
                    if (workflowSchemes == null || workflowSchemes.isEmpty())
                    {
                        workflowManager.deleteWorkflow(workflowManager.getWorkflow(workflowName));
                        return returnCompleteWithInlineRedirect("ListWorkflows.jspa");
                    }
                    else
                    {
                        StringBuilder schemes = new StringBuilder();
                        for (GenericValue schemeGV : workflowSchemes)
                        {
                            schemes.append('\'').append(schemeGV.getString("name")).append('\'').append(", ");
                        }
                        schemes.delete(schemes.length() - 2, schemes.length() - 1);
                        addErrorMessage(getText("admin.errors.cannot.delete.this.workflow") + " " + schemes);
                        return getResult();
                    }
                }
                else
                {
                    addErrorMessage(getText("admin.errors.workflow.cannot.be.deleted.as.it.is.not.editable"));
                    return getResult();
                }
            }
            else
            {
                addErrorMessage(getText("admin.errors.workflow.with.name.does.not.exist", "'" + workflowName + "'"));
                return getResult();
            }
        }
        else
        {
            return INPUT;
        }
    }

    public Collection<JiraWorkflow> getWorkflows()
    {
        if (workflows == null)
        {
            workflows = workflowManager.getWorkflows();
        }
        return workflows;
    }

    public List<JiraWorkflow> getActiveWorkflows()
    {
        return newArrayList(filter(getWorkflows(), new Predicate<JiraWorkflow>()
        {
            @Override
            public boolean apply(final JiraWorkflow workflow)
            {
                return workflow.isActive();
            }
        }));
    }

    public List<JiraWorkflow> getInactiveWorkflows()
    {
        return newArrayList(filter(getWorkflows(), new Predicate<JiraWorkflow>()
        {
            @Override
            public boolean apply(final JiraWorkflow workflow)
            {
                return !workflow.isActive();
            }
        }));
    }

    public JiraWorkflow getDraftFor(final String workflowName)
    {
        for (final JiraWorkflow draft : getDraftWorkflows())
        {
            if (draft.getName().equals(workflowName))
            {
                return draft;
            }
        }
        return null;
    }

    public String getLastModifiedDateForDraft(final JiraWorkflow workflow)
    {
        final JiraWorkflow draft = getDraftFor(workflow.getName());
        return getDateTimeFormatter().withStyle(DateTimeStyle.COMPLETE).format(draft.getUpdatedDate());
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public JiraWorkflow getWorkflow()
    {
        if (workflow == null && isNotBlank(newWorkflowName))
        {
            workflow = workflowManager.getWorkflow(newWorkflowName);
        }

        return workflow;
    }

    public String getNewWorkflowName()
    {
        return newWorkflowName;
    }

    public void setNewWorkflowName(String newWorkflowName)
    {
        this.newWorkflowName = newWorkflowName;
    }

    public boolean isNoProjects() throws GenericEntityException
    {
        return projectManager.getProjectObjects().size() == 0;
    }

    // For doDeleteWorkflow
    public String getWorkflowName()
    {
        return workflowName;
    }

    public void setConfirmedDelete(boolean confirmedDelete)
    {
        this.confirmedDelete = confirmedDelete;
    }

    public Collection getSchemesForWorkflow(JiraWorkflow workflow)
    {
        return workflowSchemeManager.getSchemesForWorkflow(workflow);
    }

    private void showErrorsForDraftWorkflowsWithAnInactiveParent()
    {
        for (final JiraWorkflow draft : getDraftWorkflows())
        {
            if (!isParentWorkflowActive(draft))
            {
                addErrorMessage("The parent workflow of draft '" + draft.getName() + "' is no longer active. Please delete the draft. You may wish to copy the draft before deleting it.");
            }
        }
    }

    private List<JiraWorkflow> getDraftWorkflows()
    {
        if (draftWorkflows == null)
        {
            final ImmutableList.Builder<JiraWorkflow> results = ImmutableList.builder();
            final List<JiraWorkflow> allWorkflows = workflowManager.getWorkflowsIncludingDrafts();
            for (final JiraWorkflow jiraWorkflow : allWorkflows)
            {
                if (jiraWorkflow.isDraftWorkflow())
                {
                    results.add(jiraWorkflow);
                }
            }
            draftWorkflows = results.build();
        }
        return draftWorkflows;
    }

    public void setWorkflowMode(String workflowMode)
    {
        this.workflowMode = workflowMode;
    }

    public boolean isParentWorkflowActive(JiraWorkflow workflow)
    {
        //not an draft workflow? Well you don't have a parent so your parent is active for the purposes
        // of this method.
        if(!workflow.isDraftWorkflow())
        {
            return true;
        }

        JiraWorkflow parentWorkflow = workflowManager.getWorkflow(workflow.getName());
        return parentWorkflow != null && parentWorkflow.isActive();
    }
}
