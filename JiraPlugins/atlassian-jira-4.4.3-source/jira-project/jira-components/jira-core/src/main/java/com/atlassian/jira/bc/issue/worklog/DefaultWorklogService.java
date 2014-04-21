package com.atlassian.jira.bc.issue.worklog;

import com.atlassian.core.util.InvalidDurationException;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.util.VisibilityValidator;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.worklog.Worklog;
import com.atlassian.jira.issue.worklog.WorklogImpl;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Default implemenation of the WorklogService.
 */
public class DefaultWorklogService implements WorklogService
{
    private static final Logger log = Logger.getLogger(DefaultWorklogService.class);

    private final WorklogManager worklogManager;
    private final PermissionManager permissionManager;
    private final VisibilityValidator visibilityValidator;
    private final ProjectRoleManager projectRoleManager;
    private final IssueManager issueManager;
    private final TimeTrackingConfiguration timeTrackingConfiguration;
    private final JiraDurationUtils jiraDurationUtils;

    private static final String WORKLOG_I18N_PREFIX = "worklog";

    public DefaultWorklogService(WorklogManager worklogManager, PermissionManager permissionManager,
            VisibilityValidator visibilityValidator, ProjectRoleManager projectRoleManager, IssueManager issueManager,
            final TimeTrackingConfiguration timeTrackingConfiguration, JiraDurationUtils jiraDurationUtils)
    {
        this.worklogManager = worklogManager;
        this.permissionManager = permissionManager;
        this.visibilityValidator = visibilityValidator;
        this.projectRoleManager = projectRoleManager;
        this.issueManager = issueManager;
        this.timeTrackingConfiguration = timeTrackingConfiguration;
        this.jiraDurationUtils = jiraDurationUtils;
    }

    public WorklogResult validateDelete(JiraServiceContext jiraServiceContext, Long worklogId)
    {
        Worklog originalWorklog = worklogManager.getById(worklogId);

        // Does the user have permission to delete a worklog
        if (hasPermissionToDelete(jiraServiceContext, originalWorklog))
        {
            return WorklogResultFactory.create(originalWorklog);
        }
        return null;
    }

    public WorklogNewEstimateResult validateDeleteWithNewEstimate(JiraServiceContext jiraServiceContext, Long worklogId, String newEstimate)
    {
        WorklogResult originalWorklogResult = validateDelete(jiraServiceContext, worklogId);
        if(originalWorklogResult != null && isValidNewEstimate(jiraServiceContext, newEstimate))
        {
            final Long estimate = (newEstimate == null) ? null : getDurationForFormattedString(newEstimate, jiraServiceContext);
            return WorklogResultFactory.createNewEstimate(originalWorklogResult, estimate);
        }
        return null;
    }

    public WorklogAdjustmentAmountResult validateDeleteWithManuallyAdjustedEstimate(JiraServiceContext jiraServiceContext, Long worklogId, String adjustmentAmount)
    {
        WorklogResult originalWorklogResult = validateDelete(jiraServiceContext, worklogId);
        if(originalWorklogResult != null && isValidAdjustmentAmount(jiraServiceContext, adjustmentAmount))
        {
            return WorklogResultFactory.createAdjustmentAmount(originalWorklogResult, getDurationForFormattedString(adjustmentAmount, jiraServiceContext));
        }
        return null;
    }

