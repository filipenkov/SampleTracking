package com.atlassian.jira.workflow.migration;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueVerifier;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.task.StatefulTaskProgressSink;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationResult;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationSuccess;
import com.atlassian.jira.web.action.admin.workflow.WorkflowMigrationTerminated;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.util.profiling.UtilTimerStack;
import com.google.common.collect.ImmutableMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RejectedExecutionException;

import static java.util.Arrays.asList;

public class WorkflowMigrationHelper
{
    private static final Logger log = Logger.getLogger(WorkflowMigrationHelper.class);

    private final GenericValue project;
    private final GenericValue targetScheme;
    private final WorkflowManager workflowManager;
    private final SchemeManager schemeManager;
    private final List<GenericValue> typesNeedingMigration;
    private final Map<GenericValue, Collection<GenericValue>> statusesNeedingMigration;
    private final WorkflowMigrationMapping workflowMigrationMapping;
    private final String projectName;
    private final Long projectId;
    private final OfBizDelegator delegator;
    private final User user;
    private final ConstantsManager constantsManager;
    private final I18nHelper i18nHelper;
    private final Long schemeId;
    private final String schemeName;
    private final TaskManager taskManager;
    private final IssueIndexManager issueIndexManager;

    WorkflowMigrationHelper(GenericValue project, GenericValue targetScheme, WorkflowManager workflowManager, OfBizDelegator delegator,
            SchemeManager schemeManager, I18nHelper i18nHelper, User user, ConstantsManager constantsManager, TaskManager taskManager, IssueIndexManager issueIndexManager)
            throws WorkflowException, GenericEntityException
    {
        this.project = project;
        this.targetScheme = targetScheme;
        this.workflowManager = workflowManager;
        this.delegator = delegator;
        this.schemeManager = schemeManager;
        this.i18nHelper = i18nHelper;
        this.user = user;
        this.taskManager = taskManager;
        this.issueIndexManager = issueIndexManager;
        this.workflowMigrationMapping = new WorkflowMigrationMapping();
        this.constantsManager = constantsManager;
        this.typesNeedingMigration = new ArrayList<GenericValue>();
        this.statusesNeedingMigration = new HashMap<GenericValue, Collection<GenericValue>>();

        this.projectName = project.getString("name");
        this.projectId = project.getLong("id");

        if (targetScheme == null)
        {
            schemeId = null;
            schemeName = i18nHelper.getText("admin.common.words.default");
        }
        else
        {
            schemeId =  targetScheme.getLong("id");
            schemeName = targetScheme.getString("name");
        }
        calculateInputRequired();
    }

    private void calculateInputRequired() throws WorkflowException, GenericEntityException
    {
        List<GenericValue> issueTypes = getConstantsManager().getAllIssueTypes();

        for (GenericValue issueType : issueTypes)
        {
            String issueTypeId = issueType.getString("id");
            JiraWorkflow existingWorkflow = getExistingWorkflow(issueTypeId);
            JiraWorkflow targetWorkflow = getTargetWorkflow(issueTypeId);

            boolean needMigration = false;
            // Check if the source workflow is the same as the destination one
            if (existingWorkflow.equals(targetWorkflow))
            {
                // If the workflows are the same, then we need to find out if we have any issues on the 'wrong' workflow for this issue type
                Collection<Long> issueIdsOnWrongWorkflow = getIssueIdsOnWrongWorkflow(issueTypeId, existingWorkflow.getName());
                if (issueIdsOnWrongWorkflow != null && !issueIdsOnWrongWorkflow.isEmpty())
                {
                    // If we do have issues on the wrong workflow then we need to migrate these issues
                    needMigration = true;
                    // Record the issue ids of issues that are on wrong workflow so that we know that they
                    // need migration even though the workflow they are supposed to be on is the same as the
                    // destination workflow
                    addIssueIdsOnWrongWorkflow(issueIdsOnWrongWorkflow);
                }
            }
            else
            {
                // If not, we definitely need to migrate issues for this issue type
                needMigration = true;
            }

            // We need to ask the user to provide a mapping for statuses for the given issue type if:
            // 1. The source and destination workflows are different
            // 2. We have issues on the 'wrong' workflow to what they should be.
            if (needMigration)
            {
                Collection<GenericValue> existingStatuses = new HashSet<GenericValue>(existingWorkflow.getLinkedStatuses());

                // Get a collection of statuses for which we have issues of type issueType
                // This is needed in case we have inconsistent data, where we have issues in statuses
                // that do not exist in workflow the issues should be on
                Collection<GenericValue> actualExistingStatuses = getUniqueStatusesForIssueType(issueTypeId);
                existingStatuses.addAll(actualExistingStatuses);

                List<GenericValue> targetStatuses = targetWorkflow.getLinkedStatuses();
                existingStatuses.removeAll(targetStatuses);

                if (existingStatuses.size() > 0)
                {
                    typesNeedingMigration.add(issueType);
                    statusesNeedingMigration.put(issueType, existingStatuses);
                }

                // find out intersection of statuses and add mappings
                Collection<GenericValue> intersection = new HashSet<GenericValue>(existingWorkflow.getLinkedStatuses());
                intersection.addAll(actualExistingStatuses);

                intersection.retainAll(targetStatuses);

                for (GenericValue status : intersection)
                {
                    addMapping(issueType, status, status);
                }
            }
        }
    }

