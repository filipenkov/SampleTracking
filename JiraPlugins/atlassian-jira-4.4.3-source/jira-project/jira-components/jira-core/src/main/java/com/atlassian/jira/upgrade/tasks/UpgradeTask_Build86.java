package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.web.util.ExternalLinkUtilImpl;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.loader.StepDescriptor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;


public class UpgradeTask_Build86 extends AbstractFieldScreenUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build86.class);

    private final ConstantsManager constantsManager;
    private final WorkflowManager workflowManager;

    public UpgradeTask_Build86(ConstantsManager constantsManager, WorkflowManager workflowManager)
    {
        this.constantsManager = constantsManager;
        this.workflowManager = workflowManager;
    }

    public String getBuildNumber()
    {
        return "86";
    }

    public String getShortDescription()
    {
        return "Updating all workflows with closed status to have the jira.issue.editable=false flag (for backwards compatibility)";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        final Collection workflows = workflowManager.getWorkflows();
        GenericValue closedStatus = constantsManager.getStatus("6");
        for (Iterator iterator = workflows.iterator(); iterator.hasNext();)
        {
            JiraWorkflow workflow = (JiraWorkflow) iterator.next();

            if (!workflow.isDefault())
            {

                final StepDescriptor closedStep = workflow.getLinkedStep(closedStatus);
                if (closedStep != null)
                {
                    final Map metaAttributes = closedStep.getMetaAttributes();
                    if (!metaAttributes.containsKey(JiraWorkflow.JIRA_META_ATTRIBUTE_EDIT_ALLOWED))
                    {
                        if (!workflow.isSystemWorkflow())
                        {
                            log.info("Step '" + closedStep.getName() + "' in workflow '" + workflow.getName() + "' is associated with closed status. " + JiraWorkflow.JIRA_META_ATTRIBUTE_EDIT_ALLOWED + "=false meta property automatically added");
                            metaAttributes.put(JiraWorkflow.JIRA_META_ATTRIBUTE_EDIT_ALLOWED, "false");
                            workflowManager.saveWorkflowWithoutAudit(workflow);
                        }
                        else
                        {
                            // Workflow is on disk.
                            log.warn("MANUAL UPGRADE REQUIRED: The workflow '" + workflow.getName() + "' is on disk and cannot be automatically upgraded. Please see " + ExternalLinkUtilImpl.getInstance().getProperty("external.link.jira.confluence.doc.workflow") + " for more details.");
                        }
                    }
                    else
                    {
                        log.info("Step '" + closedStep.getName() + "' in workflow '" + workflow.getName() + "' already has the " + JiraWorkflow.JIRA_META_ATTRIBUTE_EDIT_ALLOWED + " property. No upgrade required.");
                    }
                }
                else
                {
                    log.info("Workflow '" + workflow.getName() + "' does not require an upgrade");
                }

            }

        }
    }
}
