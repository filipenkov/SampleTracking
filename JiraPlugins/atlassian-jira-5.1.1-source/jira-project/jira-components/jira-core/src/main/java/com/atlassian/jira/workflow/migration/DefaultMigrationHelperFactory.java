package com.atlassian.jira.workflow.migration;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.google.common.base.Preconditions;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * @since v5.1
 */
public class DefaultMigrationHelperFactory implements MigrationHelperFactory
{
    private final WorkflowManager workflowManager;
    private final OfBizDelegator delegator;
    private final IssueManager issueManager;
    private final SchemeManager schemeManager;
    private final JiraAuthenticationContext authCtx;
    private final ConstantsManager constantsManager;
    private final TaskManager taskManager;
    private final IssueIndexManager issueIndexManager;

    public DefaultMigrationHelperFactory(WorkflowManager workflowManager, OfBizDelegator delegator,
            IssueManager issueManager, WorkflowSchemeManager schemeManager, ConstantsManager constantsManager,
            JiraAuthenticationContext authCtx, TaskManager taskManager, IssueIndexManager issueIndexManager)
    {
        this.workflowManager = workflowManager;
        this.delegator = delegator;
        this.issueManager = issueManager;
        this.schemeManager = schemeManager;
        this.authCtx = authCtx;
        this.constantsManager = constantsManager;
        this.taskManager = taskManager;
        this.issueIndexManager = issueIndexManager;
    }

    @Override
    public WorkflowMigrationHelper createMigrationHelper(GenericValue project, GenericValue scheme)
            throws GenericEntityException
    {
        Preconditions.checkNotNull(project, "project cannot be null.");

        return new WorkflowMigrationHelper(project, scheme, workflowManager, delegator,
                schemeManager, authCtx.getI18nHelper(), authCtx.getLoggedInUser(), constantsManager, taskManager, issueIndexManager);
    }
}
