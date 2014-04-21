package com.atlassian.jira.issue.fields;

import com.atlassian.core.user.UserUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueUtils;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.comparator.UserBestNameComparator;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.option.AssigneeOption;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.AssigneeSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.AssigneeStatisticsMapper;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.plugin.assignee.AssigneeResolver;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Copyright (c) 2002-2004 All rights reserved.
 */
public class AssigneeSystemField extends AbstractOrderableNavigableFieldImpl implements HideableField, UserField
{
    private static final Logger log = Logger.getLogger(AssigneeSystemField.class);

    private static final String SEPERATOR_STRING = "---------------";

    private static final String ASSIGNEE_NAME_KEY = "issue.field.assignee";
    public static final String AUTOMATIC_ASSIGNEE_STRING = "-automatic-";

    private final PermissionSchemeManager permissionSchemeManager;
    private final AssigneeStatisticsMapper assigneeStatisticsMapper;
    private final AssigneeResolver assigneeResolver;
    private PermissionContextFactory permissionContextFactory;
    private UserManager userManager;
    private final UserHistoryManager userHistoryManager;
    private final VelocityRequestContextFactory velocityRequestContextFactory;


    public AssigneeSystemField(VelocityManager velocityManager, PermissionSchemeManager permissionSchemeManager,
            PermissionManager permissionManager, ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext, AssigneeStatisticsMapper assigneeStatisticsMapper,
            AssigneeResolver assigneeResolver, PermissionContextFactory permissionContextFactory,
            AssigneeSearchHandlerFactory assigneeSearchHandlerFactory, UserManager userManager,
            UserHistoryManager userHistoryManager, VelocityRequestContextFactory velocityRequestContextFactory)