    public boolean deleteWithNewRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogNewEstimateResult worklogNewEstimate, boolean dispatchEvent)
    {
        return delete(jiraServiceContext, worklogNewEstimate, worklogNewEstimate.getNewEstimate(), dispatchEvent);
    }

    public boolean deleteWithManuallyAdjustedEstimate(JiraServiceContext jiraServiceContext, WorklogAdjustmentAmountResult worklogResult, boolean dispatchEvent)
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        if (worklogResult == null || worklogResult.getWorklog() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
            return false;
        }

        if (worklogResult.getWorklog().getIssue() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
            return false;
        }

        // Calculate the new remaining estimate
        Long newEstimate = increaseEstimate(worklogResult.getWorklog().getIssue(), worklogResult.getAdjustmentAmount());
        return delete(jiraServiceContext, worklogResult, newEstimate, dispatchEvent);
    }

    public boolean deleteAndRetainRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, boolean dispatchEvent)
    {
        return delete(jiraServiceContext, worklogResult, null, dispatchEvent);
    }

    public boolean deleteAndAutoAdjustRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, boolean dispatchEvent)
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        //these null checks are duplicated in update() but must be run before passing worklog to getAutoAdjustNewEstimateOnUpdate()
        if (worklogResult == null || worklogResult.getWorklog() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
            return false;
        }

        if (worklogResult.getWorklog().getIssue() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
            return false;
        }

        if (worklogResult.getWorklog().getId() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.id.null"));
            return false;
        }

        Long timeSpent = worklogResult.getWorklog().getTimeSpent();

        Long newEstimate = increaseEstimate(worklogResult.getWorklog().getIssue(), timeSpent);
        return delete(jiraServiceContext, worklogResult, newEstimate, dispatchEvent);
    }

    public WorklogResult validateUpdate(JiraServiceContext jiraServiceContext, final WorklogInputParameters params)
    {
        notNull("params", params);
        Worklog originalWorklog = worklogManager.getById(params.getWorklogId());
        User user = jiraServiceContext.getUser();

        // Does the user have permission to edit a worklog
        if (hasPermissionToUpdate(jiraServiceContext, originalWorklog))
        {
            String updateAuthor = (user == null) ? null : user.getName();
            Worklog updatedWorklog = validateParamsAndCreateWorklog(jiraServiceContext, originalWorklog.getIssue(),
                    originalWorklog.getAuthor(), params.getGroupLevel(), params.getRoleLevelId(), params.getTimeSpent(),
                    params.getStartDate(), params.getWorklogId(), params.getComment(), originalWorklog.getCreated(),
                    new Date(), updateAuthor, params.getErrorFieldPrefix());
            if (updatedWorklog != null)
            {
                return WorklogResultFactory.create(updatedWorklog);
            }
        }

        return null;
    }

    public WorklogNewEstimateResult validateUpdateWithNewEstimate(JiraServiceContext jiraServiceContext, final WorklogNewEstimateInputParameters params)
    {
        notNull("params", params);
        WorklogResult worklogResult = validateUpdate(jiraServiceContext, params);
        if(isValidNewEstimate(jiraServiceContext, params.getNewEstimate(), params.getErrorFieldPrefix()) && worklogResult != null)
        {
            final Long estimate = (params.getNewEstimate() == null) ? null : getDurationForFormattedString(params.getNewEstimate(), jiraServiceContext);
            return WorklogResultFactory.createNewEstimate(worklogResult, estimate);
        }
        return null;
    }

    public Worklog updateWithNewRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogNewEstimateResult worklogResult, boolean dispatchEvent)
    {
        return update(jiraServiceContext, worklogResult, worklogResult.getNewEstimate(), dispatchEvent);
    }

    public Worklog updateAndRetainRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, boolean dispatchEvent)
    {
        return update(jiraServiceContext, worklogResult, null, dispatchEvent);
    }

    public Worklog updateAndAutoAdjustRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, boolean dispatchEvent)
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        //these null checks are duplicated in update() but must be run before passing worklog to getAutoAdjustNewEstimateOnUpdate()
        if (worklogResult == null || worklogResult.getWorklog() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
            return null;
        }

        if (worklogResult.getWorklog().getIssue() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
            return null;
        }

        if (worklogResult.getWorklog().getId() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.id.null"));
            return null;
        }

        Worklog originalWorklog = worklogManager.getById(worklogResult.getWorklog().getId());

        if (originalWorklog == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.worklog.for.id", worklogResult.getWorklog().getId().toString()));
            return null;
        }

        Long originalTimeSpent = originalWorklog.getTimeSpent();
        Long newTimeSpent = worklogResult.getWorklog().getTimeSpent();

        Long newEstimate = getAutoAdjustNewEstimateOnUpdate(worklogResult.getWorklog().getIssue(), newTimeSpent, originalTimeSpent);
        return update(jiraServiceContext, worklogResult, newEstimate, dispatchEvent);
    }

    protected boolean delete(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, Long newEstimate, boolean dispatchEvent)
    {
        User user = jiraServiceContext.getUser();
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        if (worklogResult == null || worklogResult.getWorklog() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
            return false;
        }

        if (worklogResult.getWorklog().getIssue() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
            return false;
        }

        if (worklogResult.getWorklog().getId() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.id.null"));
            return false;
        }

        // Re-do the permission check
        if (hasPermissionToDelete(jiraServiceContext, worklogResult.getWorklog()))
        {
            return worklogManager.delete(user, worklogResult.getWorklog(), newEstimate, dispatchEvent);
        }

        return false;
    }

    protected Worklog update(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, Long newEstimate, boolean dispatchEvent)
    {
        Worklog updatedWorklog = null;
        User user = jiraServiceContext.getUser();
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        if (worklogResult == null || worklogResult.getWorklog() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
            return null;
        }

        if (worklogResult.getWorklog().getIssue() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
            return null;
        }

        if (worklogResult.getWorklog().getId() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.id.null"));
            return null;
        }

        // Re-do the permission check
        if (hasPermissionToUpdate(jiraServiceContext, worklogResult.getWorklog()))
        {
            updatedWorklog = worklogManager.update(user, worklogResult.getWorklog(), newEstimate, dispatchEvent);
        }

        return updatedWorklog;
    }

    public WorklogResult validateCreate(JiraServiceContext jiraServiceContext, WorklogInputParameters params)
    {
        User user = jiraServiceContext.getUser();

        // Does the user have permission to create a worklog
        if (hasPermissionToCreate(jiraServiceContext, params.getIssue(), params.isEditableCheckRequired()))
        {
            String authorName = (user == null) ? null : user.getName();
            Worklog worklog = validateParamsAndCreateWorklog(jiraServiceContext, params.getIssue(), authorName, params.getGroupLevel(),
                    params.getRoleLevelId(), params.getTimeSpent(), params.getStartDate(), null, params.getComment(), null, null, null, params.getErrorFieldPrefix());
            if (worklog != null)
            {
                return WorklogResultFactory.create(worklog, params.isEditableCheckRequired());
            }
        }
        return null;
    }

    public WorklogNewEstimateResult validateCreateWithNewEstimate(JiraServiceContext jiraServiceContext, WorklogNewEstimateInputParameters params)
    {
        WorklogResult worklogResult = validateCreate(jiraServiceContext, params);
        if (isValidNewEstimate(jiraServiceContext, params.getNewEstimate(), params.getErrorFieldPrefix()) && worklogResult != null)
        {
            final Long estimate = (params.getNewEstimate() == null) ? null : getDurationForFormattedString(params.getNewEstimate(), jiraServiceContext);
            return WorklogResultFactory.createNewEstimate(worklogResult, estimate);
        }
        return null;
    }

    public WorklogAdjustmentAmountResult validateCreateWithManuallyAdjustedEstimate(JiraServiceContext jiraServiceContext, WorklogAdjustmentAmountInputParameters params)
    {
        WorklogResult worklogResult = validateCreate(jiraServiceContext, params);
        if (isValidAdjustmentAmount(jiraServiceContext, params.getAdjustmentAmount(), params.getErrorFieldPrefix()) && worklogResult != null)
        {
            return WorklogResultFactory.createAdjustmentAmount(worklogResult, getDurationForFormattedString(params.getAdjustmentAmount(), jiraServiceContext));
        }
        return null;
    }

    public Worklog createWithNewRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogNewEstimateResult worklogResult, boolean dispatchEvent)
    {
        return create(jiraServiceContext, worklogResult, worklogResult.getNewEstimate(), dispatchEvent);
    }

    public Worklog createWithManuallyAdjustedEstimate(final JiraServiceContext jiraServiceContext, final WorklogAdjustmentAmountResult worklogResult, final boolean dispatchEvent)
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        if (worklogResult == null || worklogResult.getWorklog() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
            return null;
        }

        if (worklogResult.getWorklog().getIssue() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
            return null;
        }

        // Calculate the new remaining estimate
        final Worklog worklog = worklogResult.getWorklog();
        Long newEstimate = reduceEstimate(worklog.getIssue(), worklogResult.getAdjustmentAmount());
        return create(jiraServiceContext, worklogResult, newEstimate, dispatchEvent);
    }

    public Worklog createAndRetainRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, boolean dispatchEvent)
    {
        return create(jiraServiceContext, worklogResult, null, dispatchEvent);
    }

    public Worklog createAndAutoAdjustRemainingEstimate(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, boolean dispatchEvent)
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        if (worklogResult == null || worklogResult.getWorklog() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
            return null;
        }

        if (worklogResult.getWorklog().getIssue() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
            return null;
        }

        Long newEstimate = reduceEstimate(worklogResult.getWorklog().getIssue(), worklogResult.getWorklog().getTimeSpent());
        return create(jiraServiceContext, worklogResult, newEstimate, dispatchEvent);
    }

    public boolean hasPermissionToCreate(JiraServiceContext jiraServiceContext, Issue issue, final boolean isEditableCheckRequired)
    {
        User user = jiraServiceContext.getUser();
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        if (!isTimeTrackingEnabled())
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.time.tracking.not.enabed"));
            return false;
        }

        if (issue == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
            return false;
        }

        // this flag was added so that we can skip the editable check in order to create worklogs on transitioning
        // to/from "ineditable" issue states.
        if (isEditableCheckRequired && !isIssueInEditableWorkflowState(issue))
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.not.editable.workflow.state"));
            return false;
        }

        boolean hasPerm = permissionManager.hasPermission(Permissions.WORK_ISSUE, issue, user);

        if (!hasPerm)
        {
            if (user != null)
            {
                errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.permission", user.getDisplayName()));
            }
            else
            {
                errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.permission.no.user"));
            }
        }
        return hasPerm;
    }

    public boolean hasPermissionToUpdate(JiraServiceContext jiraServiceContext, Worklog worklog)
    {
        User user = jiraServiceContext.getUser();
        ErrorCollection errorCollection = new SimpleErrorCollection();

        if (!isTimeTrackingEnabled())
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "worklog.service.error.time.tracking.not.enabed"));
            return false;
        }

        validateUpdateOrDeletePermissionCheckParams(worklog, errorCollection, jiraServiceContext);

        if (errorCollection.hasAnyErrors())
        {
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        if (!hasEditAllPermission(user, worklog.getIssue()) && !hasEditOwnPermission(user, worklog))
        {
            if (user != null)
            {
                errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.edit.permission", user.getDisplayName()));
            }
            else
            {
                errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.edit.permission.no.user"));
            }
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        // Do a check to make sure that the user is a member of the role or group if the worklog is protected by
        // any of these visibility levels
        boolean isValidVisibility = visibilityValidator.isValidVisibilityData(new JiraServiceContextImpl(user, errorCollection), WORKLOG_I18N_PREFIX, worklog.getIssue(),
                worklog.getGroupLevel(), (worklog.getRoleLevelId() == null) ? null : worklog.getRoleLevelId().toString());

        if (!isValidVisibility)
        {
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        return true;
    }

    public boolean hasPermissionToDelete(JiraServiceContext jiraServiceContext, Worklog worklog)
    {
        User user = jiraServiceContext.getUser();
        ErrorCollection errorCollection = new SimpleErrorCollection();

        if (!isTimeTrackingEnabled())
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(getText(jiraServiceContext, "worklog.service.error.time.tracking.not.enabed"));
            return false;
        }

        validateUpdateOrDeletePermissionCheckParams(worklog, errorCollection, jiraServiceContext);

        if (errorCollection.hasAnyErrors())
        {
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        if (!hasDeleteAllPermission(user, worklog.getIssue()) && !hasDeleteOwnPermission(user, worklog))
        {
            if (user != null)
            {
                errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.delete.permission", user.getDisplayName()));
            }
            else
            {
                errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.no.delete.permission.no.user"));
            }
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        // Do a check to make sure that the user is a member of the role or group if the worklog is protected by
        // any of these visibility levels
        boolean isValidVisibility = visibilityValidator.isValidVisibilityData(new JiraServiceContextImpl(user, errorCollection), WORKLOG_I18N_PREFIX, worklog.getIssue(),
                worklog.getGroupLevel(), (worklog.getRoleLevelId() == null) ? null : worklog.getRoleLevelId().toString());

        if (!isValidVisibility)
        {
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
            return false;
        }

        return true;
    }

    public Worklog getById(JiraServiceContext jiraServiceContext, Long id)
    {
        if (!isTimeTrackingEnabled())
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(jiraServiceContext.getI18nBean().getText("worklog.service.error.time.tracking.not.enabed"));
            return null;
        }

        final Worklog worklog = worklogManager.getById(id);
        if (worklog != null)
        {
            if (hasPermissionToView(jiraServiceContext, worklog))
            {
                return worklog;
            }
            else
            {
                jiraServiceContext.getErrorCollection().addErrorMessage(jiraServiceContext.getI18nBean().getText("worklog.service.error.no.view.permission"));
                return null;
            }
        }
        else
        {
            jiraServiceContext.getErrorCollection().addErrorMessage(jiraServiceContext.getI18nBean().getText("worklog.service.error.no.worklog.for.id", id));
            return null;
        }
    }

    public List getByIssue(JiraServiceContext jiraServiceContext, Issue issue)
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();
        if (issue == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.null.issue"));
            return Collections.EMPTY_LIST;
        }
        return worklogManager.getByIssue(issue);
    }

    public List<Worklog> getByIssueVisibleToUser(JiraServiceContext jiraServiceContext, Issue issue)
    {
        List<Worklog> visibleWorklogs = new ArrayList<Worklog>();
        List allWorklogs = getByIssue(jiraServiceContext, issue);

        for (final Object allWorklog : allWorklogs)
        {
            Worklog worklog = (Worklog) allWorklog;
            if (hasPermissionToView(jiraServiceContext, worklog))
            {
                visibleWorklogs.add(worklog);
            }
        }

        return visibleWorklogs;
    }

    private boolean hasPermissionToView(JiraServiceContext jiraServiceContext, Worklog worklog) {
        // Retrieve both the group level and role level
        String groupLevel = worklog.getGroupLevel();
        Long roleLevel = worklog.getRoleLevelId();

        boolean roleProvided = (roleLevel != null);
        boolean groupProvided = StringUtils.isNotBlank(groupLevel);

        User user = jiraServiceContext.getUser();
        boolean userInRole = roleProvided && isUserInRole(roleLevel, user, worklog.getIssue());
        boolean userInGroup = groupProvided && isUserInGroup(user, groupLevel);
        boolean noLevelsProvided = !groupProvided && !roleProvided;

        return (noLevelsProvided || userInRole || userInGroup);
    }

    public boolean isTimeTrackingEnabled()
    {
        // TODO check permissions to see if you can tell if worklogging is enabled
        return timeTrackingConfiguration.enabled();
    }

    public boolean isIssueInEditableWorkflowState(Issue issue)
    {
        return issueManager.isEditable(issue);
    }

    void validateUpdateOrDeletePermissionCheckParams(Worklog worklog, ErrorCollection errorCollection, final JiraServiceContext jiraServiceContext)
    {
        if (worklog == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
            return;
        }

        Issue issue = worklog.getIssue();

        if (issue == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
            return;
        }

        if (!isIssueInEditableWorkflowState(issue))
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.not.editable.workflow.state"));
            return;
        }

        if (worklog.getId() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.id.null"));
        }
    }

    boolean hasEditIssuePermission(User user, Issue issue)
    {
        return permissionManager.hasPermission(Permissions.EDIT_ISSUE, issue, user);
    }

    protected Worklog validateParamsAndCreateWorklog(JiraServiceContext jiraServiceContext, Issue issue, String author,
            String groupLevel, String roleLevelId, String timeSpent, Date startDate, Long worklogId, String comment,
            Date created, Date updated, String updateAuthor, final String errorFieldPrefix)
    {
        Worklog worklog = null;
        // Validate the worklog params
        if (visibilityValidator.isValidVisibilityData(jiraServiceContext, WORKLOG_I18N_PREFIX, issue, groupLevel, roleLevelId))
        {
            // Validate the worklog input fields
            boolean defaultInputFieldsValidated = isValidWorklogInputFields(jiraServiceContext, issue, timeSpent, startDate, errorFieldPrefix);
            if (defaultInputFieldsValidated)
            {
                worklog = new WorklogImpl(worklogManager, issue, worklogId, author, comment, startDate, groupLevel,
                        TextUtils.stringSet(roleLevelId) ? Long.valueOf(roleLevelId) : null,
                        getDurationForFormattedString(timeSpent, jiraServiceContext), updateAuthor, created, updated);
            }
        }
        return worklog;
    }

    protected Worklog create(JiraServiceContext jiraServiceContext, WorklogResult worklogResult, Long newEstimate, boolean dispatchEvent)
    {
        Worklog newWorklog = null;
        User user = jiraServiceContext.getUser();
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        if (worklogResult == null || worklogResult.getWorklog() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.worklog.null"));
            return null;
        }

        if (worklogResult.getWorklog().getIssue() == null)
        {
            errorCollection.addErrorMessage(getText(jiraServiceContext, "worklog.service.error.issue.null"));
            return null;
        }

        // Re-do the permission check
        if (hasPermissionToCreate(jiraServiceContext, worklogResult.getWorklog().getIssue(), worklogResult.isEditableCheckRequired()))
        {
            newWorklog = worklogManager.create(user, worklogResult.getWorklog(), newEstimate, dispatchEvent);
        }

        return newWorklog;
    }

    protected boolean hasEditOwnPermission(User user, Worklog worklog)
    {
        return isSameAuthor(user, worklog) && permissionManager.hasPermission(Permissions.WORKLOG_EDIT_OWN, worklog.getIssue(), user);
    }

    protected boolean hasEditAllPermission(User user, Issue issue)
    {
        return permissionManager.hasPermission(Permissions.WORKLOG_EDIT_ALL, issue, user);
    }

    protected boolean hasDeleteOwnPermission(User user, Worklog worklog)
    {
        return isSameAuthor(user, worklog) && permissionManager.hasPermission(Permissions.WORKLOG_DELETE_OWN, worklog.getIssue(), user);
    }

    protected boolean hasDeleteAllPermission(User user, Issue issue)
    {
        return permissionManager.hasPermission(Permissions.WORKLOG_DELETE_ALL, issue, user);
    }

    protected boolean isSameAuthor(User user, Worklog worklog)
    {
        if (user != null && worklog.getAuthor() != null)
        {
            return user.getName().equals(worklog.getAuthor());
        }
        // We treat both authors being null as an equivalent user
        return user == null && worklog.getAuthor() == null;
    }

    protected Long getAutoAdjustNewEstimateOnUpdate(Issue issue, Long newTimeSpent, Long originalTimeSpent)
    {
        Long timeEstimate = issue.getEstimate();
        long oldTimeEstimate = (timeEstimate == null ? 0 : timeEstimate);
        long newTimeEstimate = oldTimeEstimate + originalTimeSpent - newTimeSpent;
        return newTimeEstimate < 0 ? 0 : newTimeEstimate;
    }

    protected Long reduceEstimate(Issue issue, Long amount)
    {
        Long timeEstimate = issue.getEstimate();
        long oldTimeEstimate = (timeEstimate == null ? 0 : timeEstimate);
        long newTimeEstimate = oldTimeEstimate - amount;
        return newTimeEstimate < 0 ? 0 : newTimeEstimate;
    }

    protected Long increaseEstimate(Issue issue, Long amount)
    {
        Long timeEstimate = issue.getEstimate();
        long oldTimeEstimate = (timeEstimate == null ? 0 : timeEstimate);
        long newTimeEstimate = oldTimeEstimate + amount;
        return newTimeEstimate < 0 ? 0 : newTimeEstimate;
    }

    protected boolean isValidNewEstimate(JiraServiceContext jiraServiceContext, String newEstimate)
    {
        return isValidNewEstimate(jiraServiceContext, newEstimate, null);
    }
    
    protected boolean isValidNewEstimate(JiraServiceContext jiraServiceContext, String newEstimate, final String errorFieldPrefix)
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        String errorField = "newEstimate";
        if (StringUtils.isNotBlank(errorFieldPrefix))
        {
            errorField = errorFieldPrefix + errorField;
        }
        if (TextUtils.stringSet(newEstimate))
        {
            if (!isValidDuration(newEstimate, jiraServiceContext))
            {
                errorCollection.addError(errorField, getText(jiraServiceContext, "worklog.service.error.newestimate"));
                return false;
            }
        }
        else
        {
            errorCollection.addError(errorField, getText(jiraServiceContext, "worklog.service.error.new.estimate.not.specified"));
            return false;
        }

        return true;
    }

    /**
     * Checks if the given String is a valid amount of time to change an estimate by.
     *
     * @param jiraServiceContext JiraServiceContext
     * @param adjustmentAmount String with amount of time eg "3d 4h"
     * @return true if this change is a valid time.
     */
    protected boolean isValidAdjustmentAmount(JiraServiceContext jiraServiceContext, String adjustmentAmount)
    {
        return isValidAdjustmentAmount(jiraServiceContext, adjustmentAmount, null);
    }

    /**
     * Checks if the given String is a valid amount of time to change an estimate by.
     *
     * @param jiraServiceContext JiraServiceContext
     * @param adjustmentAmount String with amount of time eg "3d 4h"
     * @param errorFieldPrefix the prefix for the error field
     * @return true if this change is a valid time.
     */
    protected boolean isValidAdjustmentAmount(JiraServiceContext jiraServiceContext, String adjustmentAmount, final String errorFieldPrefix)
    {
        ErrorCollection errorCollection = jiraServiceContext.getErrorCollection();

        String errorField = "adjustmentAmount";
        if (StringUtils.isNotBlank(errorFieldPrefix))
        {
            errorField = errorFieldPrefix + errorField;
        }

        // Check that it is not empty
        if (!TextUtils.stringSet(adjustmentAmount))
        {
            errorCollection.addError(errorField, getText(jiraServiceContext, "worklog.service.error.adjustment.amount.not.specified"));
            return false;
        }
        // Check that the String is a valid time.
        if (!isValidDuration(adjustmentAmount, jiraServiceContext))
        {
            errorCollection.addError(errorField, getText(jiraServiceContext, "worklog.service.error.adjustment.amount.invalid"));
            return false;
        }

        return true;
    }

    protected boolean isValidWorklogInputFields(JiraServiceContext jiraServiceContext, Issue issue, String timeSpent, Date startDate, final String errorFieldPrefix)
    {
        ErrorCollection errorCollection = new SimpleErrorCollection();

        // Check that timespent has been specified and that it is a valid duration
        String errorField = "timeLogged";
        if (StringUtils.isNotBlank(errorFieldPrefix))
        {
            errorField = errorFieldPrefix + errorField;
        }

        if (!TextUtils.stringSet(timeSpent))
        {
            errorCollection.addError(errorField, getText(jiraServiceContext, "worklog.service.error.timespent.required"));
        }
        else if (!isValidDuration(timeSpent, jiraServiceContext))
        {
            errorCollection.addError(errorField, getText(jiraServiceContext, "worklog.service.error.invalid.time.duration"));
        }
        else if (getDurationForFormattedString(timeSpent, jiraServiceContext) == 0)
        {
            errorCollection.addError(errorField, getText(jiraServiceContext, "worklog.service.error.timespent.zero"));
        }

        // Check that if startDate is specified that it is a valid date
        errorField = "startDate";
        if (StringUtils.isNotBlank(errorFieldPrefix))
        {
            errorField = errorFieldPrefix + errorField;
        }
        if (startDate == null)
        {
            errorCollection.addError(errorField, getText(jiraServiceContext, "worklog.service.error.invalid.worklog.date"));
        }

        if (errorCollection.hasAnyErrors())
        {
            jiraServiceContext.getErrorCollection().addErrorCollection(errorCollection);
        }
        return !errorCollection.hasAnyErrors();
    }

    /**
     * @param duration the input duration
     * @return true only if the input duration is not blank and is not valid as deemed by {@link com.atlassian.jira.util.JiraDurationUtils#parseDuration(String, java.util.Locale)}
     */
    boolean isValidDuration(final String duration, JiraServiceContext jiraServiceContext)
    {
        if (StringUtils.isNotBlank(duration))
        {
            Long aLong = null;
            try
            {
                aLong = jiraDurationUtils.parseDuration(duration, jiraServiceContext.getI18nBean().getLocale());
            }
            catch (InvalidDurationException e)
            {
                return false;
            }
        }
        return true;
    }


    protected long getDurationForFormattedString(String timeSpent, JiraServiceContext jiraServiceContext)
    {
        try
        {
            return jiraDurationUtils.parseDuration(timeSpent, jiraServiceContext.getI18nBean().getLocale());
        }
        catch (InvalidDurationException e)
        {
            // This should never happen since we have done the validation with isValid before this method is ever
            // called
            log.error("Trying to create/update a worklog with an invalid duration, this should never happen.", e);
            throw new RuntimeException(e);
        }
    }

    protected boolean isUserInGroup(User user, String groupLevel)
    {
        return user != null && user.inGroup(groupLevel);
    }

    protected boolean isUserInRole(Long roleLevel, User user, Issue issue)
    {
        boolean isUserInRole = false;
        ProjectRole projectRole = projectRoleManager.getProjectRole(roleLevel);
        if (projectRole != null)
        {
            isUserInRole = projectRoleManager.isUserInProjectRole(user, projectRole, issue.getProjectObject());
        }
        return isUserInRole;
    }

    private String getText(JiraServiceContext jiraServiceContext, String key)
    {
        return jiraServiceContext.getI18nBean().getText(key);
    }

    private String getText(JiraServiceContext jiraServiceContext, String key, String param)
    {
        return jiraServiceContext.getI18nBean().getText(key, param);
    }

}
