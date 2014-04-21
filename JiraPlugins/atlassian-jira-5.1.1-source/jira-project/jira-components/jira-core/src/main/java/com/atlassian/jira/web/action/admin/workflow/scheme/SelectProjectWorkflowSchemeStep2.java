/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Apr 2, 2004
 * Time: 12:19:05 PM
 */
package com.atlassian.jira.web.action.admin.workflow.scheme;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.ofbiz.OfBizStringFieldComparator;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.jira.workflow.WorkflowSchemeManager;
import com.atlassian.jira.workflow.migration.MigrationHelperFactory;
import com.atlassian.jira.workflow.migration.WorkflowMigrationHelper;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

@WebSudoRequired
public class SelectProjectWorkflowSchemeStep2 extends SelectProjectWorkflowScheme
{
    private WorkflowMigrationHelper migrationHelper;
    private final TaskManager taskManager;
    private final WorkflowManager workflowManager;
    private Boolean haveIssuesToMigrate;
    private final Map failedIssueIds;
    private final SearchProvider searchProvider;
    private final static String ABORTED_MIGRATION_MESSAGE_KEY = "admin.workflowmigration.aborted.defaultworkflow";
    private static final String FAILURE_MIGRATION_MESSAGE_KEY = "admin.workflowmigration.withfailure.defaultworkflow";
    private final ConstantsManager constantsManager;
    private final WorkflowSchemeManager workflowSchemeManager;
    private final MigrationHelperFactory migrationHelperFactory;

    public SelectProjectWorkflowSchemeStep2(final SearchProvider searchProvider, final TaskManager taskManager,
            final JiraAuthenticationContext authenticationContext, final OutlookDateManager outlookDateManager,
            final WorkflowManager workflowManager, final ConstantsManager constantsManager, final WorkflowSchemeManager workflowSchemeManager, final MigrationHelperFactory migrationHelperFactory)
    {
        super(taskManager, authenticationContext, outlookDateManager);
        this.searchProvider = searchProvider;
        this.taskManager = taskManager;
        this.workflowManager = workflowManager;
        this.constantsManager = constantsManager;
        this.workflowSchemeManager = workflowSchemeManager;
        this.migrationHelperFactory = migrationHelperFactory;
        this.failedIssueIds = new HashMap();
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

        migrationHelper = migrationHelperFactory.createMigrationHelper(getProject(), getScheme());

        return super.doDefault();
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        migrationHelper = migrationHelperFactory.createMigrationHelper(getProject(), getScheme());

        if (migrationHelper.doQuickMigrate())
        {
            return redirectUser();
        }
        else
        {
            // setup manual migrations
            addMigrationMappings();

            if (invalidInput())
            {
                return getResult();
            }

            try
            {
                return getRedirect(getMigrationHelper().migrateAsync().getProgressURL());
            }
            catch (final RejectedExecutionException e)
            {
                return ERROR;
            }
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

        for (final GenericValue issueType : migrationHelper.getTypesNeedingMigration())
        {
            for (final GenericValue status : migrationHelper.getStatusesNeedingMigration(issueType))
            {
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

    public WorkflowMigrationHelper getMigrationHelper()
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
        for (final GenericValue status : migrationHelper.getStatusesNeedingMigration(issueType))
        {
            statuses.add(status.getString("id"));
        }
        if (!statuses.isEmpty())
        {
            whereBuilder.status().inStrings(statuses);
        }
        return searchProvider.searchCountOverrideSecurity(queryBuilder.buildQuery(), getLoggedInUser());
    }

    public long getTotalAffectedIssues(final GenericValue issueType) throws SearchException
    {
        final JqlClauseBuilder queryBuilder = JqlQueryBuilder.newBuilder().where();
        queryBuilder.issueType(issueType.getString("id"));
        queryBuilder.and().project(getProjectId());
        return searchProvider.searchCountOverrideSecurity(queryBuilder.buildQuery(), getLoggedInUser());
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
            haveIssuesToMigrate = migrationHelper.isHaveIssuesToMigrate();
        }

        return haveIssuesToMigrate;
    }

    public Collection getStatusesNeedingMigration(final GenericValue issueType)
    {
        final List<GenericValue> statuses = new ArrayList<GenericValue>(migrationHelper.getStatusesNeedingMigration(issueType));
        Collections.sort(statuses, new OfBizStringFieldComparator("sequence"));
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