    {
        super(IssueFieldConstants.ASSIGNEE, ASSIGNEE_NAME_KEY, velocityManager, applicationProperties, authenticationContext, permissionManager, assigneeSearchHandlerFactory);
        this.permissionSchemeManager = permissionSchemeManager;
        this.assigneeStatisticsMapper = assigneeStatisticsMapper;
        this.assigneeResolver = assigneeResolver;
        this.permissionContextFactory = permissionContextFactory;
        this.userManager = userManager;
        this.userHistoryManager = userHistoryManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, displayParameters);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put("currentAssignee", operationContext.getFieldValuesHolder().get(getId()));
        if (hasContext(operationContext, issue))
        {
            velocityParams.put("assigneeOptions", getAssigneeOptionsList(operationContext, issue));
            return renderTemplate("assignee-edit.vm", velocityParams);
        }
        else
        {
            WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
            webResourceManager.requireResource("jira.webresources:autocomplete");

            velocityParams.put("allowUnassigned", isUnassignedIssuesEnabled());
            return renderTemplate("assignee-edit-no-context.vm", velocityParams);
        }
    }

    public String getBulkEditHtml(OperationContext operationContext, Action action, BulkEditBean bulkEditBean, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(null, action, null, displayParameters);
        velocityParams.put("currentAssignee", operationContext.getFieldValuesHolder().get(getId()));
        velocityParams.put("assigneeOptions", getAssigneeOptionsList(bulkEditBean));
        return renderTemplate("assignee-edit.vm", velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        User assignee = issue.getAssignee();
        if (assignee != null)
        {
            velocityParams.put("assignee", assignee.getName());
        }
        else
        {
            velocityParams.put("assignee", null);
        }
        return getViewHtml(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        velocityParams.put("assignee", value);
        return getViewHtml(velocityParams);
    }

    private String getViewHtml(Map velocityParams)
    {
        velocityParams.put("userutils", new UserUtils());
        return renderTemplate("assignee-view.vm", velocityParams);
    }

    /**
     * Validate from parameters given an existing issue (usually invoked during some sort of edit stage)
     */
    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        String assigneeId = (String) fieldValuesHolder.get(getId());

        if (IssueUtils.SEPERATOR_ASSIGNEE.equals(assigneeId))
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("assign.error.invaliduser"));
            return;
        }
        else if (IssueUtils.AUTOMATIC_ASSIGNEE.equals(assigneeId))
        {
            errorCollectionToAddTo.addErrorCollection(assigneeResolver.validateDefaultAssignee(issue, fieldValuesHolder));
            return;
        }
        else
        {
            // The user must have 'assign' permission - as otherwise 'automatic' should be chosen, or the field should not
            // be presented at all
            if (!hasPermission(issue, Permissions.ASSIGN_ISSUE))
            {
                errorCollectionToAddTo.addErrorMessage(i18n.getText("assign.error.no.permission"));
                return;
            }
        }

        // Check that the assignee is valid
        if (assigneeId != null)
        {
            final User assigneeUser = userManager.getUser(assigneeId);
            if (assigneeUser == null)
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("assign.error.user.cannot.be.assigned", "'" + assigneeId + "'"));
            }
            else
            {
                // Check that the assignee has the assignable permission
                // Note: we may be creating the issue still, so it may be null - hence (?) pass in project
                if (!ManagerFactory.getPermissionManager().hasPermission(Permissions.ASSIGNABLE_USER, issue.getProject(), assigneeUser))
                {
                    errorCollectionToAddTo.addError("assignee", i18n.getText("assign.error.user.cannot.be.assigned", "'" + assigneeId + "'"));
                }
            }
        }
        else
        {
            // check whether assigning to null is allowed
            if (!isUnassignedIssuesEnabled())
            {
                log.info("Validation error: Issues must be assigned");
                errorCollectionToAddTo.addError("assignee", i18n.getText("assign.error.issues.unassigned"));
            }
        }
    }

    private boolean isUnassignedIssuesEnabled()
    {
        return getApplicationProperties().getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED);
    }

    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            User assignee;
            String assigneeId = (String) getValueFromParams(fieldValueHolder);
            if (IssueUtils.AUTOMATIC_ASSIGNEE.equals(assigneeId))
            {
                assignee = assigneeResolver.getDefaultAssigneeObject(issue, fieldValueHolder);
            }
            else
            {
                assignee = getUser(assigneeId);
            }

            issue.setAssignee(assignee);
        }
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        for (Iterator iterator = originalIssues.iterator(); iterator.hasNext();)
        {
            Issue originalIssue = (Issue) iterator.next();

            if (hasValue(originalIssue))
            {
                // See if the assignee is assignable in the target project
                if (!getPermissionManager().hasPermission(Permissions.ASSIGNABLE_USER, targetIssue.getProject(), originalIssue.getAssignee()))
                {
                    return new MessagedResult(true);
                }
            }
            else
            {
                // See unassigned issues are allowed - if not, then we need to set the value
                if (!isUnassignedIssuesEnabled())
                {
                    return new MessagedResult(true);
                }
            }
        }
        return new MessagedResult(false);
    }

    public void populateForMove(Map fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // If we are displaying the field then the current assignee is not assinable, so populate with default value.
        populateDefaults(fieldValuesHolder, targetIssue);
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setAssignee(null);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public boolean hasValue(Issue issue)
    {
        return issue.getAssignee() != null;
    }

    public void createValue(Issue issue, Object value)
    {
        // The field is recorded on the issue itself so there is nothing to do
    }

    /**
     * Update the issue
     */
    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        //NOTE: No need to update issue in the data store as the value is stored on the issue record itself

        Object currentValue = modifiedValue.getOldValue();
        Object value = modifiedValue.getNewValue();
        ChangeItemBean cib = null;

        if (currentValue == null)
        {
            if (value != null)
            {
                User assignee = (User) value;
                cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), null, null, assignee.getName(), assignee.getDisplayName());
            }
        }
        else
        {
            if (!valuesEqual(value, currentValue))
            {
                User currentAssignee = (User) currentValue;
                if (value != null)
                {
                    User assignee = (User) value;
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), currentAssignee.getName(), currentAssignee.getDisplayName(), assignee.getName(), assignee.getDisplayName());
                }
                else
                {
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), currentAssignee.getName(), currentAssignee.getDisplayName(), null, null);
                }
            }
        }

        if (cib != null)
        {
            issueChangeHolder.addChangeItem(cib);
        }
    }

    private User getUser(String assigneeId)
    {
        if (assigneeId != null)
        {
            User user = userManager.getUser(assigneeId);
            if (user == null)
            {
                throw new DataAccessException("Error while retrieving user with id '" + assigneeId + "'.");
            }
            return user;
        }
        else
        {
            return null;
        }
    }

    private boolean hasContext(OperationContext operationContext, Issue issue)
    {
        return issue != null;
    }

    /**
     * Get this issue's potential assignees.
     *
     * @return {@link AssigneeOption}s.
     */
    private Collection getAssigneeOptionsList(OperationContext operationContext, Issue issue)
    {
        Collection users = getAssignableUsers(operationContext, issue);
        User reporter = null;
        if (issue.getReporter() != null)
        {
            reporter = issue.getReporter();
        }

        // Get recent assignees for this issue
       Set<String> recentAssignees = new HashSet();
       ChangeHistoryManager changeHistoryManager = (ChangeHistoryManager) ComponentManager.getInstance().getContainer().getComponentInstanceOfType(ChangeHistoryManager.class);
       List<ChangeItemBean> assigneeHistory = changeHistoryManager.getChangeItemsForField(issue, "assignee");
       for (ChangeItemBean changeItemBean : assigneeHistory)
       {
           recentAssignees.add(changeItemBean.getTo());
       }
       recentAssignees.add(issue.getAssigneeId());

       return makeAssigneeOptionsList(users, reporter, recentAssignees);
    }

    private Collection<User> getAssignableUsers(OperationContext operationContext, Issue issue)
    {
        PermissionContext ctx = permissionContextFactory.getPermissionContext(operationContext, issue);
        Collection<com.opensymphony.user.User> users = permissionSchemeManager.getUsers((long) Permissions.ASSIGNABLE_USER, ctx);
        // Need to remove duplicates users, i.e. users who occur in 2 directories
        Map<String,User> uniqueUsers = new HashMap<String, User>();
        for (User user : users)
        {
            if (uniqueUsers.containsKey(user.getName()))
            {
                // Get the real user with this name and add him to the list.
                User realUser = userManager.getUserObject(user.getName());
                uniqueUsers.put(user.getName(), realUser);
            }
            else
            {
                uniqueUsers.put(user.getName(), user);
            }
        }

        return uniqueUsers.values();
    }


    private Collection<AssigneeOption> getAssigneeOptionsList(BulkEditBean bulkEditBean)
    {
        Collection<User> users = null;

        final Collection<Issue> issues;
        if (BulkMoveOperation.NAME.equals(bulkEditBean.getOperationName()))
        {
            //JRA-17011: When moving we wish to find the asignees in the target project not the source.
            issues = bulkEditBean.getTargetIssueObjects().values();
        }
        else
        {
            issues = bulkEditBean.getSelectedIssues();
        }

        ChangeHistoryManager changeHistoryManager = ComponentAccessor.getChangeHistoryManager();
        Set<String> recentAssignees = new HashSet();
        for (Issue issue : issues)
        {
            if (users == null)
            {
                users = getAssignableUsers(bulkEditBean, issue);
            }
            else
            {
                users.retainAll(getAssignableUsers(bulkEditBean, issue));
            }
            // Get recent assignees for this issue
            List<ChangeItemBean> assigneeHistory = changeHistoryManager.getChangeItemsForField(issue, "assignee");
            for (ChangeItemBean changeItemBean : assigneeHistory)
            {
                if (!recentAssignees.contains(changeItemBean.getTo()))
                {
                    recentAssignees.add(changeItemBean.getTo());
                }
            }
            recentAssignees.add(issue.getAssigneeId());
        }

        return makeAssigneeOptionsList(users, null, recentAssignees);
    }

    private Collection<AssigneeOption> makeAssigneeOptionsList(Collection<User> initialUsers, User reporter, final Collection<String> recentAssignees)
    {
        // First sort the list to be sure we display an ordered list
        List<User> users = new ArrayList<User>(initialUsers);
        Collections.sort(users, new UserBestNameComparator(authenticationContext.getLocale()));

        // JRA-14128: make a map of the counts of the Full Names of the users,
        // so that we can detect which users have duplicate Full Names
        Map<String, Boolean> fullNames = new HashMap<String, Boolean>();

        for (User user : users)
        {
            String fullName = user.getDisplayName();
            Boolean isUnique = fullNames.get(fullName);
            if (isUnique == null)
            {
                fullNames.put(fullName, Boolean.TRUE);
            }
            else
            {
                fullNames.put(fullName, Boolean.FALSE);
            }
        }

        // Collection of AssigneeOption objects
        Collection<AssigneeOption> assigneeOptionsList = new ArrayList<AssigneeOption>();
        if (isUnassignedIssuesEnabled())
        {
            assigneeOptionsList.add(new AssigneeOption(null, getAuthenticationContext().getI18nHelper().getText("common.concepts.unassigned"), true));
        }

        assigneeOptionsList.add(new AssigneeOption(IssueUtils.AUTOMATIC_ASSIGNEE, "- " + getAuthenticationContext().getI18nHelper().getText("common.concepts.automatic") + " -", true));

        // Get most recently assigned users from the user history
        User remoteUser = authenticationContext.getUser();
        List<UserHistoryItem> recentUserHistory = userHistoryManager.getHistory(UserHistoryItem.ASSIGNEE, remoteUser);
        List<String> recentHistoryAssignees = new ArrayList<String>();
        for (UserHistoryItem userHistoryItem : recentUserHistory)
        {
            recentHistoryAssignees.add(userHistoryItem.getEntityId());
            if (recentHistoryAssignees.size() >= 5)
            {
                break;
            }
        }

        int lruCount = 0;
        for (Iterator<User> iter = users.iterator(); iter.hasNext(); )
        {
            User user = iter.next();
            if (recentAssignees.contains(user.getName()) || recentHistoryAssignees.contains(user.getName()) || user.equals(reporter))
            {
                if (lruCount == 0)
                {
                    assigneeOptionsList.add(new AssigneeOption(IssueUtils.SEPERATOR_ASSIGNEE, SEPERATOR_STRING, false));
                }
                assigneeOptionsList.add(createAssigneeOption(user, fullNames));
                lruCount++;
            }
        }

        if (!users.isEmpty())
        {
            assigneeOptionsList.add(new AssigneeOption(IssueUtils.SEPERATOR_ASSIGNEE, SEPERATOR_STRING, false));
        }

        for (User user : users)
        {
            assigneeOptionsList.add(createAssigneeOption(user, fullNames));
        }

        return assigneeOptionsList;
    }

    /*
     * JRA-14128: if the user's full name is not unique, append the user name at the end of the display name
     */
    private AssigneeOption createAssigneeOption(User user, Map<String, Boolean> fullNames)
    {
        String displayName = user.getDisplayName();
        boolean isUnique = fullNames.get(displayName);
        if (!isUnique)
        {
            displayName += " (" + user.getName() + ")";
        }
        return new AssigneeOption(user.getName(), displayName, true);
    }

    public boolean isShown(Issue issue)
    {
        return hasPermission(issue, Permissions.ASSIGN_ISSUE);
    }

    public void populateDefaults(Map fieldValuesHolder, Issue issue)
    {
        fieldValuesHolder.put(getId(), IssueUtils.AUTOMATIC_ASSIGNEE);
    }

    public void populateFromIssue(Map fieldValuesHolder, Issue issue)
    {
        User assignee = issue.getAssignee();
        if (assignee != null)
        {
            fieldValuesHolder.put(getId(), assignee.getName());
        }
        else
        {
            fieldValuesHolder.put(getId(), null);
        }
    }

    protected Object getRelevantParams(Map params)
    {
        String[] value = (String[]) params.get(getId());
        if (value != null && value.length > 0 && TextUtils.stringSet(value[0]))
        {
            return value[0];
        }
        else
        {
            return null;
        }
    }

    /////////////////////////////////////////// Bulk Edit //////////////////////////////////////////////////////////
    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        if (isHidden(bulkEditBean.getFieldLayouts()))
        {
            return "bulk.edit.unavailable.hidden";
        }

        // NOTE: Not checking the available assignees for the selected issues as if the user has the assign permission
        // the 'Automatic' option is always available.

        // Have to look through all the issues in case permission has been given to current assignee/reporter (i.e. role based)
        for (Iterator iterator = bulkEditBean.getSelectedIssues().iterator(); iterator.hasNext();)
        {
            Issue issue = (Issue) iterator.next();
            // If we got here then the field is visible in all field layouts
            // So check for permission
            if (!isShown(issue))
            {
                return "bulk.edit.unavailable.permission";
            }
        }

        // This field is available for bulk-editing, return null (i.e no unavailble message)
        return null;
    }

    public Object getValueFromParams(Map params)
    {
        return params.get(getId());
    }

    public void populateParamsFromString(Map fieldValuesHolder, String stringValue, Issue issue)
            throws FieldValidationException
    {
        if (AUTOMATIC_ASSIGNEE_STRING.equals(stringValue))
        {
            fieldValuesHolder.put(getId(), IssueUtils.AUTOMATIC_ASSIGNEE);
        }
        else
        {
            fieldValuesHolder.put(getId(), stringValue);
        }
    }

    ///////////////////////// Navigable field implementation //////////////////////////////////////////////////////////
    public String getColumnHeadingKey()
    {
        return "issue.column.heading.assignee";
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_ASCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return assigneeStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        final Map velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        try
        {
            final String assigneeUserId = issue.getAssigneeId();
            if (assigneeUserId != null)
            {
                velocityParams.put("assigneeUsername", assigneeUserId);
            }
        }
        catch (DataAccessException e)
        {
            log.debug("Error occurred retrieving assignee", e);
        }
        return renderTemplate("assignee-columnview.vm", velocityParams);
    }
}