    private Collection<GenericValue> getUniqueStatusesForIssueType(String issueTypeId) throws GenericEntityException
    {
        if (issueTypeId == null)
        {
            throw new NullPointerException("Issue Type should not be null.");
        }

        return getUniqueStatuses(new EntityFieldMap(ImmutableMap.of("project", projectId, "type", issueTypeId), EntityOperator.AND));
    }

    private void addIssueIdsOnWrongWorkflow(Collection<Long> issueIdsOnWrongWorkflow)
    {
        if (issueIdsOnWrongWorkflow != null)
        {
            this.workflowMigrationMapping.addIssueIdsOnWorongWorkflow(issueIdsOnWrongWorkflow);
        }
    }

    /**
     * Retrieves issue ids of any issues that are on the 'wrong' workflow for the given issue type. The issues could
     * be on a wrong issue type due to a previously failed workflow migration for the project.
     *
     * @param issueTypeId          the issue type the issue's of which should be checked
     * @param expectedWorkflowName the name of the workflow we expect the issues to be on
     * @return a {@link Collection} of issue ids ({@link Long}s) of any issues in project {@link #project} with given issueTypeId that are not using
     *         workflow with expectedWorkflowName. Remember, the name of a workflow is it unique identifier. If no issues are found
     *         an empty collection is returned.
     * @throws GenericEntityException if there is problem querying the database.
     */
    private Collection<Long> getIssueIdsOnWrongWorkflow(String issueTypeId, String expectedWorkflowName)
            throws GenericEntityException
    {
        if (issueTypeId == null)
        {
            throw new IllegalArgumentException("Issue Type id should not be null.");
        }

        List<Long> issueIds = new ArrayList<Long>();
        OfBizListIterator listIterator = null;

        try
        {
            EntityCondition projectIssueTypeClause = new EntityFieldMap(ImmutableMap.of("issueProject", projectId, "issueType", issueTypeId), EntityOperator.AND);
            EntityCondition workflowClause = new EntityExpr("workflowName", EntityOperator.NOT_EQUAL, expectedWorkflowName);

            EntityCondition condition = new EntityExpr(projectIssueTypeClause, EntityOperator.AND, workflowClause);

            listIterator = getDelegator().findListIteratorByCondition("IssueWorkflowEntryView", condition, null, asList("issueId"), null, null);
            GenericValue issueIdGV = listIterator.next();
            while (issueIdGV != null)
            {
                issueIds.add(issueIdGV.getLong("issueId"));
                // See if we have another record
                issueIdGV = listIterator.next();
            }
        }
        finally
        {
            if (listIterator != null)
            {
                listIterator.close();
            }
        }

        return Collections.unmodifiableList(issueIds);
    }


