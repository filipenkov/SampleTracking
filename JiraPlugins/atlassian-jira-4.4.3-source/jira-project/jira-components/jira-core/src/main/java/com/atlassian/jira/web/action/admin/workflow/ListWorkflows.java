/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Mar 23, 2004
 * Time: 4:02:21 PM
 */
package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

@WebSudoRequired
public class ListWorkflows extends JiraWebActionSupport
{
    private JiraWorkflow workflow;
    private final WorkflowManager workflowManager;
    private final ProjectManager projectManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private List workflows;
    private String description;
    private String newWorkflowName; // for doAddWorkflow
    private String workflowName; // for doDeleteWorkflow
    private String workflowMode; // for doDeleteWorkflow
    private boolean confirmedDelete; // doDeleteWorkflow

    public ListWorkflows(WorkflowManager workflowManager, ProjectManager projectManager, WorkflowSchemeManager workflowSchemeManager)
    {
        this.workflowManager = workflowManager;
        this.projectManager = projectManager;
        this.workflowSchemeManager = workflowSchemeManager;
    }

    protected String doExecute() throws Exception
    {
        workflows = getWorkflowsIncludingDrafts();
        return SUCCESS;
    }

    @RequiresXsrfCheck
    public String doAddWorkflow() throws Exception
    {
        if (!TextUtils.stringSet(newWorkflowName))
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
            return INPUT;

        ConfigurableJiraWorkflow newWorkflow = new ConfigurableJiraWorkflow(newWorkflowName, workflowManager);
        newWorkflow.setDescription(description);
        workflowManager.createWorkflow(getRemoteUser(), newWorkflow);
        return getRedirect("ListWorkflows.jspa");
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
                return getRedirect("ListWorkflows.jspa");
            }

            JiraWorkflow workflow = workflowManager.getWorkflow(workflowName);
            if (workflow != null)
            {
                if (workflow.isEditable())
                {
                    // Ensure that the workflow is not associated with any schemes
                    Collection workflowSchemes = workflowSchemeManager.getSchemesForWorkflow(workflow);
                    if (workflowSchemes == null || workflowSchemes.isEmpty())
                    {
                        workflowManager.deleteWorkflow(workflowManager.getWorkflow(workflowName));
                        return getRedirect("ListWorkflows.jspa");
                    }
                    else
                    {
                        StringBuffer schemes = new StringBuffer();
                        for (Iterator iterator = workflowSchemes.iterator(); iterator.hasNext();)
                        {
                            GenericValue schemeGV = (GenericValue) iterator.next();
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

    public List getWorkflows()
    {
        return workflows;
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
        if (workflow == null && TextUtils.stringSet(newWorkflowName))
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
        Collection projects = projectManager.getProjects();
        return (projects == null || projects.size() == 0);
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

    private List getWorkflowsIncludingDrafts()
    {
        List ret = workflowManager.getWorkflowsIncludingDrafts();
        for (Iterator iterator = ret.iterator(); iterator.hasNext();)
        {
            JiraWorkflow jiraWorkflow = (JiraWorkflow) iterator.next();
            if (jiraWorkflow.isDraftWorkflow())
            {
                if(!isParentWorkflowActive(jiraWorkflow))
                {
                    addErrorMessage("The parent workflow of draft '"+ jiraWorkflow.getName() + "' is no longer active. Please delete the draft. You may wish to copy the draft before deleting it.");
                }
            }
        }
        return ret;
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