package com.atlassian.jira.web.util;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContextImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.renderer.text.DefaultTextRenderer;
import com.atlassian.jira.issue.operation.IssueOperations;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.JiraVelocityUtils;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.component.AbstractWebComponent;
import com.atlassian.util.profiling.UtilTimerStack;
import com.atlassian.velocity.VelocityManager;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;
import webwork.action.ActionContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to render a Quick Sub Task Creation (html) form. This form is primarily used on the View Issue page
 * when sub-tasks are enabled to allow users to quickly create new sub tasks. This web component allows to show
 * the form anywhere.
 * <p/>
 * To use this Web Component instantiate it and then call its {@link #getHtml()} method. To instantiate this object you
 * will need to pass in the parent issue for which the sub-tasks will be created, and a WebWork action that backs the
 * display operation. This class also has quite a few dependencies on JIRA Manager Objects. The easiest way to
 * instantiate it is to use {@link com.atlassian.jira.util.JiraUtils}.
 * <p/>
 * <p/>
 * For example:
 * <pre>
 * SubTaskQuickCreationWebComponent component =
 *         (SubTaskQuickCreationWebComponent) JiraUtils.loadComponent(SubTaskQuickCreationWebComponent.class,
 *                                                                    EasyList.build(issue, action));
 * String html = component.getHtml();
 * </pre>
 * </p>
 * <p/>
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class SubTaskQuickCreationWebComponent extends AbstractWebComponent
{
    private static final Logger log = Logger.getLogger(SubTaskQuickCreationWebComponent.class);

    private final Issue parentIssue;
    private final Action action;
    private final FieldManager fieldManager;
    private final JiraAuthenticationContext authenticationContext;
    private final SubTaskQuickCreationConfig config;
    private final MutableIssue subTask;
    private final FieldVisibilityManager fieldVisibilityManager;
    private final List<String> subTaskIssueTypeIds;
    private final I18nBean i18nBean;

    private final OperationContextImpl operationContext;

    public SubTaskQuickCreationWebComponent(final Issue parentIssue, final Action action, final IssueFactory issueFactory, final SubTaskManager subTaskManager, final FieldManager fieldManager, final VelocityManager velocityManager, final JiraAuthenticationContext authenticationContext, final ApplicationProperties applicationProperties, final SubTaskQuickCreationConfig config)
    {
        super(velocityManager, applicationProperties);
        i18nBean = new I18nBean(authenticationContext.getUser());
        this.parentIssue = parentIssue;
        this.action = action;
        this.fieldManager = fieldManager;
        this.authenticationContext = authenticationContext;
        this.config = config;
        fieldVisibilityManager = new FieldVisibilityBean();

        operationContext = new OperationContextImpl(IssueOperations.CREATE_ISSUE_OPERATION, new HashMap());

        // Initialise the sub-task issue
        subTask = issueFactory.getIssue((GenericValue) null);
        subTask.setProject(this.parentIssue.getProject());
        subTask.setParentId(this.parentIssue.getId());
        if (subTaskManager.getSubTasksIssueTypes().size() > 0)
        {
            // Set issue type to that of the first subtask, so that custom fields don't break.
            final GenericValue issueType = subTaskManager.getSubTasksIssueTypes().iterator().next();
            subTask.setIssueType(issueType);
        }

        // Initialise sub-task issue type ids
        subTaskIssueTypeIds = new ArrayList<String>();
        for (final GenericValue subTaskIssueTypeGV : subTaskManager.getSubTasksIssueTypes())
        {
            subTaskIssueTypeIds.add(subTaskIssueTypeGV.getString("id"));
        }

        setHistoryIssueType();
    }

    public Issue getParentIssue()
    {
        return parentIssue;
    }

    public Issue getSubTask()
    {
        return subTask;
    }

    /**
     * Generates HTML view (input control) for the field with given field id. If the field is visible
     * for <i>at least one</i> sub-task issue type in the parent issue's project the HTML for the input control is
     * returned. If the field is hidden for <i>all</i> sub-task issue types of the parent issue's project then &amp;nbsp
     * is returned.
     * <p/>
     * <p/>
     * Remember that sub-tasks are always in the same project as the parent issue
     * </p>
     * <p/>
     * <p/>
     * This method is used to generate input HTML for a field by the (velocity) template of this object. It is not
     * really intended to be used by anything other than the template.
     * </p>
     *
     * @param fieldId     the id of the field for with HTML shoudl be generated. See
     *                    {@link com.atlassian.jira.issue.IssueFieldConstants} for the list of issue fields.
     * @param showHeaders if set to true headers will be shown (true for vertical layout, false for horizontal layout)
     * @return HTML of the "input control" of the field. It will be wrapped in a <td> in expectation of being included in table.
     */

    public String getSubTaskFieldHtml(final String fieldId, final boolean showHeaders)
    {
        // If the field is not hidden in AT LEAST one field layout for the project then show the field
        if (!fieldVisibilityManager.isFieldHiddenInAllSchemes(subTask.getProjectObject().getId(), fieldId, subTaskIssueTypeIds))
        {
            final OrderableField field = fieldManager.getOrderableField(fieldId);

            if (field != null)
            {
                if (isFieldInScope(field))
                {
                    // Ensure the user is allowed to edit the field. For example, one must have Assign Issue permission
                    // to edit the Assignee field (i.e. change and/or set the assignee of the issue)
                    if (field.isShown(subTask))
                    {
                        final StringBuffer sb = new StringBuffer();
                        // if horizontal layout
                        if (!showHeaders)
                        {
                            final String i18nKey = config.getFieldI18nLabelKey(field.getId());
                            // if key specified in jira-appliaction.properties#jira.subtask.quickcreateform.fields
                            if (i18nKey != null)
                            {
                                sb.append("<td id=\"label_").append(fieldId).append("\" class=\"minNoWrap\" style=\"text-align:right;\">");
                                sb.append(i18nBean.getText(i18nKey));
                                sb.append(":</td>");
                            }
                        }
                        final FieldLayoutItem layoutItem = getLayoutItem(field);
                        final Map displayParameters = EasyMap.build(OrderableField.NO_HEADER_PARAM_KEY, Boolean.valueOf(!showHeaders));
                        sb.append(field.getCreateHtml(layoutItem, operationContext, action, subTask, displayParameters));
                        return sb.toString();
                    }
                    else
                    {
                        log.debug("The user does not have permissions to edit field with id '" + fieldId + "'.");
                        return "<td>&nbsp</td>";
                    }
                }
            }
            else
            {
                log.error("Cannot find field with id '" + fieldId + "'.");
            }
        }
        return showHeaders ? "" : "<td>&nbsp;</td>";
    }

    public String getSubTaskFieldPreset(final String fieldId)
    {
        return config.getPreset(fieldId);
    }

    protected boolean isFieldInScope(final OrderableField field)
    {
        if (field != null)
        {
            // If we have a custom field, then ensure it is in the correct scope.
            if (fieldManager.isCustomField(field))
            {
                final CustomField customField = fieldManager.getCustomField(field.getId());
                if (customField.isInScope(subTask.getProjectObject(), Collections.<String>emptyList()))
                {
                    return true;
                }
                else
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("Custom field with id '" + field.getId() + "' is not in scope for project '" + subTask.getProjectObject().getName() + "'.");
                    }
                    return false;
                }
            }
            else
            {
                return true;
            }
        }
        else
        {
            return false;
        }
    }

    protected void setHistoryIssueType()
    {

        final String issueTypeId = (String) ActionContext.getSession().get(SessionKeys.USER_HISTORY_SUBTASK_ISSUETYPE);

        if (issueTypeId != null)
        {
            operationContext.getFieldValuesHolder().put(IssueFieldConstants.ISSUE_TYPE, issueTypeId);
        }
    }

    /**
     * Generates and returns the HTML of the Sub-Task Quick Creation form.
     *
     * @return HTML of the Sub-Task Quick Creation form
     */
    public String getHtml()
    {
        try
        {
            UtilTimerStack.push(getClass().getName());
            final I18nHelper i18n = authenticationContext.getI18nHelper();
            final Map<String, Object> startingParams = MapBuilder.<String, Object>newBuilder("i18n", i18n)
                    .add("webComponent", this).add("displayFieldIds", getDisplayFieldIds())
                    .add("presetFieldIds", getPresetFieldIds()).toHashMap();

            final Map<String, Object> params = JiraVelocityUtils.getDefaultVelocityParams(startingParams, authenticationContext);
            return getHtml(config.getVelocityTemplate(), params);
        }
        finally
        {
            UtilTimerStack.pop(getClass().getName());
        }
    }

    /**
     * Returns a collection of {@link OrderableField} ids that will be dispalyed on the sub-task creation form.
     *
     * @return collection of {@link OrderableField} ids that will be dispalyed on the sub-task creation form
     */
    protected Collection getDisplayFieldIds()
    {
        return config.getDisplayFieldIds();
    }

    /**
     * Returns a collection of {@link OrderableField} ids which have predefined values.
     *
     * @return a collection of {@link OrderableField} ids which have predefined values
     */
    protected Collection getPresetFieldIds()
    {
        return config.getPresetFieldIds();
    }

    private FieldLayoutItem getLayoutItem(final OrderableField field)
    {
        return new FieldLayoutItem()
        {
            public OrderableField getOrderableField()
            {
                return field;
            }

            public String getFieldDescription()
            {
                // Do not show description of the field on the quick creation form
                return "";
            }

            public boolean isHidden()
            {
                // The field is shown, as otherwise this object would not be created. i.e. the code would never
                // get here
                return false;
            }

            public boolean isRequired()
            {
                return false;
            }

            public String getRendererType()
            {
                return DefaultTextRenderer.RENDERER_TYPE;
            }

            public FieldLayout getFieldLayout()
            {
                return null;
            }

            // TODO: Implement properly JRA-13216
            public int compareTo(final FieldLayoutItem o)
            {
                return 0;
            }

        };
    }
}