    /** Gets non-null workflow for an issue type in the current project. */
    private JiraWorkflow getExistingWorkflow(String issueTypeId) throws WorkflowException
    {
        JiraWorkflow workflow = workflowManager.getWorkflow(projectId, issueTypeId);
        if (workflow == null)
        {
            throw new WorkflowException("Could not find workflow associated with project '" + projectName + "', issuetype " + issueTypeId);
        }
        return workflow;
    }

    /** Gets non-null target workflow for an issue type in the current project. */
    private JiraWorkflow getTargetWorkflow(String issueTypeId) throws WorkflowException
    {
        JiraWorkflow workflow = workflowManager.getWorkflowFromScheme(targetScheme, issueTypeId);
        if (workflow == null)
        {
            throw new WorkflowException("Could not find workflow associated with project '" + projectName + "', issuetype " + issueTypeId);
        }
        return workflow;
    }

    public List<GenericValue> getTypesNeedingMigration()
    {
        return typesNeedingMigration;
    }

    public Collection<GenericValue> getStatusesNeedingMigration(GenericValue issueType)
    {
        return statusesNeedingMigration.get(issueType);
    }

    // Returns a collection of errors associated with issues in the workflow migration
    WorkflowMigrationResult migrate(TaskProgressSink sink) throws GenericEntityException, WorkflowException
    {
        UtilTimerStack.push("EnterpriseWorkflowMigrationHelper.migrate");
        log.info("Started workflow migration for project '" + projectName + "'.");

        if (sink == null)
        {
            sink = TaskProgressSink.NULL_SINK;
        }

        StatefulTaskProgressSink migrationSink = new StatefulTaskProgressSink(0, 100, sink);
        String currentSubTask = getI18nHelper().getText("admin.selectworkflowscheme.subtask.verification");

        log.info("Verifying issues can be moved to another workflow for project '" + projectName + "'.");
        migrationSink.makeProgress(0, currentSubTask, getI18nHelper().getText("admin.selectworkflowscheme.progress.find.affected.issues", projectName));

        try
        {
            UtilTimerStack.push("Verifying Issues can be moved to another workflow");

            IssueVerifier issueVerifier = new IssueVerifier();
            ErrorCollection errorCollection = new SimpleErrorCollection();

            Collection<Long> issueIds;
            try
            {
                // Collect issue ids for issues to verify before migration
                issueIds = getIssueIds(projectId);
            }
            catch (Exception e)
            {
                log.error("Error occurred while validating issues for workflow migration on project '" + projectName + "'.", e);
                // This should not really occur - but lets handle it the best we can
                errorCollection.addErrorMessage("Error occurred while retrieving issues for verifying for workflow migration: " + e.getMessage());
                // Pop the stack before returning
                UtilTimerStack.pop("Verifying Issues can be moved to another workflow");

                return new WorkflowMigrationTerminated(errorCollection);
            }

            migrationSink.makeProgressIncrement(5, currentSubTask,
                    getI18nHelper().getText("admin.selectworkflowscheme.progress.found.affected.issues", projectName));

            try
            {
                final int numberOfIssues = issueIds.size();

                TaskProgressSink issueSink = migrationSink.createStepSinkView(6, 40, numberOfIssues);

                // Loop through issue ids, retrieve one issue at a time and verify it
                int issueCounter = 1;
                for (Long issueId : issueIds)
                {
                    issueSink.makeProgress(issueCounter, currentSubTask,
                            getI18nHelper().getText("admin.selectworkflowscheme.progress.verify.issue", String.valueOf(issueCounter), String.valueOf(numberOfIssues), projectName));


                    GenericValue issueGV = retrieveIssue(issueId);
                    if (issueGV != null)
                    {
                        // Do not check the current workflow integrity. Even if issues are in 'bad' workflow state we should migrate them to new
                        // workflow and fix any problems.
                        ErrorCollection possibleErrors = issueVerifier.verifyForMigration(issueGV, typesNeedingMigration, workflowMigrationMapping, false);
                        errorCollection.addErrorCollection(possibleErrors);
                    }
                    else
                    {
                        log.debug("Issue with id '" + issueId + "' not found.");
                    }
                    issueCounter++;
                }

                if (errorCollection != null && errorCollection.hasAnyErrors())
                {
                    log.info("Enterprise workflow migration failed with invalid issues for project '" + projectName + "'.");
                    return new WorkflowMigrationTerminated(errorCollection);
                }
            }
            catch (Exception e)
            {
                log.error("Error occurred while verifying issues for workflow migration on project '" + projectName + "'.", e);
                errorCollection.addErrorMessage(getI18nHelper().getText("admin.errors.workflows.error.occurred.verifying.issues", e.getMessage()));
                return new WorkflowMigrationTerminated(errorCollection);
            }
            finally
            {
                UtilTimerStack.pop("Verifying Issues can be moved to another workflow");
            }
            currentSubTask = getI18nHelper().getText("admin.selectworkflowscheme.subtask.migration");
            migrationSink.makeProgress(47, currentSubTask, getI18nHelper().getText("admin.selectworkflowscheme.progress.find.affected.issues", projectName));

            UtilTimerStack.push("Refinding issues for new workflow");
            try
            {
                // Collect issue ids for issues to migrate. Need to pull out the list again so that if any issues
                // were created in the mean time we find them.
                issueIds = getIssueIds(projectId);
            }
            catch (Exception e)
            {
                log.error("Error occurred while retrieving issues for workflow migration of project '" + projectName + "'.", e);
                // This should not really occur - but lets handle it the best we can
                errorCollection.addErrorMessage("Error occurred while retrieving issues for workflow migration. " + e.getMessage());
                // Pop the stack before returning

                return new WorkflowMigrationTerminated(errorCollection);
            }
            finally
            {
                UtilTimerStack.pop("Refinding issues for new workflow");
            }

            migrationSink.makeProgressIncrement(5, currentSubTask,
                    getI18nHelper().getText("admin.selectworkflowscheme.progress.found.affected.issues", projectName));

            WorkflowMigrationResult result = migrateIssues(issueIds, migrationSink);

            currentSubTask = getI18nHelper().getText("admin.selectworkflowscheme.subtask.association");

            log.info("Assigning workflow scheme to project '" + projectName + "'.");
            migrationSink.makeProgress(94, currentSubTask,
                    getI18nHelper().getText("admin.selectworkflowscheme.progress.assign.workflow", getTargetSchemeName(), projectName));

            if (result.getResult() == WorkflowMigrationResult.SUCCESS)
            {
                associateProjectAndWorkflowScheme(schemeManager, project, targetScheme);
            }

            log.info("Workflow migration complete for project '" + projectName + "'.");
            migrationSink.makeProgress(100, null,
                    getI18nHelper().getText("admin.selectworkflowscheme.progress.complete"));

            return result;
        }
        finally
        {
            UtilTimerStack.pop("EnterpriseWorkflowMigrationHelper.migrate");
        }
    }

