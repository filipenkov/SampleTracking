package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.ActionDescriptor;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Adds "common.forms.create" meta attribute to initial action of workflows, so that
 * we can rely on its presence in {@link com.atlassian.jira.web.action.issue.CreateIssue#getSubmitButtonName()} .
 */
public class UpgradeTask_Build151 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build151.class);
    private WorkflowManager workflowManager;

    public UpgradeTask_Build151(WorkflowManager workflowManager)
    {
        this.workflowManager = workflowManager;
    }

    public String getBuildNumber()
    {
        return "151";
    }

    public String getShortDescription()
    {
        return "Make minor workflow format changes to saved workflows";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        Collection workflows = null;
        try
        {
            workflows = workflowManager.getWorkflows();
        } catch (Throwable t)
        {
            log.error("Error loading workflows. Upgrade task 151 not running. This is not critical, but you may wish to manually add 'jira.i18n.title' attributes to your workflows.", t);
            return;
        }
        Iterator iter = workflows.iterator();
        while (iter.hasNext())
        {
            JiraWorkflow jiraWorkflow = (JiraWorkflow) iter.next();
            if (jiraWorkflow.isSystemWorkflow()) continue;
            List initActions = jiraWorkflow.getDescriptor().getInitialActions();
            Iterator itActions = initActions.iterator();
            while (itActions.hasNext())
            {
                ActionDescriptor actionDescriptor = (ActionDescriptor) itActions.next();
                Map metaAttributes = actionDescriptor.getMetaAttributes();
                if (!metaAttributes.containsKey(JiraWorkflow.JIRA_META_ATTRIBUTE_I18N))
                {
                    log.info("Adding " + JiraWorkflow.JIRA_META_ATTRIBUTE_I18N + " attribute to initial action of workflow '" + jiraWorkflow.getName() + "'");
                    metaAttributes.put(JiraWorkflow.JIRA_META_ATTRIBUTE_I18N, "common.forms.create");
                    workflowManager.saveWorkflowWithoutAudit(jiraWorkflow);
                }
            }
        }
    }
}
