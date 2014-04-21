/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Apr 2, 2004
 * Time: 12:19:05 PM
 */
package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.core.ofbiz.comparators.OFBizFieldComparator;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.migration.WorkflowAsynchMigrator;
import com.atlassian.jira.workflow.migration.enterprise.EnterpriseWorkflowMigrationHelper;
import com.atlassian.jira.workflow.migration.enterprise.EnterpriseWorkflowTaskContext;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

@WebSudoRequired
public class SelectProjectWorkflowSchemeStep2 extends SelectProjectWorkflowScheme
{
    private EnterpriseWorkflowMigrationHelper migrationHelper;
    private final OfBizDelegator delegator;
    private final IssueManager issueManager;
    private final TaskManager taskManager;
    private final WorkflowManager workflowManager;
    private Boolean haveIssuesToMigrate;
    private final Map failedIssueIds;
    private final SearchProvider searchProvider;
    private final static String ABORTED_MIGRATION_MESSAGE_KEY = "admin.workflowmigration.aborted.defaultworkflow";
    private static final String FAILURE_MIGRATION_MESSAGE_KEY = "admin.workflowmigration.withfailure.defaultworkflow";
    private final ConstantsManager constantsManager;
    private final WorkflowSchemeManager workflowSchemeManager;

    public SelectProjectWorkflowSchemeStep2(final SearchProvider searchProvider, final OfBizDelegator delegator, final IssueManager issueManager, final TaskManager taskManager, final JiraAuthenticationContext authenticationContext, final OutlookDateManager outlookDateManager, final WorkflowManager workflowManager, final ConstantsManager constantsManager, final WorkflowSchemeManager workflowSchemeManager)
    {
        super(taskManager, authenticationContext, outlookDateManager);
        this.searchProvider = searchProvider;
        this.delegator = delegator;
        this.issueManager = issueManager;
        this.taskManager = taskManager;
        this.workflowManager = workflowManager;
        this.constantsManager = constantsManager;
        this.workflowSchemeManager = workflowSchemeManager;
        failedIssueIds = new HashMap();
    }

    @Override
    public String doDefault() throws Exception
    {
        // if they're not swapping scheme at all - do nothing
        final GenericValue existingScheme = workflowSchemeManager.getWorkflowScheme(getProject());
        final GenericValue targetScheme = getScheme();
        if (((targetScheme == null) && (existingScheme == null)) || ((targetScheme != null) && targetScheme.equals(existingScheme)))
        {
            return getRedirect("/plugins/servlet/project-config/" + getProject().getString("key") + "/workflows");
        }

        final JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        migrationHelper = new EnterpriseWorkflowMigrationHelper(getProject(), getScheme(), workflowManager, delegator, issueManager,
            getSchemeManager(), jiraAuthenticationContext.getI18nHelper(), jiraAuthenticationContext.getUser());

        return super.doDefault();
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final JiraAuthenticationContext jiraAuthenticationContext = ComponentAccessor.getJiraAuthenticationContext();
        migrationHelper = new EnterpriseWorkflowMigrationHelper(getProject(), getScheme(), workflowManager, delegator, issueManager,
            getSchemeManager(), jiraAuthenticationContext.getI18nHelper(), jiraAuthenticationContext.getUser());
        if (isHaveIssuesToMigrate())
        {
            // setup manual migrations
            addMigrationMappings();

            if (invalidInput())
            {
                return getResult();
            }

            final String taskDesc = getText("admin.selectworkflows.task.desc", getProject().getString("name"), getSchemeName());
            final EnterpriseWorkflowTaskContext taskContext = new EnterpriseWorkflowTaskContext(getProjectId(), getSchemeId());

            try
            {
                return getRedirect(taskManager.submitTask(new WorkflowAsynchMigrator(getMigrationHelper()), taskDesc, taskContext).getProgressURL());
            }
            catch (final RejectedExecutionException e)
            {
                return ERROR;
            }
        }
        else
        {
            // Associate the scheme with project
            migrationHelper.associateProjectAndWorkflowScheme(getSchemeManager(), getProject(), getScheme());

            // And return the user to the next screen
            return redirectUser();
        }
    }

    private String redirectUser() throws Exception
    {
        return getRedirect("/plugins/servlet/project-config/" + getProject().getString("key") + "/workflows");
    }

    public Map getFailedIssueIds()
    {
        return failedIssueIds;
    }