    private WorkflowMigrationResult migrateIssues(Collection issueIds, StatefulTaskProgressSink percentageSink)
            throws GenericEntityException
    {
        UtilTimerStack.push("Moving issues to new workflow");
        // Contains issue id to issue key mapping of issues that have failed the migration
        Map<Long, String> failedIssues = new HashMap<Long, String>();

        log.info("Migrating issues in project '" + projectName + "' to new workflow.");

        try
        {
            final int numberOfIssues = issueIds.size();
            long issueCounter = 1;
            TaskProgressSink issueSink = percentageSink.createStepSinkView(53, 40, numberOfIssues);

            final String currentSubTask = getI18nHelper().getText("admin.selectworkflowscheme.subtask.migration");

            // Retrieve one issue at a time and migrate it to new workflow
            for (Iterator iterator = issueIds.iterator(); iterator.hasNext(); issueCounter++)
            {
                issueSink.makeProgress(issueCounter, currentSubTask,
                        getI18nHelper().getText("admin.selectworkflowscheme.progress.migrate.issue", String.valueOf(issueCounter), String.valueOf(numberOfIssues), projectName));

                Long issueId = (Long) iterator.next();
                GenericValue issueGV = retrieveIssue(issueId);

                if (issueGV != null)
                {
                    try
                    {
                        GenericValue currentIssueType = getConstantsManager().getIssueType(issueGV.getString("type"));

                        // Get details for changelog
                        JiraWorkflow originalWorkflow = workflowManager.getWorkflow(issueGV);

                        // Note that if we tried to migrate earlier and something went wrong, this status and wfId may not exist in originalWorkflow!
                        GenericValue originalStatus = getConstantsManager().getStatus(issueGV.getString("status"));
                        String originalWfIdString = issueGV.getLong("workflowId").toString();
                        GenericValue targetStatus;

                        // Mappings exist only for types that require migration
                        // Other types retain their current status
                        if (typesNeedingMigration.contains(currentIssueType))
                        {
                            // For each issue look up the target status using current issue type and CURRENT status in the mapping
                            targetStatus = workflowMigrationMapping.getTargetStatus(issueGV);
                        }
                        else
                        {
                            targetStatus = getConstantsManager().getStatus(issueGV.getString("status"));
                        }

                        // Go for it
                        String issueTypeId = issueGV.getString("type");
                        JiraWorkflow targetWorkflow = getTargetWorkflow(issueTypeId);

                        // Migrate the issue to new workflow if:
                        // 1. If the issue is one of the 'bad' issues - issue which is on the workflow which it should not
                        // be on. Issues get into this state most often due to a previous failed workflow migration.
                        // 2. The source workflow is different to the destination workflow
                        boolean isIssueOnWrongWorkflow = workflowMigrationMapping.isIssueOnWrongWorkflow(issueGV.getLong("id"));
                        if (isIssueOnWrongWorkflow || !targetWorkflow.equals(getExistingWorkflow(issueTypeId)))
                        {
                            // Start transaction before transitioning an issue
                            Transaction txn = Txn.begin();
                            try
                            {
                                // Disable indexing so that no indexing occurrs inside a transaction
                                // We will reindex the issue once the migration of the issue finishes
                                issueIndexManager.hold();

                                final boolean needsIndex = workflowManager.migrateIssueToWorkflowNoReindex(issueGV, targetWorkflow, targetStatus);
                                createChangeLog(issueGV, originalWfIdString, originalStatus, originalWorkflow, targetWorkflow, targetStatus);
                                // Commit changes for the issue
                                txn.commit();

                                //We only need to do a reindex if the WF migration changed the issue. The issue is
                                //always technically changed as the wfId is updated, however, we don't index this value
                                //and as such we don't have to do a re-index. We also always add change items to the issue,
                                //but again we don't index these types of change items.
                                if (needsIndex)
                                {
                                    reindexIssue(issueGV);
                                }
                            }
                            catch (Exception e)
                            {
                                // Roll back changes for the issue and throw an Exception
                                txn.rollback();
                                // Rethrow the exception for further handling.
                                throw e;
                            }
                            finally
                            {
                                issueIndexManager.release();
                            }
                        }
                    }
                    catch (Exception e)
                    {
                        log.error("Error occurred while migrating issue to a new workflow for project '" + projectName + "'.", e);

                        // Record that an issue failed and see if we should proceed
                        failedIssues.put(issueId, issueGV.getString("key"));

                        // Test if we have failed too many times
                        if (failedIssues.size() >= 10)
                        {
                            log.info("Enterprise workflow migration cancelled due to number of errors during issues migration for project '" + projectName + "'.");

                            // If too many issues have failed then return
                            return new WorkflowMigrationTerminated(failedIssues);
                        }
                    }
                }
                else
                {
                    log.debug("Issue with id '" + issueId + "' not found.");
                }
            }
        }
        finally
        {
            UtilTimerStack.pop("Moving issues to new workflow");
        }
        return new WorkflowMigrationSuccess(failedIssues);
    }


