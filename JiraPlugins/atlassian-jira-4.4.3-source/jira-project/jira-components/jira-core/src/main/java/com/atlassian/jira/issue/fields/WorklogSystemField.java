package com.atlassian.jira.issue.fields;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.worklog.TimeTrackingConfiguration;
import com.atlassian.jira.bc.issue.worklog.WorklogAdjustmentAmountInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogAdjustmentAmountResult;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogInputParametersImpl;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateInputParameters;
import com.atlassian.jira.bc.issue.worklog.WorklogNewEstimateResult;
import com.atlassian.jira.bc.issue.worklog.WorklogResult;
import com.atlassian.jira.bc.issue.worklog.WorklogService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.RenderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.handlers.CommentSearchHandlerFactory;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.issue.worklog.WorklogManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraDurationUtils;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.velocity.VelocityManager;
import org.apache.commons.lang.StringUtils;
import webwork.action.Action;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * <p>A field representation of logging work.
 *
 * <p>This allows JIRA administrators to place the "Log Work" field on screens. This means that JIRA users can now log
 * work whilst creating, editing or transitioning issues. The field works in a similar way to the
 * {@link CommentSystemField} in that while it implements the "getEditHtml" method, previously entered work logs cannot
 * be edited. In effect, the only functionality available is to "add" more work logs.
 *
 * <p>The {@link WorklogService} is used to do the hard work of validating and creating the work logs from user input.
 *
 * @since v4.2
 * @see com.atlassian.jira.bc.issue.worklog.WorklogService
 */
public class WorklogSystemField extends AbstractOrderableField implements OrderableField, RequirableField, HideableField, RenderableField
{
    public static final String WORKLOG_TIMELOGGED = IssueFieldConstants.WORKLOG + "_timeLogged";
    public static final String WORKLOG_STARTDATE = IssueFieldConstants.WORKLOG + "_startDate";
    public static final String WORKLOG_NEWESTIMATE = IssueFieldConstants.WORKLOG + "_newEstimate";
    public static final String WORKLOG_ADJUSTMENTAMOUNT = IssueFieldConstants.WORKLOG + "_adjustmentAmount";
    public static final String WORKLOG_ADJUSTESTIMATE = IssueFieldConstants.WORKLOG + "_adjustEstimate";
    public static final String WORKLOG_ACTIVATE = IssueFieldConstants.WORKLOG + "_activate";

    private static final String WORKLOG_NAME_KEY = "issue.field.worklog";
    private static final String WORKLOG_ADD_TEMPLATE = "worklog-edit.vm";

    private static final String PARAM_ISCREATEISSUE = "isCreateIssue";
    private static final String PARAM_ISEDITISSUE = "isEditIssue";

    /**
     * The parameter name of the user-chosen group or role-type "level" for restricting the comment visibility
     */
    private static final String PARAM_COMMENT = "comment";
    private static final String PARAM_COMMENT_LEVEL = CommentSystemField.PARAM_COMMENT_LEVEL;

    private final ComponentLocator componentLocator;