    private void addMigrationMappings()
    {
        final Map params = ActionContext.getParameters();

        for (final Iterator iterator = migrationHelper.getTypesNeedingMigration().iterator(); iterator.hasNext();)
        {
            final GenericValue issueType = (GenericValue) iterator.next();

            for (final Iterator iterator1 = migrationHelper.getStatusesNeedingMigration(issueType).iterator(); iterator1.hasNext();)
            {
                final GenericValue status = (GenericValue) iterator1.next();
                final String[] paramValue = (String[]) params.get(getSelectListName(issueType, status));
                if ((paramValue == null) || (paramValue.length != 1))
                {
                    addErrorMessage(getText("admin.errors.workflows.specify.mapping", issueType.getString("name"), status.getString("name")));
                }
                else
                {
                    migrationHelper.addMapping(issueType, status, constantsManager.getStatus(paramValue[0]));
                }
            }
        }
    }

    public EnterpriseWorkflowMigrationHelper getMigrationHelper()
    {
        return migrationHelper;
    }

    public long getNumAffectedIssues(final GenericValue issueType) throws SearchException
    {
        final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder();
        final JqlClauseBuilder whereBuilder = queryBuilder.where().defaultAnd();
        whereBuilder.issueType(issueType.getString("id"));
        whereBuilder.project(getProjectId());
        final List<String> statuses = new ArrayList<String>();
        for (final Iterator it = migrationHelper.getStatusesNeedingMigration(issueType).iterator(); it.hasNext();)
        {
            final GenericValue status = (GenericValue) it.next();
            statuses.add(status.getString("id"));
        }
        if (!statuses.isEmpty())
        {
            whereBuilder.status().inStrings(statuses);
        }
        return searchProvider.searchCountOverrideSecurity(queryBuilder.buildQuery(), getRemoteUser());
    }

    public long getTotalAffectedIssues(final GenericValue issueType) throws SearchException
    {
        final JqlClauseBuilder queryBuilder = JqlQueryBuilder.newBuilder().where();
        queryBuilder.issueType(issueType.getString("id"));
        queryBuilder.and().project(getProjectId());
        return searchProvider.searchCountOverrideSecurity(queryBuilder.buildQuery(), getRemoteUser());
    }

    public JiraWorkflow getTargetWorkflow(final GenericValue issueType) throws GenericEntityException, WorkflowException
    {
        return workflowManager.getWorkflowFromScheme(getScheme(), issueType.getString("id"));
    }

    public JiraWorkflow getExistingWorkflow(final GenericValue issueType) throws GenericEntityException, WorkflowException
    {
        return workflowManager.getWorkflow(getProject().getLong("id"), issueType.getString("id"));
    }

    public Collection getTargetStatuses(final GenericValue issueType) throws WorkflowException, GenericEntityException
    {
        return getTargetWorkflow(issueType).getLinkedStatuses();
    }

    public String getSelectListName(final GenericValue issueType, final GenericValue status)
    {
        return "mapping_" + issueType.getString("id") + "_" + status.getString("id");
    }

    public boolean isHaveIssuesToMigrate() throws GenericEntityException
    {
        if (haveIssuesToMigrate == null)
        {
            OfBizListIterator issueIterator = null;
            try
            {
                issueIterator = delegator.findListIteratorByCondition("Issue", new EntityFieldMap(EasyMap.build("project", getProjectId()),
                    EntityOperator.AND));
                haveIssuesToMigrate = (issueIterator.next() != null) ? Boolean.TRUE : Boolean.FALSE;
            }
            finally
            {
                if (issueIterator != null)
                {
                    issueIterator.close();
                }
            }
        }

        return haveIssuesToMigrate.booleanValue();
    }

    public Collection getStatusesNeedingMigration(final GenericValue issueType)
    {
        final List statuses = new ArrayList(migrationHelper.getStatusesNeedingMigration(issueType));
        Collections.sort(statuses, new OFBizFieldComparator("sequence"));
        return statuses;
    }

    public static String getAbortedMigrationMessageKey()
    {
        return ABORTED_MIGRATION_MESSAGE_KEY;
    }

    public static String getFailureMigrationMessageKey()
    {
        return FAILURE_MIGRATION_MESSAGE_KEY;
    }

    private String getSchemeName() throws GenericEntityException
    {
        final GenericValue scheme = getScheme();
        if (scheme == null)
        {
            return getText("admin.common.words.default");
        }
        else
        {
            return scheme.getString("name");
        }
    }
}