    private void reindexIssue(GenericValue issueGV)
    {
        String issueKey = issueGV.getString("key");
        try
        {
            // Reindex issue
            UtilTimerStack.push("Reindexing issue: " + issueKey);
            ComponentAccessor.getIssueIndexManager().reIndex(issueGV);
            UtilTimerStack.pop("Reindexing issue: " + issueKey);
        }
        catch (IndexException e)
        {
            log.error("Error occurred while reindexing issue: " + issueKey, e);
        }
    }

    // Create change log for changes made to issue during migration
    private void createChangeLog(GenericValue issue, String originalWfIdString, GenericValue originalStatus, JiraWorkflow originalWorkflow, JiraWorkflow targetWorkflow, GenericValue targetStatus)
    {
        String newwfIdString = issue.getLong("workflowId").toString();
        List<ChangeItemBean> changeItems = new ArrayList<ChangeItemBean>();

        boolean createChangeLog = false;

        // Record the changes
        if (!originalWfIdString.equals(newwfIdString))
        {
            changeItems.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "Workflow", originalWfIdString, originalWorkflow.getName(), newwfIdString, targetWorkflow.getName()));
            createChangeLog = true;
        }

        if (!originalStatus.getString("id").equals(targetStatus.getString("id")))
        {
            changeItems.add(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, "status", originalStatus.getString("id"), originalStatus.getString("name"), targetStatus.getString("id"), targetStatus.getString("name")));
            createChangeLog = true;
        }

        try
        {
            // Only create the change log if there have been changes
            if (createChangeLog)
            {
                ChangeLogUtils.createChangeGroup(getUser(), issue, issue, changeItems, true);
            }
        }
        catch (Exception e)
        {
            log.error("Error occurred creating change log: " + e, e);
        }
    }

    public void addMapping(GenericValue issueType, GenericValue oldStatus, GenericValue newStatus)
    {
        workflowMigrationMapping.addMapping(issueType, oldStatus, newStatus);
    }

    public void associateProjectAndWorkflowScheme(SchemeManager schemeManager, GenericValue project,
            GenericValue scheme) throws GenericEntityException
    {
        if (schemeManager == null)
        {
            throw new NullPointerException();
        }
        if (project == null)
        {
            throw new NullPointerException();
        }

        // need to find the set of workflows that may need their draft workflows to be copied and deleted, since
        // the parent workflow may no longer be active.
        final List<GenericValue> workflowSchemes = schemeManager.getSchemes(project);
        final Set<JiraWorkflow> workflowsFromOldScheme = new HashSet<JiraWorkflow>();
        for (GenericValue workflowGV : workflowSchemes)
        {
            workflowsFromOldScheme.addAll(workflowManager.getWorkflowsFromScheme(workflowGV));
        }

        schemeManager.removeSchemesFromProject(project);

        // Check if associating with none - the default workflow
        if (scheme != null)
        {
            schemeManager.addSchemeToProject(project, scheme);
        }

        // Clear the active workflow name cache
        ComponentAccessor.getWorkflowSchemeManager().clearWorkflowCache();

        // note that order here is important.  This step needs to happen *after* the workflowSchemeManager cache is
        // flushed otherwise isActive() may return the wrong result for the workflows passed in.
        workflowManager.copyAndDeleteDraftWorkflows(getUser(), workflowsFromOldScheme);
    }


    public Logger getLogger()
    {
        return WorkflowMigrationHelper.log;
    }

    private String getTargetSchemeName()
    {
        GenericValue scheme = targetScheme;
        if (scheme == null)
        {
            return getI18nHelper().getText("admin.common.words.default");
        }
        else
        {
            return scheme.getString("name");
        }
    }

    private OfBizDelegator getDelegator()
    {
        return delegator;
    }

    private ConstantsManager getConstantsManager()
    {
        return constantsManager;
    }

    private User getUser()
    {
        return user;
    }

    private I18nHelper getI18nHelper()
    {
        return i18nHelper;
    }

    private Collection<Long> getIssueIds(Long projectId) throws GenericEntityException
    {
        getLogger().debug("Returning all issues associated with project.");

        // JRA-6987 - do not retrieve all issues at once - use ofbiz iterator to iterate over each issue id
        OfBizListIterator issueIterator = null;

        Collection<Long> issueIds = new ArrayList<Long>();

        try
        {
            final EntityFieldMap cond = new EntityFieldMap(ImmutableMap.of("project", projectId), EntityOperator.AND);
            issueIterator = delegator.findListIteratorByCondition("Issue", cond, null, asList("id"), null, null);
            GenericValue issueIdGV = issueIterator.next();
            // As documented in org.ofbiz.core.entity.EntityListIterator.hasNext() the best way to find out
            // if there are any results left in the iterator is to iterate over it until null is returned
            // (i.e. not use hasNext() method)
            // The documentation mentions efficiency only - but the functionality is totally broken when using
            // hsqldb JDBC drivers (hasNext() always returns true).
            // So listen to the OfBiz folk and iterate until null is returned.
            while (issueIdGV != null)
            {
                // record the issue id
                issueIds.add(issueIdGV.getLong("id"));
                // See if we have another issue
                issueIdGV = issueIterator.next();
            }
        }
        finally
        {
            if (issueIterator != null)
            {
                issueIterator.close();
            }
        }

        return issueIds;
    }

    private GenericValue retrieveIssue(Long issueId) throws GenericEntityException
    {
        return delegator.findById("Issue", issueId);
    }

    /**
     * AbstractWorkflowMigrationHelper
     * Retrieves a collection of unique status GenericValues for which issues exist given a EntityCondition that
     * will be used as the SQL WHERE clause against the Issue table.
     */
    private Collection<GenericValue> getUniqueStatuses(EntityCondition condition) throws GenericEntityException
    {
        Collection<GenericValue> foundStatuses = new ArrayList<GenericValue>();

        OfBizListIterator listIterator = null;

        try
        {
            // SELECT DISTINCT status FROM jiraissue WHERE project = projectId AND type = issueTypeId
            EntityFindOptions findOptions = new EntityFindOptions();
            findOptions.setDistinct(true);
            listIterator = getDelegator().findListIteratorByCondition("Issue", condition, null, asList(IssueFieldConstants.STATUS), null, findOptions);
            GenericValue statusIdGV = listIterator.next();
            while (statusIdGV != null)
            {
                GenericValue statusGV = constantsManager.getStatus(statusIdGV.getString("status"));
                // If the issue status does not exist or is null - do not include the status here
                // The IssueVerifier should catch it down the line and not allow the issues to be migrated to new worfklow
                if (statusGV != null)
                {
                    foundStatuses.add(statusGV);
                }
                else
                {
                    // Print out a message here. It might help solve a few support cases.
                    getLogger().debug("Found issue with status id '" + statusIdGV.getString("status") + "'. The status for this id does not exist.");
                }

                // See if we have another status
                statusIdGV = listIterator.next();
            }
        }
        finally
        {
            if (listIterator != null)
            {
                listIterator.close();
            }
        }

        return foundStatuses;
    }

    public boolean isHaveIssuesToMigrate() throws GenericEntityException
    {
        OfBizListIterator issueIterator = null;
        try
        {
            issueIterator = delegator.findListIteratorByCondition("Issue", new EntityFieldMap(ImmutableMap.of("project", projectId),
                    EntityOperator.AND));
            return (issueIterator.next() != null);
        }
        finally
        {
            if (issueIterator != null)
            {
                issueIterator.close();
            }
        }
    }

    public boolean doQuickMigrate() throws GenericEntityException
    {
        if (isHaveIssuesToMigrate())
        {
            return false;
        }
        else
        {
            associateProjectAndWorkflowScheme(schemeManager, project, targetScheme);
            return true;
        }
    }

    public TaskDescriptor<WorkflowMigrationResult> migrateAsync() throws RejectedExecutionException
    {
        final String taskDesc = i18nHelper.getText("admin.selectworkflows.task.desc", projectName, schemeName);
        final EnterpriseWorkflowTaskContext taskContext = new EnterpriseWorkflowTaskContext(projectId, schemeId);

        return taskManager.submitTask(new WorkflowAsynchMigrator(this), taskDesc, taskContext);
    }
}
