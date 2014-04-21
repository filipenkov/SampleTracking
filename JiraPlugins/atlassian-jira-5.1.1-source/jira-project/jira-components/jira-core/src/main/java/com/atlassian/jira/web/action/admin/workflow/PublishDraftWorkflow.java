package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.issue.fields.option.TextOption;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.atlassian.jira.workflow.names.WorkflowCopyNameFactory;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.lang.StringUtils;

import java.util.Collection;

/**
 * This webwork Action will do validation, and then display a confirmation screen to the user when they want to
 * publish a draft workflow to become active.
 *
 * @since v3.13
 */
@WebSudoRequired
public class PublishDraftWorkflow extends JiraWebActionSupport
{
    private final WorkflowService workflowService;
    private final JiraWorkflow jiraWorkflow;
    private final WorkflowCopyNameFactory workflowCopyNameFactory;
    private boolean enableBackup = false;
    private boolean enableBackupSubmitted = false;
    private boolean madeDeliberateChoice = false;
    private String newWorkflowName;

    public PublishDraftWorkflow(final WorkflowService workflowService, final JiraWorkflow jiraWorkflow,
            final WorkflowCopyNameFactory workflowCopyNameFactory)
    {
        this.workflowService = workflowService;
        this.jiraWorkflow = jiraWorkflow;
        this.workflowCopyNameFactory = workflowCopyNameFactory;
    }

    public String doDefault() throws Exception
    {
        generateWorkflowName();
        return INPUT;
    }

    private void generateWorkflowName()
    {
        newWorkflowName = workflowCopyNameFactory.createFrom(getWorkflow().getName(), getLocale());
    }

    protected void doValidation()
    {
        if (!enableBackupSubmitted)
        {
            addError("enableBackup", getText("admin.workflows.publish.error.save.backup"));
            // the backup button is "disabled" by default and enabled via JS.  It will be null (ie not submitted) if
            // they never chose the backup radio button.  So lets force it to the right value
            if (StringUtils.isBlank(newWorkflowName))
            {
                generateWorkflowName();                
            }
        } else {
            setMadeDeliberateChoice(true);
        }
        //if we're saving a backup, check the new workflowname provided is valid!
        if (enableBackup)
        {
            workflowService.validateCopyWorkflow(getJiraServiceContext(), newWorkflowName);
        }
        workflowService.validateOverwriteWorkflow(getJiraServiceContext(), jiraWorkflow.getName());
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (enableBackup)
        {
            final JiraWorkflow activeWorkflow = workflowService.getWorkflow(getJiraServiceContext(), jiraWorkflow.getName());
            workflowService.copyWorkflow(getJiraServiceContext(), newWorkflowName, null, activeWorkflow);
        }

        workflowService.overwriteActiveWorkflow(getJiraServiceContext(), jiraWorkflow.getName());
        if (hasAnyErrors())
        {
            return ERROR;
        }

        UrlBuilder builder = new UrlBuilder("ViewWorkflowSteps.jspa")
                .addParameter("workflowName", jiraWorkflow.getName())
                .addParameter("workflowMode", "live");

        return returnCompleteWithInlineRedirectAndMsg(builder.asUrlString(),
                getText("admin.workflows.draft.draftworkflow.now.active"), MessageType.SUCCESS, false, null);
    }

    public JiraWorkflow getWorkflow()
    {
        return jiraWorkflow;
    }

    public boolean isEnableBackup()
    {
        return enableBackup;
    }

    public void setEnableBackup(final boolean enableBackup)
    {
        enableBackupSubmitted = true;
        this.enableBackup = enableBackup;
    }

    public String getNewWorkflowName()
    {
        return newWorkflowName;
    }

    public void setNewWorkflowName(final String newWorkflowName)
    {
        this.newWorkflowName = newWorkflowName;
    }

    public Collection getBooleanList()
    {
        return EasyList.build(new TextOption("true", getText("common.words.yes")), new TextOption("false", getText("common.words.no")));
    }

    public String getWorkflowDisplayName()
    {
        return WorkflowUtil.getWorkflowDisplayName(getWorkflow());
    }

    public boolean isMadeDeliberateChoice()
    {
        return madeDeliberateChoice;
    }

    public void setMadeDeliberateChoice(final boolean madeDeliberateChoice)
    {
        this.madeDeliberateChoice = madeDeliberateChoice;
    }
}
