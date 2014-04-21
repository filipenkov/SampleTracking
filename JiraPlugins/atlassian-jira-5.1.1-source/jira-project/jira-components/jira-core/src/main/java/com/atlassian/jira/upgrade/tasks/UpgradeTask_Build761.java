package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.workflow.WorkflowManager;
import org.apache.log4j.Logger;

/**
 * This upgrade task will create a backup copy of any inactive workflow drafts stored in JIRA.
 *
 * @since v5.1
 */
public class UpgradeTask_Build761 extends AbstractUpgradeTask
{
    private final WorkflowManager workflowManager;
    private Logger log = Logger.getLogger(UpgradeTask_Build761.class);

    public UpgradeTask_Build761(final WorkflowManager workflowManager)
    {
        super(false);
        this.workflowManager = workflowManager;
    }

    @Override
    public String getBuildNumber()
    {
        return "761";
    }

    @Override
    public String getShortDescription()
    {
        return "Backing up all inactive workflow drafts";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
        workflowManager.copyAndDeleteDraftsForInactiveWorkflowsIn(null, workflowManager.getWorkflows());
    }
}