    public WorklogSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties,
            JiraAuthenticationContext authenticationContext, PermissionManager permissionManager,
            CommentSearchHandlerFactory searchHandlerFactory, ComponentLocator componentLocator)
    {
        super(IssueFieldConstants.WORKLOG, WORKLOG_NAME_KEY, velocityManager, applicationProperties, authenticationContext, permissionManager, searchHandlerFactory);
        this.componentLocator = componentLocator;
    }

    @Override
    public SearchHandler createAssociatedSearchHandler()
    {
        // work log system field cannot be searched
        return null;
    }

    /**
     * Defines the object that will be passed through to the create method
     *
     * @param inputParameters is a representation of the request params that are available
     * @return an object that holds the params we need for this Field.
     */
    protected Object getRelevantParams(Map inputParameters)
    {
        boolean isCreateIssue = parseBoolean(asArray(inputParameters, PARAM_ISCREATEISSUE));

        final WorklogValue.Builder builder = new WorklogValue.Builder()
                .setTimeLogged(asArray(inputParameters, WORKLOG_TIMELOGGED))
                .setStartDate(asArray(inputParameters, WORKLOG_STARTDATE))
                .setAdjustEstimate(asArray(inputParameters, WORKLOG_ADJUSTESTIMATE))
                .setNewEstimate(asArray(inputParameters, WORKLOG_NEWESTIMATE))
                .setAdjustmentAmount(asArray(inputParameters, WORKLOG_ADJUSTMENTAMOUNT))
                .setCreateIssue(isCreateIssue)
                .setEditIssue(asArray(inputParameters, PARAM_ISEDITISSUE))
                .setActivated(asArray(inputParameters, WORKLOG_ACTIVATE));

        // on Create Issue, there is no Comment field, so we always use the comment as the Work Description
        if (isCreateIssue || getTimeTrackingConfiguration().copyCommentToWorkDescriptionOnTransition())
        {
            builder.setComment(asArray(inputParameters, PARAM_COMMENT)).setCommentLevel(asArray(inputParameters, PARAM_COMMENT_LEVEL));
        }

        return builder.build();
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        // get the previously created WorklogValue
        final WorklogValue value = (WorklogValue) operationContext.getFieldValuesHolder().get(getId());
        if (value == null)
        {
            return;
        }

        // if we were not activated, replace us with a new copy that has no values entered and then return
        if (!value.isActivated())
        {
            WorklogValue.Builder valueBuilder = new WorklogValue.Builder().setCreateIssue(value.isCreateIssue()).setEditIssue(value.isEditIssue());
            operationContext.getFieldValuesHolder().put(getId(), valueBuilder.build());
            return;
        }

        // only need to validate if the field is Required or if timeLogged, adjustmentAmount or newEstimate have been entered
        boolean isValidationRequired = fieldScreenRenderLayoutItem.isRequired() || value.isSet();
        if (!isValidationRequired)
        {
            return;
        }

        boolean isEditableCheckRequired = value.isEditIssue();

        final WorklogValue.Builder valueBuilder = new WorklogValue.Builder(value);

        final CommentVisibility visibility = new CommentVisibility(value.commentLevel());
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(authenticationContext.getUser(), errorCollectionToAddTo, i18n);
        final Date parsedStartDate = WorklogValue.Builder.parseStartDate(getOutlookDateManager(), authenticationContext.getLocale(), value.startDate());

        // Use the WorklogService to do the validation and save the results back onto the WorklogValue.Builder
        WorklogResult worklogResult;
        final WorklogInputParametersImpl.Builder inputBuilder = WorklogInputParametersImpl
                .issue(issue)
                .timeSpent(value.timeLogged())
                .startDate(parsedStartDate)
                .comment(value.comment())
                .groupLevel(visibility.getGroupLevel())
                .roleLevelId(visibility.getRoleLevel())
                .editableCheckRequired(isEditableCheckRequired)
                .errorFieldPrefix(IssueFieldConstants.WORKLOG + "_");
        if (WorklogValue.AdjustEstimate.NEW == value.adjustEstimate())
        {
            final WorklogNewEstimateInputParameters params = inputBuilder
                    .newEstimate(value.newEstimate())
                    .buildNewEstimate();
            worklogResult = getWorklogService().validateCreateWithNewEstimate(jiraServiceContext, params);
        }
        else if (WorklogValue.AdjustEstimate.MANUAL == value.adjustEstimate())
        {
            final WorklogAdjustmentAmountInputParameters params = inputBuilder
                    .adjustmentAmount(value.adjustmentAmount())
                    .buildAdjustmentAmount();
            worklogResult = getWorklogService().validateCreateWithManuallyAdjustedEstimate(jiraServiceContext, params);
        }
        else
        {
            final WorklogInputParameters params = inputBuilder.build();
            worklogResult = getWorklogService().validateCreate(jiraServiceContext, params);
        }
        valueBuilder.setWorklogResult(worklogResult);

        // reset the WorklogValue in the FieldValuesHolder
        operationContext.getFieldValuesHolder().put(getId(), valueBuilder.build());
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        final Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        populateOurVelocityParams((operationContext != null) ? operationContext.getFieldValuesHolder() : null, velocityParams, issue, true, operationContext, fieldLayoutItem);
        return renderTemplate(WORKLOG_ADD_TEMPLATE, velocityParams);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        final Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        populateOurVelocityParams((operationContext != null) ? operationContext.getFieldValuesHolder() : null, velocityParams, issue, false, operationContext, fieldLayoutItem);
        return renderTemplate(WORKLOG_ADD_TEMPLATE, velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public boolean isShown(Issue issue)
    {
        return getTimeTrackingConfiguration().enabled() && hasPermission(issue, Permissions.WORK_ISSUE);
    }

    public void populateDefaults(Map fieldValuesHolder, Issue issue)
    {
        final WorklogValue.Builder builder = new WorklogValue.Builder();
        builder.setCommentLevel(null);
        builder.setAdjustEstimate(WorklogValue.AdjustEstimate.AUTO);
        fieldValuesHolder.put(getId(), builder.build());
    }

    // since we don't edit the previously entered worklog values and we can't resolve a single worklog value from an issue,
    // just populate with defaults.
    public void populateFromIssue(Map fieldValuesHolder, Issue issue)
    {
        populateDefaults(fieldValuesHolder, issue);
    }

    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    public void createValue(Issue issue, Object value)
    {
        createWorklog((WorklogValue) value);
    }

    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        createWorklog((WorklogValue) modifiedValue.getNewValue());
    }

    private void createWorklog(final WorklogValue value)
    {
        // here is where we invoke the WorklogService to commit the Worklog
        final WorklogResult worklogResult = value.worklogResult();

        // note that we don't expect this to be actually used but we need to pass it anyway
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(authenticationContext.getUser());

        // Based on how the user wants to update the remaining estimate we will call the correct do method on the service
        switch (value.adjustEstimate())
        {
            case AUTO:
                getWorklogService().createAndAutoAdjustRemainingEstimate(jiraServiceContext, worklogResult, true);
                break;
            case NEW:
                getWorklogService().createWithNewRemainingEstimate(jiraServiceContext, (WorklogNewEstimateResult) worklogResult, true);
                break;
            case MANUAL:
                getWorklogService().createWithManuallyAdjustedEstimate(jiraServiceContext, (WorklogAdjustmentAmountResult) worklogResult, true);
                break;
            case LEAVE:
                getWorklogService().createAndRetainRemainingEstimate(jiraServiceContext, worklogResult, true);
                break;
        }
    }

    /**
     * Sets the value as a modified external field in the issue so that this
     * field will be updated along with all the other modified issue values.
     */
    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            final WorklogValue value = (WorklogValue) fieldValueHolder.get(getId());
            if (value.worklogResult() != null)
            {
                issue.setExternalFieldValue(getId(), value);
            }
        }
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        return new MessagedResult(false);
    }

    public void populateForMove(Map fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        throw new UnsupportedOperationException("Remove is not done through the system field for worklogs.");
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return false;
    }

    // return false so that move does not get the wrong idea
    public boolean hasValue(Issue issue)
    {
        return false;
    }

    public Object getValueFromParams(Map params) throws FieldValidationException
    {
        if (params.containsKey(getId()))
        {
            return params.get(getId());
        }

        return null;
    }

    // no conversion is needed
    public void populateParamsFromString(Map fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
    }

    /**
     * Adds to the given velocity parameters using the given fieldValuesHolder and
     * fieldLayoutItem (to determine the renderer).
     *
     * @param fieldValuesHolder the field values holder
     * @param velocityParams    the velocity parameters to which values will be added
     * @param issue             the issue in context
     * @param isCreateIssue     set to true if we are currently creating an issue
     * @param operationContext  the operation context
     * @param fieldLayoutItem   the field layout item representing the WorklogSystemField
     */
    private void populateOurVelocityParams(Map fieldValuesHolder, Map<String, Object> velocityParams, final Issue issue, final boolean isCreateIssue, final OperationContext operationContext, final FieldLayoutItem fieldLayoutItem)
    {
        // include extra parameters for renderer
        populateRendererParams(velocityParams, fieldLayoutItem);

        // include extra resource for managing the view toggle between Logging Work and Remaining Estimate
        final WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:worklog-timetracking-toggle");

        if (fieldValuesHolder != null)
        {
            final WorklogValue value = (WorklogValue) fieldValuesHolder.get(getId());
            String startDate = null;
            if (value != null)
            {
                velocityParams.put(getId(), value);
                velocityParams.put("isLogWorkActivated", value.isActivated());
                velocityParams.put("adjustEstimate", value.adjustEstimate().toString().toLowerCase());
                startDate = value.startDate();
            }

            // if a startDate was not previously specified, use the current date as the default
            if (StringUtils.isBlank(startDate))
            {
                startDate = getCurrentDateTimeFormatted();
            }
            velocityParams.put("startDate", startDate);
        }

        velocityParams.put("groupLevels", getGroupLevels());

        if (issue != null)
        {
            velocityParams.put("roleLevels", getRoleLevels(issue));
            // current estimate
            velocityParams.put("currentEstimate", getCurrentEstimate(issue));
        }

        // timetracking configuration
        velocityParams.put("daysPerWeek", getTimeTrackingConfiguration().getDaysPerWeek());
        velocityParams.put("hoursPerDay", getTimeTrackingConfiguration().getHoursPerDay());

        // date popup
        velocityParams.put("dateFormat", CustomFieldUtils.getDateFormat());
        velocityParams.put("dateTimeFormat", CustomFieldUtils.getDateTimeFormat());
        velocityParams.put("timeFormat", CustomFieldUtils.getTimeFormat());
        velocityParams.put("dateTimePicker", Boolean.TRUE);

        velocityParams.put("isCreateIssue", isCreateIssue);

        // put this in the context so we can read it back during validation
        boolean isEditIssue = operationContext != null && IssueOperations.EDIT_ISSUE_OPERATION == operationContext.getIssueOperation();
        velocityParams.put("isEditIssue", isEditIssue);

        populateVelocityParamsForTimeTrackingProxy(fieldValuesHolder, issue, velocityParams, fieldLayoutItem);
    }

    private void populateRendererParams(final Map<String, Object> velocityParams, final FieldLayoutItem fieldLayoutItem)
    {
        final String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
        velocityParams.put("rendererDescriptor", getRendererManager().getRendererForType(rendererType).getDescriptor());
        velocityParams.put("rendererParams", new HashMap());
    }

    private void populateVelocityParamsForTimeTrackingProxy(final Map fieldValuesHolder, final Issue issue, final Map<String, Object> velocityParams, final FieldLayoutItem worklogFieldLayoutItem)
    {
        // never show TimeTracking proxy when Log Work is required
        boolean isTimeTrackingPresentAndShouldBeRendered = false;
        if (!worklogFieldLayoutItem.isRequired())
        {
            // first check for the presence of the TimeTrackingValue in the FieldValuesHolder
            isTimeTrackingPresentAndShouldBeRendered = fieldValuesHolder != null && fieldValuesHolder.containsKey(IssueFieldConstants.TIMETRACKING);
            if (isTimeTrackingPresentAndShouldBeRendered)
            {
                TimeTrackingSystemField.TimeTrackingValue value = (TimeTrackingSystemField.TimeTrackingValue) fieldValuesHolder.get(IssueFieldConstants.TIMETRACKING);

                // if we are in modern mode, then always render
                // if we are in legacy mode, only render when work has started
                if (!isTimeTrackingModernMode())
                {
                    isTimeTrackingPresentAndShouldBeRendered = hasWorkStarted(issue);
                    velocityParams.put("timeTrackingFieldId", IssueFieldConstants.TIMETRACKING);
                    velocityParams.put("remainingEstimateDisplayValue", value.getEstimateDisplayValue());
                }
                else
                {
                    velocityParams.put("timeTrackingFieldId", TimeTrackingSystemField.TIMETRACKING_REMAININGESTIMATE);
                    velocityParams.put("remainingEstimateDisplayValue", value.getRemainingEstimateDisplayValue());
                }
            }
        }
        velocityParams.put("isTimeTrackingPresentAndShouldBeRendered", isTimeTrackingPresentAndShouldBeRendered);

        // find out if the TimeTrackingField is in our FieldLayout and is required
        boolean isTimeTrackingRequired = false;
        if (worklogFieldLayoutItem.getFieldLayout() != null)
        {
            final FieldLayoutItem timeTrackingItem = worklogFieldLayoutItem.getFieldLayout().getFieldLayoutItem(IssueFieldConstants.TIMETRACKING);
            isTimeTrackingRequired = timeTrackingItem.isRequired();
        }
        velocityParams.put("isTimeTrackingRequired", isTimeTrackingRequired);
    }

    private boolean hasWorkStarted(final Issue issue)
    {
        return !getWorklogManager().getByIssue(issue).isEmpty();
    }

    private boolean isTimeTrackingModernMode()
    {
        return getTimeTrackingConfiguration().getMode() == TimeTrackingConfiguration.Mode.MODERN;
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        return "bulk.edit.unavailable";
    }

    /**
     * @return the current date and time, formatted for the date time picker by the {@link OutlookDateManager}.
     */
    private String getCurrentDateTimeFormatted()
    {
        return getOutlookDateManager().getOutlookDate(authenticationContext.getLocale()).formatDateTimePicker(new Date());
    }

    private static String[] asArray(final Map inputParameters, final String key)
    {
        return (String[]) inputParameters.get(key);
    }

    private static String fromArray(final String[] value)
    {
        return value != null && value.length > 0 ? value[0] : null;
    }

    /**
     * Parses the boolean value of a web parameter from a String array. If the value was not specified, false is
     * returned by default.
     *
     * @param booleanArray the web parameters value for the field.
     * @return true or false
     */
    private static boolean parseBoolean(final String[] booleanArray)
    {
        final String s = fromArray(booleanArray);
        return StringUtils.isNotBlank(s) ? Boolean.valueOf(s) : false;
    }

    /**
     * @return the Collection of group names that the current user is in - possibly empty
     */
    private Collection<String> getGroupLevels()
    {
        if (authenticationContext.getUser() == null || !getCommentService().isGroupVisiblityEnabled())
        {
            return Collections.emptyList();
        }
        else
        {
            List<String> userGroups = new ArrayList<String>(authenticationContext.getUser().getGroups());
            Collections.sort(userGroups);
            return userGroups;
        }
    }

    /**
     * @param issue the issue in context
     * @return the Collection of {@link ProjectRole}s for the specified issue that the current user is in - possibly empty
     */
    private Collection<ProjectRole> getRoleLevels(Issue issue)
    {
        if (authenticationContext.getUser() != null && issue != null && getCommentService().isProjectRoleVisiblityEnabled())
        {
            return getProjectRoleManager().getProjectRoles(authenticationContext.getUser(), issue.getProjectObject());
        }
        return Collections.emptyList();
    }

    /**
     * @param issue the current issue
     * @return null if the remaining estimate is not set; the pretty duration formatted remaining estimate otherwise.
     */
    private String getCurrentEstimate(final Issue issue)
    {
        final Long estimate = issue.getEstimate();
        return estimate == null ? null : getJiraDurationUtils().getFormattedDuration(estimate);
    }

    private WorklogService getWorklogService()
    {
        return componentLocator.getComponentInstanceOfType(WorklogService.class);
    }

    private TimeTrackingConfiguration getTimeTrackingConfiguration()
    {
        return componentLocator.getComponentInstanceOfType(TimeTrackingConfiguration.class);
    }

    private ProjectRoleManager getProjectRoleManager()
    {
        return componentLocator.getComponentInstanceOfType(ProjectRoleManager.class);
    }

    private CommentService getCommentService()
    {
        return componentLocator.getComponentInstanceOfType(CommentService.class);
    }

    private OutlookDateManager getOutlookDateManager()
    {
        return componentLocator.getComponentInstanceOfType(OutlookDateManager.class);
    }

    private JiraDurationUtils getJiraDurationUtils()
    {
        return componentLocator.getComponentInstanceOfType(JiraDurationUtils.class);
    }

    private WorklogManager getWorklogManager()
    {
        return componentLocator.getComponentInstanceOfType(WorklogManager.class);
    }

    private RendererManager getRendererManager()
    {
        return componentLocator.getComponentInstanceOfType(RendererManager.class);
    }

    public String getValueFromIssue(final Issue issue)
    {
        return null;
    }

    public boolean isRenderable()
    {
        return true;
    }

    /**
     * A value object used to aggregate all the transient values required in taking input from the UI, validating it and
     * creating the resultant work logs.
     */
    public static interface WorklogValue
    {
        /**
         * @return true if the "Log Work" checkbox is checked.
         */
        boolean isActivated();

        String timeLogged();

        String startDate();
        
        AdjustEstimate adjustEstimate();
        
        String newEstimate();

        Long newEstimateLong();
        
        String adjustmentAmount();

        Long adjustmentAmountLong();

        String comment();

        String commentLevel();

        /**
         * @return the result after invoking the {@link WorklogService}'s "validateCreate" methods. Stored on the WorklogValue
         * so that it can be accessed in the different phases of the field lifecycle.
         */
        WorklogResult worklogResult();

        /**
         * @return true if one of {@link #timeLogged()}, {@link #newEstimate()} or {@link #adjustmentAmount()} is set.
         */
        boolean isSet();

        /**
         * @return true if we are logging work as we are creating an issue; false otherwise
         */
        boolean isCreateIssue();

        /**
         * @return true if we are logging work as we are editing an issue (on the Edit Screen); false otherwise
         */
        boolean isEditIssue();

        /**
         * Denotes the possible states of the radio button group which captures what the user wishes to do with the
         * estimate after logging work.
         */
        enum AdjustEstimate
        {
            AUTO, LEAVE, NEW, MANUAL
        }
        
        static class Builder
        {
            private String timeLogged;
            private String startDate;
            private AdjustEstimate adjustEstimate = AdjustEstimate.AUTO;
            private String newEstimate;
            private Long newEstimateLong = null;
            private String adjustmentAmount;
            private Long adjustmentAmountLong = null;
            private String comment;
            private String commentLevel = null;
            private WorklogResult worklogResult = null;
            private boolean isCreateIssue = false;
            private boolean isEditIssue = false;
            private boolean isActivated = false;

            Builder()
            {
            }

            Builder(final WorklogValue value)
            {
                this.isActivated = value.isActivated();
                this.timeLogged = value.timeLogged();
                this.startDate = value.startDate();
                this.adjustEstimate = value.adjustEstimate();
                this.newEstimate = value.newEstimate();
                this.newEstimateLong = value.newEstimateLong();
                this.adjustmentAmount = value.adjustmentAmount();
                this.adjustmentAmountLong = value.adjustmentAmountLong();
                this.comment = value.comment();
                this.commentLevel = value.commentLevel();
                this.worklogResult = value.worklogResult();
            }

            Builder setActivated(final String[] activated)
            {
                this.isActivated = parseBoolean(activated);
                return this;
            }

            Builder setAdjustEstimate(final String[] adjustEstimate)
            {
                final String s = fromArray(adjustEstimate);
                if (StringUtils.isNotBlank(s))
                {
                    try
                    {
                        this.adjustEstimate = AdjustEstimate.valueOf(s.toUpperCase());
                    }
                    catch (IllegalArgumentException e)
                    {
                        this.adjustEstimate = null;
                    }
                }
                return this;
            }

            Builder setAdjustEstimate(final AdjustEstimate adjustEstimate)
            {
                this.adjustEstimate = adjustEstimate;
                return this;
            }

            Builder setAdjustmentAmount(final String[] adjustmentAmount)
            {
                this.adjustmentAmount = fromArray(adjustmentAmount);
                return this;
            }

            Builder setAdjustmentAmountLong(final Long adjustmentAmount)
            {
                this.adjustmentAmountLong = adjustmentAmount;
                return this;
            }

            Builder setNewEstimate(final String[] newEstimate)
            {
                this.newEstimate = fromArray(newEstimate);
                return this;
            }

            Builder setNewEstimateLong(final Long newEstimate)
            {
                this.newEstimateLong = newEstimate;
                return this;
            }

            Builder setStartDate(final String[] startDate)
            {
                this.startDate = fromArray(startDate);
                return this;
            }

            Builder setTimeLogged(final String[] timeLogged)
            {
                this.timeLogged = fromArray(timeLogged);
                return this;
            }

            Builder setComment(final String[] comment)
            {
                this.comment = fromArray(comment);
                return this;
            }

            Builder setCommentLevel(final String[] commentLevel)
            {
                this.commentLevel = fromArray(commentLevel);
                return this;
            }

            Builder setWorklogResult(final WorklogResult worklogResult)
            {
                this.worklogResult = worklogResult;
                return this;
            }

            Builder setCreateIssue(final boolean createIssue)
            {
                this.isCreateIssue = createIssue;
                return this;
            }

            Builder setEditIssue(final String[] editIssue)
            {
                return setEditIssue(parseBoolean(editIssue));
            }

            Builder setEditIssue(final boolean editIssue)
            {
                this.isEditIssue = editIssue;
                return this;
            }

            /**
             * Parses the specified start date string, expecting it to be formatted as a date time picker string.
             *
             * @param outlookDateManager the outlook date manager
             * @param locale the locale used to render the picker
             * @param startDate the input string
             * @return the parsed Date object or null if it was not parseable
             */
            static Date parseStartDate(final OutlookDateManager outlookDateManager, final Locale locale, final String startDate)
            {
                try
                {
                    return (startDate == null) ? null : outlookDateManager.getOutlookDate(locale).parseDateTimePicker(startDate);
                }
                catch (ParseException e)
                {
                    return null;
                }
            }

            WorklogValue build()
            {
                final boolean isActivated = this.isActivated;
                final String timeLogged = this.timeLogged;
                final String startDate = this.startDate;
                final AdjustEstimate adjustEstimate = this.adjustEstimate;
                final String newEstimate = this.newEstimate;
                final Long newEstimateLong = this.newEstimateLong;
                final String adjustmentAmount = this.adjustmentAmount;
                final Long adjustmentAmountLong = this.adjustmentAmountLong;
                final String comment = this.comment;
                final String commentLevel = this.commentLevel;
                final WorklogResult worklogResult = this.worklogResult;
                final boolean isCreateIssue = this.isCreateIssue;
                final boolean isEditIssue = this.isEditIssue;

                return new WorklogValue()
                {
                    public boolean isActivated()
                    {
                        return isActivated;
                    }

                    public String timeLogged()
                    {
                        return timeLogged;
                    }

                    public String startDate()
                    {
                        return startDate;
                    }

                    public AdjustEstimate adjustEstimate()
                    {
                        return adjustEstimate;
                    }

                    public String newEstimate()
                    {
                        return newEstimate;
                    }

                    public Long newEstimateLong()
                    {
                        return newEstimateLong;
                    }

                    public String adjustmentAmount()
                    {
                        return adjustmentAmount;
                    }

                    public Long adjustmentAmountLong()
                    {
                        return adjustmentAmountLong;
                    }

                    public String comment()
                    {
                        return comment;
                    }

                    public String commentLevel()
                    {
                        return commentLevel;
                    }

                    public WorklogResult worklogResult()
                    {
                        return worklogResult;
                    }

                    public boolean isSet()
                    {
                        return StringUtils.isNotBlank(timeLogged) || StringUtils.isNotBlank(adjustmentAmount) || StringUtils.isNotBlank(newEstimate);
                    }

                    public boolean isCreateIssue()
                    {
                        return isCreateIssue;
                    }

                    public boolean isEditIssue()
                    {
                        return isEditIssue;
                    }
                };
            }
        }
    }
}
